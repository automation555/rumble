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

package org.rumbledb.expressions.primary;


import org.rumbledb.compiler.VisitorConfig;
import org.rumbledb.context.FunctionIdentifier;
import org.rumbledb.context.Name;
import org.rumbledb.exceptions.ExceptionMetadata;
import org.rumbledb.expressions.AbstractNodeVisitor;
import org.rumbledb.expressions.Expression;
import org.rumbledb.expressions.Node;
import org.rumbledb.types.SequenceType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InlineFunctionExpression extends Expression {

    private final Name name;
    private final FunctionIdentifier functionIdentifier;
    private final Map<Name, SequenceType> params;
    private final SequenceType returnType;
    private final Expression body;

    public InlineFunctionExpression(
            Name name,
            Map<Name, SequenceType> params,
            SequenceType returnType,
            Expression body,
            ExceptionMetadata metadata
    ) {
        super(metadata);
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.body = body;
        this.functionIdentifier = new FunctionIdentifier(name, params.size());
    }

    public Name getName() {
        return this.name;
    }

    public FunctionIdentifier getFunctionIdentifier() {
        return this.functionIdentifier;
    }

    public Map<Name, SequenceType> getParams() {
        return this.params;
    }

    public SequenceType getReturnType() {
        return this.returnType == null ? SequenceType.ITEM_STAR : this.returnType;
    }

    public SequenceType getActualReturnType() {
        return this.returnType;
    }

    public Expression getBody() {
        return this.body;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(this.body);
    }

    public void registerUserDefinedFunctionExecutionMode(
            VisitorConfig visitorConfig
    ) {
        FunctionIdentifier identifier = new FunctionIdentifier(this.name, this.params.size());
        // if named(static) function declaration
        if (this.name != null) {
            getStaticContext().getUserDefinedFunctionsExecutionModes()
                .setExecutionMode(
                    identifier,
                    this.body.getHighestExecutionMode(visitorConfig),
                    visitorConfig.suppressErrorsForFunctionSignatureCollision(),
                    this.getMetadata()
                );
        }
    }

    @Override
    public <T> T accept(AbstractNodeVisitor<T> visitor, T argument) {
        return visitor.visitInlineFunctionExpr(this, argument);
    }

    public void print(StringBuffer buffer, int indent) {
        for (int i = 0; i < indent; ++i) {
            buffer.append("  ");
        }
        buffer.append(getClass().getSimpleName());
        buffer.append("(");
        for (Map.Entry<Name, SequenceType> entry : this.params.entrySet()) {
            buffer.append(entry.getKey());
            buffer.append(", ");
            buffer.append(
                entry.getValue().toString() + (entry.getValue().isResolved() ? " (resolved)" : " (unresolved)")
            );
            buffer.append(", ");
        }
        buffer.append(
            this.returnType == null
                ? "not set"
                : this.returnType.toString() + (this.returnType.isResolved() ? " (resolved)" : " (unresolved)")
        );
        buffer.append(")");
        buffer.append(" | " + this.highestExecutionMode);
        buffer.append(
            " | "
                + (this.staticSequenceType == null
                    ? "not set"
                    : this.staticSequenceType
                        + (this.staticSequenceType.isResolved() ? " (resolved)" : " (unresolved)"))
        );
        buffer.append("\n");
        for (int i = 0; i < indent + 2; ++i) {
            buffer.append("  ");
        }
        buffer.append("Body:\n");
        this.body.print(buffer, indent + 2);
    }

    @Override
    public void serializeToJSONiq(StringBuffer sb, int indent) {
        indentIt(sb, indent);
        if (this.name != null) {
            sb.append("declare function " + this.name.toString() + "(");
        } else {
            sb.append("function (");
        }
        if (this.params != null) {
            int i = 0;
            for (Map.Entry<Name, SequenceType> entry : this.params.entrySet()) {
                indentIt(sb, indent);
                sb.append("$" + entry.getKey() + " as " + entry.getValue().toString());
                if (i == this.params.size() - 1) {
                    sb.append(")");
                } else {
                    sb.append(", ");
                }
                i++;
            }
            if (this.returnType != null)
                sb.append(" as " + this.returnType.toString());
            else
                sb.append("\n");
            indentIt(sb, indent);
            sb.append("{\n");
            this.body.serializeToJSONiq(sb, indent + 1);
            indentIt(sb, indent);
            sb.append("}\n");
        }
    }
}

