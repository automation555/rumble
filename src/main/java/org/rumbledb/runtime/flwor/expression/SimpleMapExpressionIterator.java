/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Stefan Irimescu, Can Berker Cikis
 *
 */

package org.rumbledb.runtime.flwor.expression;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.rumbledb.api.Item;
import org.rumbledb.context.DynamicContext;
import org.rumbledb.context.Name;
import org.rumbledb.exceptions.ExceptionMetadata;
import org.rumbledb.exceptions.IteratorFlowException;
import org.rumbledb.expressions.ExecutionMode;
import org.rumbledb.runtime.HybridRuntimeIterator;
import org.rumbledb.runtime.RuntimeIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

public class SimpleMapExpressionIterator extends HybridRuntimeIterator {

    private static final long serialVersionUID = 1L;
    private RuntimeIterator leftIterator;
    private RuntimeIterator rightIterator;
    private Item nextResult;
    private DynamicContext mapDynamicContext;
    private Queue<Item> mapValues;


    public SimpleMapExpressionIterator(
            RuntimeIterator sequence,
            RuntimeIterator mapExpression,
            ExecutionMode executionMode,
            ExceptionMetadata iteratorMetadata
    ) {
        super(Arrays.asList(sequence, mapExpression), executionMode, iteratorMetadata);
        this.leftIterator = sequence;
        this.rightIterator = mapExpression;
        this.mapDynamicContext = null;
    }

    @Override
    public JavaRDD<Item> getRDDAux(DynamicContext dynamicContext) {
        JavaRDD<Item> childRDD = this.children.get(0).getRDD(dynamicContext);
        FlatMapFunction<Item, Item> transformation = new SimpleMapExpressionClosure(this.rightIterator, dynamicContext);
        return childRDD.flatMap(transformation);
    }

    @Override
    protected void openLocal() {
        this.mapDynamicContext = new DynamicContext(this.currentDynamicContextForLocalExecution);
        this.mapValues = new LinkedList<>();
        this.leftIterator.open(this.currentDynamicContextForLocalExecution);
        setNextResult();
    }

    @Override
    protected void closeLocal() {
        this.leftIterator.close();
    }

    @Override
    protected void resetLocal() {
        this.mapDynamicContext = new DynamicContext(this.currentDynamicContextForLocalExecution);
        this.mapValues = new LinkedList<>();
        this.leftIterator.reset(this.currentDynamicContextForLocalExecution);
        setNextResult();
    }

    @Override
    protected boolean hasNextLocal() {
        return this.hasNext;
    }

    @Override
    protected Item nextLocal() {
        if (this.hasNext) {
            Item result = this.nextResult; // save the result to be returned
            setNextResult(); // calculate and store the next result
            return result;
        }
        throw new IteratorFlowException("Invalid next() call in simple map expression", getMetadata());
    }

    private void setNextResult() {
        this.nextResult = null;

        if (this.mapValues.size() > 0) {
            this.nextResult = this.mapValues.poll();
            this.hasNext = true;
        } else if (this.leftIterator.hasNext()) {
            List<Item> mapValuesRaw = getRightIteratorValues();
            while (mapValuesRaw.size() == 0 && this.leftIterator.hasNext()) { // Discard all empty sequences
                mapValuesRaw = getRightIteratorValues();
            }

            if (mapValuesRaw.size() == 1) {
                this.nextResult = mapValuesRaw.get(0);
            } else {
                this.mapValues.addAll(mapValuesRaw);
                this.nextResult = this.mapValues.poll();
            }
        }
        if (this.nextResult != null) {
            this.hasNext = true;
        } else {
            this.hasNext = false;
        }
    }

    private List<Item> getRightIteratorValues() {
        Item item = this.leftIterator.next();
        List<Item> currentItems = new ArrayList<>();
        this.mapDynamicContext.getVariableValues().addVariableValue(Name.CONTEXT_ITEM, currentItems);
        currentItems.add(item);
        List<Item> mapValuesRaw = this.rightIterator.materialize(this.mapDynamicContext);
        this.mapDynamicContext.getVariableValues().removeVariable(Name.CONTEXT_ITEM);
        return mapValuesRaw;
    }

    public Map<Name, DynamicContext.VariableDependency> getVariableDependencies() {
        Map<Name, DynamicContext.VariableDependency> result =
            new TreeMap<Name, DynamicContext.VariableDependency>();
        result.putAll(this.rightIterator.getVariableDependencies());
        result.remove(Name.CONTEXT_ITEM);
        result.putAll(this.leftIterator.getVariableDependencies());
        return result;
    }
}
