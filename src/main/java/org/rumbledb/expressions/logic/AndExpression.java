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

package org.rumbledb.expressions.logic;

import org.rumbledb.exceptions.ExceptionMetadata;
import org.rumbledb.expressions.AbstractNodeVisitor;
import org.rumbledb.expressions.Expression;
import org.rumbledb.expressions.Node;

import java.util.Arrays;
import java.util.List;

public class AndExpression extends Expression {
    private static final long serialVersionUID = 1L;

    private Expression leftExpression;
    private Expression rightExpression;

    public AndExpression(
            Expression leftExpression,
            Expression rightExpression,
            ExceptionMetadata metadata
    ) {
        super(metadata);
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    @Override
    public <T> T accept(AbstractNodeVisitor<T> visitor, T argument) {
        return visitor.visitAndExpr(this, argument);
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.asList(this.leftExpression, this.rightExpression);
    }

    @Override
    public void serializeToJSONiq(StringBuffer sb, int indent) {
        indentIt(sb, indent);
        sb.append("(\n");

        this.leftExpression.serializeToJSONiq(sb, indent + 1);

        indentIt(sb, indent);
        sb.append(")\n");

        indentIt(sb, indent);
        sb.append("and\n");

        indentIt(sb, indent);
        sb.append("(\n");

        this.rightExpression.serializeToJSONiq(sb, indent + 1);

        indentIt(sb, indent);
        sb.append(")\n");
    }


}
