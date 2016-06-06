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
public class NehAlgorithmTest {

    private NehAlgorithm mTestedObject;
    private FlowShop flowShop_PATH3;
    private FlowShop flowShop_PATH4;

    private static final String PATH = "resources/taillard's instances/ta_1-20x5";
    final String PATH1 = "resources/certainFlowShop_hinduskizjutuba.txt";
    final String PATH2 = "resources/new_2_certainFlowShop.txt";

    final String PATH3 = "resources/Marek Sobolewski_FlowShop.txt";
    final double expectedResultForPath3 = 54d; //http://www.ioz.pwr.wroc.pl/pracownicy/kuchta/Marek%20Sobolewski_FlowShop.pdf

    final String PATH4 = "resources/taillard's instances/ta_1-20x5.txt";
    final double expectedResultForPath4 = 1286d; //http://www.sciencedirect.com/science/article/pii/S027861251400140X

    @Before
    public void setUp() throws Exception
    {
        mTestedObject = new NehAlgorithm();
        flowShop_PATH3 = FlowshopParser.parseFlowShop(new BufferedReader(new FileReader(PATH3)));
        flowShop_PATH4 = FlowshopParser.parseFlowShop(new BufferedReader(new FileReader(PATH4)));
    }

    @Test
    public void testSolve() throws Exception {
        double result = mTestedObject.solve(flowShop_PATH3, true);
        Assertions.assertThat(result).isEqualTo(expectedResultForPath3);

        result = mTestedObject.solve(flowShop_PATH4, true);
        Assertions.assertThat(result).isEqualTo(expectedResultForPath4);
    }

}