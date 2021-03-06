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
package com.github.thesmartenergy.sparql.generate.jena.engine;

import com.github.thesmartenergy.sparql.generate.jena.SPARQLGenerate;
import com.github.thesmartenergy.sparql.generate.jena.query.SPARQLGenerateQuery;
import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.util.Locator;
import org.apache.jena.util.LocatorFile;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Maxime Lefrançois <maxime.lefrancois at emse.fr>
 */
public class CSVW extends TestBase {

    static Logger LOG;
    static URL examplePath;
    static File exampleDir;
    static FileManager fileManager;

    public CSVW() {

    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        LOG = Logger.getLogger(CSVW.class);
        LOG.debug(CSVW.class.getName());
        examplePath = CSVW.class.getResource("/csvw");
        exampleDir = new File(examplePath.toURI());

        // read location-mapping
        URI confUri = exampleDir.toURI().resolve("configuration.ttl");
        Model conf = RDFDataMgr.loadModel(confUri.toString());

        // initialize file manager
        fileManager = FileManager.makeGlobal();
        Locator loc = new LocatorFile(exampleDir.toURI().getPath());
        LocationMapper mapper = new LocationMapper(conf);
        fileManager.addLocator(loc);
        fileManager.setLocationMapper(mapper);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    @Test
    public void test001() throws Exception {
        testSimple("test001");
    }

    public void testSimple(String value) throws Exception {
        String qstring = IOUtils.toString(fileManager.open("query.rqg"), "UTF-8");
        SPARQLGenerateQuery q = (SPARQLGenerateQuery) QueryFactory.create(qstring, SPARQLGenerate.SYNTAX);

        // create generation plan
        PlanFactory factory = new PlanFactory(fileManager);
        RootPlan plan = factory.create(q);
        Model output = ModelFactory.createDefaultModel();
        QuerySolutionMap initialBinding = new QuerySolutionMap();

        Model model = ModelFactory.createDefaultModel();

        String variable = "csvfile";
        RDFNode jenaLiteral = model.createResource(value + ".csv");
        initialBinding.add(variable, jenaLiteral);

        // execute plan
        plan.exec(initialBinding, output);

        // write output
        output.write(System.out, "TTL");

        Model expectedOutput = fileManager.loadModel(value + ".ttl");
        StringWriter sw = new StringWriter();
        expectedOutput.write(sw, "TTL");
        LOG.debug(sw.toString());

        Assert.assertTrue(output.isIsomorphicWith(expectedOutput));
    }

}
