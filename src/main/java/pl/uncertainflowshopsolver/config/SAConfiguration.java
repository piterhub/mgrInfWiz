package pl.uncertainflowshopsolver.config;

import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public interface SAConfiguration {

    /**
     * @return P_0 - probability, with which we want accept worse move at the beginning of algorithm
     */
    Double getDesiredInitialAcceptanceProbability();

    /**
     * @return L - number of iterations made with the same temperature
     */
    int getEpocheLength();

    /**
     * @return alpha - coefficient, which is used in cooling scheme: T_n+1 = alpha * T_n
     */
    Double getDecayRate();

    /**
     * @return T_K
     */
    Double getEndTemperature();

    /**
     * @return epsilon - how close within the desired probability we should get
     *   before stopping. A lower value will mean more accuracy, but longer
     *   runtime.
     */
    Double getErrorThreshold();

    /**
     * @return |S| where S is a set (abstractly thought) of samples result z_4 received with (hopefully) differents inputs (permutations)
     */
    int getSamplesCardinality();

    int getMaxNumberOfIterations();

//    int getMaxIterationsWithoutImprovement();

    /**
     * @return E_min, when we reach it, then we stop the algorithm
     */
    Double getCutOffEnergyLevel();

    Class<? extends SolutionInitializer> getSolutionInitializerClass();
    FlowShopWithUncertainty getUncertainFlowShop();
}
