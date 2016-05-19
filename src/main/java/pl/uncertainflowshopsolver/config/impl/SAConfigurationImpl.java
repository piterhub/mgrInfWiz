package pl.uncertainflowshopsolver.config.impl;


import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
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
    private int maxIterationsWithoutImprovement;
    private Double cutOffEnergyLevel;
    private Class<? extends SolutionInitializer> solutionInitializerClass;
    private FlowShopWithUncertainty uncertainFlowShop;

    SAConfigurationImpl(Double desiredInitialAcceptanceProbability, int epocheLength, Double decayRate, Double endTemperature, Double errorThreshold, int samplesCardinality, int maxNumberOfIterations, int maxIterationsWithoutImprovement, Double cutOffEnergyLevel, Class<? extends SolutionInitializer> solutionInitializerClass, FlowShopWithUncertainty uncertainFlowShop) {
        this.desiredInitialAcceptanceProbability = desiredInitialAcceptanceProbability;
        this.epocheLength = epocheLength;
        this.decayRate = decayRate;
        this.endTemperature = endTemperature;
        this.errorThreshold = errorThreshold;
        this.samplesCardinality = samplesCardinality;
        this.maxNumberOfIterations = maxNumberOfIterations;
        this.maxIterationsWithoutImprovement = maxIterationsWithoutImprovement;
        this.cutOffEnergyLevel = cutOffEnergyLevel;
        this.solutionInitializerClass = solutionInitializerClass;
        this.uncertainFlowShop = uncertainFlowShop;
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

    @Override
    public int getMaxIterationsWithoutImprovement() {
        return maxIterationsWithoutImprovement;
    }

    @Override
    public Double getCutOffEnergyLevel() {
        return cutOffEnergyLevel;
    }

    @Override
    public Class<? extends SolutionInitializer> getSolutionInitializerClass() {
        return solutionInitializerClass;
    }

    @Override
    public FlowShopWithUncertainty getUncertainFlowShop() {
        return uncertainFlowShop;
    }


}
