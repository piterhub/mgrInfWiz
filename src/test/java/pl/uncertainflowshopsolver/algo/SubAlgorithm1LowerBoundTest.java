package pl.uncertainflowshopsolver.algo;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import pl.uncertainflowshopsolver.flowshop.FlowShop;
import pl.uncertainflowshopsolver.testdata.FlowshopParser;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Piotr Kubicki, created on 24.05.2016.
 */
public class SubAlgorithm1LowerBoundTest {

    private SubAlgorithm1LowerBound mTestedObject;
    private FlowShop flowShop_PATH3;
    private FlowShop flowShop_PATH4;

    private static final String PATH = "resources/taillard's instances/ta_1-20x5";
    final String PATH1 = "resources/certainFlowShop_hinduskizjutuba.txt";
    final String PATH2 = "resources/new_2_certainFlowShop.txt";

    final String PATH3 = "resources/Marek Sobolewski_FlowShop.txt";
    final int expectedResultForPath3 = 54; //http://www.ioz.pwr.wroc.pl/pracownicy/kuchta/Marek%20Sobolewski_FlowShop.pdf

    final String PATH4 = "resources/taillard's instances/ta_1-20x5.txt";
    final int expectedResultForPath4 = 1232; //http://www.sciencedirect.com/science/article/pii/S027861251400140X

    @Before
    public void setUp() throws Exception
    {
        mTestedObject = new SubAlgorithm1LowerBound();
        flowShop_PATH3 = FlowshopParser.parseFlowShop(new BufferedReader(new FileReader(PATH3)));
        flowShop_PATH4 = FlowshopParser.parseFlowShop(new BufferedReader(new FileReader(PATH4)));
    }

    @Test
    public void testSolve() throws Exception {
//        Integer result = mTestedObject.solve(flowShop_PATH3, true);
//        Assertions.assertThat(result).isEqualTo(expectedResultForPath3);

        final Integer result = mTestedObject.calculateLowerBoundOfCMax(flowShop_PATH4);
        Assertions.assertThat(result).isEqualTo(expectedResultForPath4);
    }

}