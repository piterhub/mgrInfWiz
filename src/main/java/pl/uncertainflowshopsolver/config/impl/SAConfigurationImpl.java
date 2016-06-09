package pl.uncertainflowshopsolver.config.impl;


import pl.uncertainflowshopsolver.algo.util.WayToGenerateNeighborhoodEnum;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public class SAConfigurationImpl implements SAConfiguration {

    private Double desiredInitialAcceptanceProbability;
    private int epocheLength;
    private Double decayRate;
    private Double endTemperature;
    private Double errorThreshold;
    private int samplesCardinality;
    private int maxNumberOfIterations;
    private int maxIterationsWithoutImprovementForDiversificationPurpose;
//    private int maxIterationsWithoutImprovement;
    private Double cutOffEnergyLevel;
    private WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum;
    private FlowShopWithUncertainty uncertainFlowShop;
    private int maxIterationsWithoutImprovementAsStopCriterion;

    SAConfigurationImpl(Double desiredInitialAcceptanceProbability, int epocheLength, Double decayRate, Double endTemperature, Double errorThreshold, int samplesCardinality, int maxNumberOfIterations, /**int maxIterationsWithoutImprovement,*/Double cutOffEnergyLevel, WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum, FlowShopWithUncertainty uncertainFlowShop, int maxIterationsWithoutImprovementForDiversificationPurpose, int maxIterationsWithoutImprovementAsStopCriterion) {
        this.desiredInitialAcceptanceProbability = desiredInitialAcceptanceProbability;
        this.epocheLength = epocheLength;
        this.decayRate = decayRate;
        this.endTemperature = endTemperature;
        this.errorThreshold = errorThreshold;
        this.samplesCardinality = samplesCardinality;
        this.maxNumberOfIterations = maxNumberOfIterations;
//        this.maxIterationsWithoutImprovement = maxIterationsWithoutImprovement;
        this.cutOffEnergyLevel = cutOffEnergyLevel;
        this.wayToGenerateNeighborhoodEnum = wayToGenerateNeighborhoodEnum;
        this.uncertainFlowShop = uncertainFlowShop;
        this.maxIterationsWithoutImprovementForDiversificationPurpose = maxIterationsWithoutImprovementForDiversificationPurpose;
        this.maxIterationsWithoutImprovementAsStopCriterion = maxIterationsWithoutImprovementAsStopCriterion;
        System.out.println("\nDECAY RATE IS NOW: " + this.decayRate + "\n");
        System.out.println("\nP_0 IS NOW: " + this.desiredInitialAcceptanceProbability + "\n");
    }

    public static SAConfigurationImplBuilder newBuilder() {
        return new SAConfigurationImplBuilder();
    }

    @Override
    public Double getDesiredInitialAcceptanceProbability() {
        return desiredInitialAcceptanceProbability;
    }

    @Override
    public int getEpocheLength() {
        return epocheLength;
    }

    @Override
    public Double getDecayRate() {
        return decayRate;
    }

    @Override
    public Double getEndTemperature() {
        return endTemperature;
    }

    @Override
    public Double getErrorThreshold() {
        return errorThreshold;
    }

    @Override
    public int getSamplesCardinality() {
        return samplesCardinality;
    }

    @Override
    public int getMaxNumberOfIterations() {
        return maxNumberOfIterations;
    }

//    @Override
//    public int getMaxIterationsWithoutImprovement() {
//        /**return maxIterationsWithoutImprovement;*/
//        throw new UnsupportedOperationException("SAConfigurationImpl#getMaxIterationsWithoutImprovement");
//    }

    @Override
    public Double getCutOffEnergyLevel() {
        return cutOffEnergyLevel;
    }

    public WayToGenerateNeighborhoodEnum getWayToGenerateNeighborhoodEnum() {
        return wayToGenerateNeighborhoodEnum;
    }

    @Override
    public FlowShopWithUncertainty getUncertainFlowShop() {
        return uncertainFlowShop;
    }

    @Override
    public int getMaxIterationsWithoutImprovementForDiversificationPurpose()
    {
        return maxIterationsWithoutImprovementForDiversificationPurpose;
    }

    @Override
    public int getMaxIterationsWithoutImprovementAsStopCriterion() {
        return maxIterationsWithoutImprovementAsStopCriterion;
    }
}
