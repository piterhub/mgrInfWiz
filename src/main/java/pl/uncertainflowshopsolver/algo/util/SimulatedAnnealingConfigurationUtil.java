package pl.uncertainflowshopsolver.algo.util;

import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

import java.util.ArrayList;

/**
 * @author Piotr Kubicki, created on 30.04.2016.
 */
public class SimulatedAnnealingConfigurationUtil {

    /**
     * Calculates an appropriate initial temperature based on a desired initial
     * acceptance probability. Uses a set of random states to perform this
     * calculation.
     *
     * desiredProbability: desired initial acceptance probability for positive
     *   (i.e. worse) transitions.
     * Either:
     *   states: set of random states. There must be at least one positive
     *     transition (i.e. state where state.getNeighbour(1.0).getEnergyLevel() -
     *     state.getEnergyLevel() is positive). States which do not meet this
     *     criteria may be included, but will not be used in calculating the
     *     initial temperature.
     *   OR
     *   positiveEnergyDeltas: an array of positive energy deltas
     *     (i.e. state.getNeighbour(1.0).getEnergyLevel() -
     *     state.getEnergyLevel()). Set by default to
     *     (0.01 * desiredProbability).
     * errorThreshold: how close within the desired probability we should get
     *   before stopping. A lower value will mean more accuracy, but longer
     *   runtime.
     *
     * This method only works on states whose neighbor generation is
     * irrespective of the temperature.
     *
     * The algo used by this method is from:
     * Computing the Initial Temperature of Simulated Annealing
     * Walid Ben-Ameur, 2004
     */
    public static double calculateInitialTemperatureFromDesiredProbability(
            double desiredProbability,
            double[] positiveEnergyDeltas,
            double errorThreshold)
    {
        double highestDelta = positiveEnergyDeltas[0];  //java.lang.ArrayIndexOutOfBoundsException: 0
        for (int i = 0; i < positiveEnergyDeltas.length; ++i) {
            if (positiveEnergyDeltas[i] < Double.MIN_NORMAL) {
                throw new IllegalArgumentException(
                        "All energy deltas must be positive");
            }
            highestDelta = Math.max(
                    highestDelta, positiveEnergyDeltas[i]);
        }

        double temperature = highestDelta;
        double probability = estimateAcceptanceProbability(
                positiveEnergyDeltas, temperature);
        while (Math.abs(probability - desiredProbability) > errorThreshold) {
            temperature *=
                    Math.sqrt(Math.log(probability) / Math.log(desiredProbability));
            probability = estimateAcceptanceProbability(
                    positiveEnergyDeltas, temperature);
        }
        return temperature;
    }

    private static double estimateAcceptanceProbability (
            double[] energyDeltas,
            double temperature) {
        double sumProbs = 0.0;
        for (double d : energyDeltas) {
            sumProbs += Math.min(1.0, Math.exp(-d / temperature));
        }
        return sumProbs / energyDeltas.length;
    }

    public static double calculateFromDesiredProbability (
            double desiredProbability,
            Iterable<FlowShopWithUncertainty> states,
            double errorThreshold)
    {
        ArrayList<Double> energyDeltasList = new ArrayList<Double>();
        for (FlowShopWithUncertainty state : states) {
            double energyDelta = (state.getNeighbour(1.0).getUpperBoundOfMinMaxRegretOptimalization() -
                    state.getUpperBoundOfMinMaxRegretOptimalization());
            if (energyDelta > Double.MIN_NORMAL) {
                energyDeltasList.add(energyDelta);
            }
        }
        if (energyDeltasList.isEmpty()) {
            throw new IllegalArgumentException(
                    "states must have at least one positive transition");
        }

        double[] energyDeltas = new double[energyDeltasList.size()];
        for (int i = 0; i < energyDeltasList.size(); ++i) {
            energyDeltas[i] = energyDeltasList.get(i);
        }
        return calculateInitialTemperatureFromDesiredProbability(
                desiredProbability,
                energyDeltas,
                errorThreshold);
    }

    public static void main(String [] args)
    {
        double highestDelta = 2;
        double a = Double.parseDouble(null);
        final double max = Math.max(
                highestDelta, a);
    }
}
