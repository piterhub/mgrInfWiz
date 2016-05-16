package pl.uncertainflowshopsolver.algo.init;

import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

/**
 * method initialize takes instance of flow shop problem and returns a solution
 * it can be used to initialize bees with many different solutions for same problem
 */
public interface SolutionInitializer {
    FlowShopWithUncertainty initialize(FlowShopWithUncertainty flowShop);
}
