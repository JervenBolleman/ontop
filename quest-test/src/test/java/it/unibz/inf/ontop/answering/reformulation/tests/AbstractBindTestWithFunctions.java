package it.unibz.inf.ontop.answering.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
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
 * #L%
 */

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;
import static org.junit.Assert.assertTrue;

/***
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 * Refer in particular to the class {@link it.unibz.inf.ontop.owlrefplatform.core.translator.SparqlAlgebraToDatalogTranslator}
 */

public abstract class AbstractBindTestWithFunctions {

    private final String owlfile;
    private final String obdafile;
    private final String propertiesfile;

    protected static Logger log = LoggerFactory.getLogger(AbstractBindTestWithFunctions.class);


    protected AbstractBindTestWithFunctions(String owlfile, String obdafile, String propertiesfile) {
        this.owlfile = owlfile;
        this.obdafile = obdafile;
        this.propertiesfile = propertiesfile;
    }

    private void runTests(String query) throws Exception {

        // Creating a new instance of the reasoner

        QuestOWLFactory factory = new QuestOWLFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertiesfile)
                .build();
        QuestOWL reasoner = factory.createReasoner(config);

        // Now we are ready for querying
        OntopOWLConnection conn = reasoner.getConnection();
        OntopOWLStatement st = conn.createStatement();


