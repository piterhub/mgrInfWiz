package pl.uncertainflowshopsolver;

import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.algo.init.RandomInitializer;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventListener;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.impl.SAConfigurationImpl;

/**
 * @author Piotr Kubicki, created on 22.05.2016.
 */
public class MainConsoleWithMocks {

    /**
     * Use this to run without GUI
     * Change mocks to set configuration
     */
    public static void main(String[] args) {
        SimulatedAnnealing SAAlgorithm = new SimulatedAnnealing();
        SAAlgorithm.setConfigurationProvider(new ConfigurationProviderMock());
        SAAlgorithm.setEventDispatcher(new EventDispatcherMock(null));
        SAAlgorithm.start();
    }


    private static class ConfigurationProviderMock implements ConfigurationProvider {

        @Override
        public SAConfiguration getSAConfiguration() {
            return SAConfigurationImpl.newBuilder()
                    .withDesiredInitialAcceptanceProbability(0.925)
                    .withEpocheLength(10)
                    .withDecayRate(0.995)
                    .withEndTemperature(0.5)
                    .withErrorThreshold(0.01)
                    .withSamplesCardinality(10000)
                    .withMaxNumberOfIterations(1000)
//                .withCutOffEnergyLevel(cutOffEnergyLevelDoubleTextBox.getValue())
//                    .withSolutionInitializerClass(solutionInitializerClass)
                    .withUncertainFlowshop(new FlowShopWithUncertainty())
                    .build();


        }
    }

    private static class EventDispatcherMock extends AlgorithmEventDispatcher {

        public EventDispatcherMock(AlgorithmEventListener eventListener) {
            super(eventListener);
        }

        @Override
        public void dispatchIterationUpdated(int iteration, FlowShopWithUncertainty flowShop) {
            System.out.println("Iteration updated: " + iteration + " makespan: " + flowShop.getUpperBoundOfMinMaxRegretOptimalization());
        }

        @Override
        public void dispatchAlgorithmEnded(EndingReason reason, double elapsedTime) {
            System.out.println("Algorithm ended");
        }

        @Override
        public void dispatchAlgorithmStarted() {
            System.out.println("Algorithm started");
        }

    }

}
