package pl.uncertainflowshopsolver.config;

import pl.uncertainflowshopsolver.algo.util.WayToGenerateNeighborhoodEnum;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public interface TSPConfiguration {

    /**
     * @return N - number of probes of choose the best neighbor, when N = taskCount, then it's not probabilistic tabu search
     */
    int getSizeOfNeighborhood();

    /**
     * @return
     */
    int getLengthOfTabuList();

    /**
     * @return iterations without improvement as additional (optional) aspiration criterion
     */
    int getIterationsWithoutImprovementAsAdditionalAspirationCriterion();

    /**
     * @return Jesli przez 2*N przejsc nie zmieniono najlepszego rozwiazania losujemy 10x nowe rozwiazania i wybieramy z niego najlepsze lub nie stosujemy dywersyfikacji
     */
    int getMaxIterationsWithoutImprovementForDiversificationPurpose();

    /**
     * @return max iterations of work
     */
    int getMaxIterationsAsStopCriterion();

    WayToGenerateNeighborhoodEnum getWayToGenerateNeighborhoodEnum();
    FlowShopWithUncertainty getUncertainFlowShop();
}
