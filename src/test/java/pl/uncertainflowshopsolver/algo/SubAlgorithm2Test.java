package pl.uncertainflowshopsolver.algo;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import pl.uncertainflowshopsolver.flowshop.FlowShop;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.testdata.FlowshopParser;
import pl.uncertainflowshopsolver.testdata.UncertainFlowShopParser;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Piotr Kubicki, created on 24.05.2016.
 */
public class SubAlgorithm2Test {

    private SubAlgorithm2 mTestedObject;
    private FlowShopWithUncertainty uncertainFlowShop_PATH1;

    private static final String PATH1 = "resources/2_[n50, m3, K100, C50]_FlowShopWithUncertainty.txt";
    final int expectedUBResultForPath1 = 238;
    final int expectedLBResultForPath1 = 238;

    @Before
    public void setUp() throws Exception
    {
        mTestedObject = new SubAlgorithm2();
        uncertainFlowShop_PATH1 = UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH1);
    }

    @Test
    public void testSolve() throws Exception {
        final Object[] results = mTestedObject.solveGreedy(uncertainFlowShop_PATH1, null, false);
        int result_LB = (int) results[0];
        Assertions.assertThat(result_LB).isEqualTo(expectedLBResultForPath1);
        int result_UB = (int) results[1];
        Assertions.assertThat(result_UB).isEqualTo(expectedUBResultForPath1);
    }

}