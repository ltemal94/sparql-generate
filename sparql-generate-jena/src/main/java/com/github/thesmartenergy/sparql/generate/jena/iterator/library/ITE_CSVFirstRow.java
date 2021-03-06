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
package com.github.thesmartenergy.sparql.generate.jena.iterator.library;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerate;
import com.github.thesmartenergy.sparql.generate.jena.iterator.IteratorFunctionBase1;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.List;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.log4j.Logger;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.supercsv.io.ICsvListReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * A SPARQL Iterator function that iterates over the columns of a CSV document.
 * It returns a JSON string with a specific JSON structure. The structure is
 * {@code {"cell":string,"header":string,"position":integer,"hasNext":boolean}}.
 * The Iterator function URI is
 * {@code <http://w3id.org/sparql-generate/iter/CSVFirstRow>}.
 *
 * @author Maxime Lefrançois <maxime.lefrancois at emse.fr>
 */
public class ITE_CSVFirstRow extends IteratorFunctionBase1 {

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(ITE_CSVFirstRow.class);

    /**
     * The SPARQL function URI.
     */
    public static final String URI = SPARQLGenerate.ITER + "CSVFirstRow";

    /**
     * The datatype URI of the first parameter and the return literals.
     */
    private static final String datatypeUri = "http://www.iana.org/assignments/media-types/text/csv";

    /**
     *
     * @param csv the source CSV document which is a RDF Literal with datatype
     * URI {@code <http://www.iana.org/assignments/media-types/text/csv>} or {@code xsd:string}
     * @return a list of string literals representing the headers of the CSV
     * document.
     */
    @Override
    public List<NodeValue> exec(NodeValue csv) {

        if (csv.getDatatypeURI() == null
                && datatypeUri == null
                || csv.getDatatypeURI() != null
                && !csv.getDatatypeURI().equals(datatypeUri)
                && !csv.getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#string")) {
            LOG.warn("The URI of NodeValue1 MUST be"
                    + " <" + datatypeUri + "> or"
                    + " <http://www.w3.org/2001/XMLSchema#string>. Got <"
                    + csv.getDatatypeURI() + ">. Returning null.");
        }
        RDFDatatype dt = TypeMapper.getInstance()
                .getSafeTypeByName(datatypeUri);
        try {

            String sourceCSV = String.valueOf(csv.asNode().getLiteralLexicalForm());

            ICsvListReader listReader = null;
            InputStream is = new ByteArrayInputStream(sourceCSV.getBytes());
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);
            listReader = new CsvListReader(br, CsvPreference.STANDARD_PREFERENCE);

            String[] header = listReader.getHeader(true);
            Iterator<String> row = listReader.read().iterator();
            List<NodeValue> nodeValues = new ArrayList<>(listReader.length());
            
            int i = -1;
            while(row.hasNext()) {
                StringBuilder json = new StringBuilder();
                String cell = row.next();
                i++;
                json.append("{");
                json.append("\"header\":\"").append(header[i].replace("\"", "\\\"")).append("\"");
                json.append(",\"cell\":\"").append(cell.replace("\"", "\\\"")).append("\"");
                json.append(",\"position\":").append(i);
                json.append(",\"hasNext\":").append(row.hasNext());
                json.append("}");
                RDFDatatype dtjson = TypeMapper.getInstance().getSafeTypeByName("http://www.iana.org/assignments/media-types/application/json");
                Node node = NodeFactory.createLiteral(json.toString(), dtjson);
                NodeValue nodeValue = new NodeValueNode(node);
                nodeValues.add(nodeValue);
            }
            return nodeValues;
        } catch (Exception e) {
            throw new ExprEvalException("FunctionBase: no evaluation", e);
        }
    }

}
