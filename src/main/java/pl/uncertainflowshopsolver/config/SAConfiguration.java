package pl.uncertainflowshopsolver.config;

import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public interface SAConfiguration {

    int getMaxTimeOfExecuting();
    int getMaxNumberOfIterations();
    int getMaxIterationsWithoutImprovement();

    FlowShopWithUncertainty getFlowShop();
    Class<? extends SolutionInitializer> getSolutionInitializerClass();
}
