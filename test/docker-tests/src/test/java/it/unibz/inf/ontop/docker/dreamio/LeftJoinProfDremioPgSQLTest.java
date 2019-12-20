package it.unibz.inf.ontop.docker.dreamio;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class LeftJoinProfDremioPgSQLTest extends AbstractLeftJoinProfTest {

    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/redundant_join/dremio/redundant_join_fk_test_pgsql.obda";
    private static final String propertyFileName = "/dremio/redundant_join_fk_test.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFileName, obdaFileName, propertyFileName);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.5", "16.0", "18.875");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return ImmutableList.of("10.5","12.0", "13.0");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "10.5000000000000000", "12.0000000000000000", "13.0000000000000000");
    }

}
