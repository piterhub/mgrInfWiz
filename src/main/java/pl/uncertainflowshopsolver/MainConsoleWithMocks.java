package pl.uncertainflowshopsolver;

import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventListener;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.impl.SAConfigurationImpl;
import pl.uncertainflowshopsolver.testdata.UncertainFlowShopParser;

import java.io.IOException;
import java.util.Date;

/**
 * @author Piotr Kubicki, created on 22.05.2016.
 */
public class MainConsoleWithMocks {

    private static final String PATH = "resources/11_uncertainFlowShop_05.10-14.38-13.858.txt";

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
            try {
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
                        .withUncertainFlowshop(UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(PATH))
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class EventDispatcherMock extends AlgorithmEventDispatcher {

        public EventDispatcherMock(AlgorithmEventListener eventListener) {
            super(eventListener);
        }

        @Override
        public void dispatchIterationUpdated(int iteration, FlowShopWithUncertainty flowShop) {
            System.out.println("Iteration updated: " + iteration +
                    " \nUpperBound Of MinMaxRegret Optimalization: " + flowShop.getUpperBoundOfMinMaxRegretOptimalization() +
                    " \nLowerBound Of MinMaxRegret Optimalization: " + flowShop.getLowerBoundOfMinMaxRegretOptimalization());
        }

        @Override
        public void dispatchAlgorithmEnded(EndingReason reason, double elapsedTime, FlowShopWithUncertainty flowShopWithUncertainty, double initialTemperature) {
            Date date = new Date();
            System.out.println("Algorithm ended. Elapsed time " + elapsedTime + ". Now is " + date.toString());
        }

        @Override
        public void dispatchAlgorithmStarted() {
            Date date = new Date();
            System.out.println("Algorithm started. Now is " + date.toString());
        }

    }

}
