/*
 * Copyright 2016 ITEA 12004 SEAS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.thesmartenergy.sparql.generate.jena.function.library;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerate;
import java.math.BigDecimal;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueBoolean;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDecimal;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDouble;
import org.apache.jena.sparql.expr.nodevalue.NodeValueFloat;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import static javax.management.Query.value;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.nodevalue.NodeValueDT;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.function.FunctionBase1;

/**
 * A SPARQL Function that converts a timestamp (an xsd:int) to a xsd:dateTime.
 * The Function URI is {@code <http://w3id.org/sparql-generate/fn/dateTime>}.
 *
 * @author Maxime Lefrançois <maxime.lefrancois at emse.fr>
 */
public final class FN_DateTime extends FunctionBase1 {

    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(FN_DateTime.class);

    /**
     * The SPARQL function URI.
     */
    public static final String URI = SPARQLGenerate.FN + "dateTime";

    /**
     *
     */
    @Override
    public NodeValue exec(NodeValue timeStampValue) {

        BigInteger timeStamp = timeStampValue.getInteger();

        if (timeStamp == null) {
            LOG.warn("The NodeValue " + timeStampValue + " MUST be an integer."
                    + " Returning null.");
        }
        if (timeStamp.signum() != 1) {
            LOG.warn("The NodeValue " + timeStamp + " MUST be positive."
                    + " Returning null.");
        }
        if (timeStamp.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) != -1) {
            LOG.warn("The NodeValue " + timeStamp + " MUST be less than the biggest long value."
                    + " Returning null.");
        }

        Timestamp stamp = new Timestamp(timeStamp.longValue());
        Date date = new Date(stamp.getTime());
        
        Node node = NodeFactory.createLiteral(df.format(date), XSDDatatype.XSDdateTime);
        return new NodeValueNode(node);
    }
}