        int i = 0;

        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            while (rs.nextRow()) {
                OWLObject ind1 = rs.getOWLObject("w");


                System.out.println(ind1);
                i++;
            }
            assertTrue(i > 0);

        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
    }


	/*
	 * Tests for numeric functions
	 */


    @Test
    public void testCeil() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CEIL(?discount) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getCeilExpectedValues());
    }

    protected List<String> getCeilExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"1\"^^xsd:decimal");
        expectedValues.add("\"1\"^^xsd:decimal");
        expectedValues.add("\"1\"^^xsd:decimal");
        expectedValues.add("\"1\"^^xsd:decimal");

        return expectedValues;
    }


    @Test
    public void testFloor() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (FLOOR(?discount) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getFloorExpectedValues());
    }

    protected List<String> getFloorExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        return expectedValues;
    }


    @Test
    public void testRound() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (CONCAT(ROUND(?discount),', ',ROUND(?p)) AS ?w)\n"
                + "}";


        checkReturnedValues(queryBind, getRoundExpectedValues());
    }

    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0, 43.0\"");
        expectedValues.add("\"0.0, 23.0\"");
        expectedValues.add("\"0.0, 34.0\"");
        expectedValues.add("\"0.0, 10.0\"");
        return expectedValues;
    }

    @Test
    public void testAbs() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (ABS((?p - ?discount*?p) - ?p)  AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getAbsExpectedValues());
    }

    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.5\"^^xsd:decimal");
        expectedValues.add("\"5.75\"^^xsd:decimal");
        expectedValues.add("\"6.7\"^^xsd:decimal");
        expectedValues.add("\"1.5\"^^xsd:decimal");
        return expectedValues;
    }

	/*
	 * Tests for hash functions. H2 supports only SHA256 algorithm.
	 */

    @Test
    public void testHash() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount.\n"
                + "   ?x dc:title ?title .\n"
                + "   FILTER (STRSTARTS(?title, \"The S\"))\n"
                + "   BIND (SHA256(?title) AS ?w)\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest("The Semantic Web".getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            expectedValues.add(String.format("\"%s\"",hexString.toString()));
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
        checkReturnedValues(queryBind, expectedValues);

    }


	/*
	 * Tests for functions on strings.
	 */

    @Test
    public void testStrLen() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRLEN(?title) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"15\"^^xsd:integer");
        expectedValues.add("\"16\"^^xsd:integer");
        expectedValues.add("\"20\"^^xsd:integer");
        expectedValues.add("\"44\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
    }

    //test substring with 2 parameters
    @Test
    public void testSubstr2() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (SUBSTR(?title, 3) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"ARQL Tutorial\"@en");  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"e Semantic Web\"@en");
        expectedValues.add("\"ime and Punishment\"@en");
        expectedValues.add("\"e Logic Book: Introduction, Second Edition\"@en");
        checkReturnedValues(queryBind, expectedValues);
    }

    //test substring with 3 parameters
    @Test
    public void testSubstr3() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (SUBSTR(?title, 3, 6) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"ARQL T\"@en");   // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"e Sema\"@en");
        expectedValues.add("\"ime an\"@en");
        expectedValues.add("\"e Logi\"@en");
        checkReturnedValues(queryBind, expectedValues);
    }
    @Test
    public void testURIEncoding() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   FILTER (STRSTARTS(?title,\"The\"))\n"
                + "   BIND (ENCODE_FOR_URI(?title) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The%20Semantic%20Web\"");
        expectedValues.add("\"The%20Logic%20Book%3A%20Introduction%2C%20Second%20Edition\"");
        checkReturnedValues(queryBind, expectedValues);
    }



    @Test
    public void testStrEnds() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(?title AS ?w)\n"
                + "   FILTER(STRENDS(?title,\"b\"))\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The Semantic Web\"@en");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testStrStarts() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(?title AS ?w)\n"
                + "   FILTER(STRSTARTS(?title,\"The\"))\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The Semantic Web\"@en");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition\"@en");

        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testStrSubstring() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(SUBSTR(?title,1,STRLEN(?title)) AS ?w)\n"
                + "   FILTER(STRSTARTS(?title,\"The\"))\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The Semantic Web\"@en"); // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"The Logic Book: Introduction, Second Edition\"@en");  // ROMAN (23 Dec 2015): now the language tag is handled correctly

        checkReturnedValues(queryBind, expectedValues);
    }
    @Test
    public void testContains() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND(?title AS ?w)\n"
                + "   FILTER(CONTAINS(?title,\"Semantic\"))\n"
                + "}";

        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"The Semantic Web\"@en");
        checkReturnedValues(queryBind, expectedValues);

    }


    @Test
    public void testBindWithUcase() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (UCASE(?title) AS ?v)\n"
                + "   BIND (CONCAT(?title, \" \", ?v) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial SPARQL TUTORIAL\"");
        expectedValues.add("\"The Semantic Web THE SEMANTIC WEB\"");
        expectedValues.add("\"Crime and Punishment CRIME AND PUNISHMENT\"");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition " +
                "The Logic Book: Introduction, Second Edition\"".toUpperCase());
        checkReturnedValues(queryBind, expectedValues);

    }

    @Test
    public void testBindWithLcase() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (LCASE(?title) AS ?v)\n"
                + "   BIND (CONCAT(?title, \" \", ?v) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"SPARQL Tutorial sparql tutorial\"");
        expectedValues.add("\"The Semantic Web the semantic web\"");
        expectedValues.add("\"Crime and Punishment crime and punishment\"");
        expectedValues.add("\"The Logic Book: Introduction, Second Edition " +
                "The Logic Book: Introduction, Second Edition\"".toLowerCase());
        checkReturnedValues(queryBind, expectedValues);

    }


    @Test
    public void testBindWithBefore() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRBEFORE(?title,\"ti\") AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getBindWithBeforeExpectedValues());

    }

    protected List<String> getBindWithBeforeExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"@en");  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\"The Seman\"@en");
        expectedValues.add("\"\"@en");
        expectedValues.add("\"The Logic Book: Introduc\"@en");

        return expectedValues;
    }


    @Test
    public void testBindWithAfter() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   BIND (STRAFTER(?title,\"The\") AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getBindWithAfterExpectedValues());

    }

    protected List<String> getBindWithAfterExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"\"@en");  // ROMAN (23 Dec 2015): now the language tag is handled correctly
        expectedValues.add("\" Semantic Web\"@en");
        expectedValues.add("\"\"@en");
        expectedValues.add("\" Logic Book: Introduction, Second Edition\"@en");

        return expectedValues;
    }


	/*
	 * Tests for functions on date and time
	 */


    @Test
    public void testMonth() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (MONTH(?year) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getMonthExpectedValues());
    }

    protected List<String> getMonthExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"6\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"11\"^^xsd:integer");

        return expectedValues;
    }

    @Test
    public void testYear() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (YEAR(?year) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getYearExpectedValues());
    }

    protected List<String> getYearExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2014\"^^xsd:integer");
        expectedValues.add("\"2011\"^^xsd:integer");
        expectedValues.add("\"2015\"^^xsd:integer");
        expectedValues.add("\"1967\"^^xsd:integer");

        return expectedValues;
    }

    @Test
    public void testDay() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (DAY(?year) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getDayExpectedValues());
    }

    protected List<String> getDayExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"5\"^^xsd:integer");
        expectedValues.add("\"8\"^^xsd:integer");
        expectedValues.add("\"21\"^^xsd:integer");
        expectedValues.add("\"5\"^^xsd:integer");

        return expectedValues;
    }

    @Test
    public void testMinutes() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (MINUTES(?year) AS ?w)\n"
                + "}";


        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"47\"^^xsd:integer");
        expectedValues.add("\"30\"^^xsd:integer");
        expectedValues.add("\"23\"^^xsd:integer");
        expectedValues.add("\"50\"^^xsd:integer");
        checkReturnedValues(queryBind, expectedValues);
    }

    @Test
    public void testHours() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (HOURS(?year) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getHoursExpectedValues());
    }

    protected List<String> getHoursExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"18\"^^xsd:integer");
        expectedValues.add("\"12\"^^xsd:integer");
        expectedValues.add("\"9\"^^xsd:integer");
        expectedValues.add("\"7\"^^xsd:integer");
        return expectedValues;
    }


    @Test
    public void testSeconds() throws Exception {


        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (SECONDS(?year) AS ?w)\n"
                + "}";

        checkReturnedValues(queryBind, getSecondsExpectedValues());
    }

    protected List<String> getSecondsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"52\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");
        expectedValues.add("\"6\"^^xsd:decimal");
        expectedValues.add("\"0\"^^xsd:decimal");

        return expectedValues;
    }

    @Test
    public void testNow() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title ?w WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "   BIND (NOW() AS ?w)\n"
                + "}";

        runTests(queryBind);
    }

    @Test
    public void testUuid() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (UUID() AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "}";


        runTests(queryBind);
    }

    @Test
    public void testStrUuid() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (STRUUID() AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "}";


        runTests(queryBind);
    }

    @Test
    public void testRand() throws Exception {
        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (RAND() AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "}";


        runTests(queryBind);
    }

    //    @Test timezone is not supported in h2
    public void testTZ() throws Exception {

        String queryBind = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n"
                + "PREFIX  ns:  <http://example.org/ns#>\n"
                + "SELECT  ?title (TZ(?year) AS ?w) WHERE \n"
                + "{  ?x ns:price ?p .\n"
                + "   ?x ns:discount ?discount .\n"
                + "   ?x dc:title ?title .\n"
                + "   ?x ns:pubYear ?year .\n"
                + "}";

        checkReturnedValues(queryBind, getTZExpectedValues());

    }

    protected List<String> getTZExpectedValues(){
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        expectedValues.add("\"0.0\"");
        return expectedValues;
    }

    //    @Test see results of datetime with locale
    public void testDatetime() throws Exception {

        String value = "Jan 31 2013 9:32AM";

        DateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mmaa", Locale.CHINA);

        java.util.Date date;
        try {
            date = df.parse(value);
            Timestamp ts = new Timestamp(date.getTime());
            System.out.println(DATA_FACTORY.getConstantLiteral(ts.toString().replace(' ', 'T'), Predicate.COL_TYPE.DATETIME));

        } catch (ParseException pe) {

            throw new RuntimeException(pe);
        }
    }

    private void checkReturnedValues(String query, List<String> expectedValues) throws Exception {

        QuestOWLFactory factory = new QuestOWLFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertiesfile)
                .build();
        QuestOWL reasoner = factory.createReasoner(config);


        // Now we are ready for querying
        OntopOWLConnection conn = reasoner.getConnection();
        OntopOWLStatement st = conn.createStatement();



        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try {
            QuestOWLResultSet rs = st.executeTuple(query);
            while (rs.nextRow()) {
                OWLObject ind1 = rs.getOWLObject("w");
                // log.debug(ind1.toString());
                if (ind1 != null) {
                    returnedValues.add(ind1.toString());
                } else {
                    returnedValues.add(null);
                }
                java.lang.System.out.println(ind1);
                i++;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

    }



}