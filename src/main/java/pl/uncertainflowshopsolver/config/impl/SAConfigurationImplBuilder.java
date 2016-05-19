package pl.uncertainflowshopsolver.config.impl;


import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public class SAConfigurationImplBuilder {
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

    public SAConfigurationImplBuilder withDesiredInitialAcceptanceProbability(Double desiredInitialAcceptanceProbability) {
        this.desiredInitialAcceptanceProbability = desiredInitialAcceptanceProbability;
        return this;
    }

    public SAConfigurationImplBuilder withEpocheLength(int epocheLength) {
        this.epocheLength = epocheLength;
        return this;
    }

    public SAConfigurationImplBuilder withDecayRate(Double decayRate) {
        this.decayRate = decayRate;
        return this;
    }

    public SAConfigurationImplBuilder withEndTemperature(Double endTemperature) {
        this.endTemperature = endTemperature;
        return this;
    }

    public SAConfigurationImplBuilder withErrorThreshold(Double errorThreshold) {
        this.errorThreshold = errorThreshold;
        return this;
    }

    public SAConfigurationImplBuilder withSamplesCardinality(int samplesCardinality) {
        this.samplesCardinality = samplesCardinality;
        return this;
    }

    public SAConfigurationImplBuilder withMaxNumberOfIterations(int maxNumberOfIterations) {
        this.maxNumberOfIterations = maxNumberOfIterations;
        return this;
    }

    public SAConfigurationImplBuilder withMaxIterationsWithoutImprovement(int maxIterationsWithoutImprovement) {
        this.maxIterationsWithoutImprovement = maxIterationsWithoutImprovement;
        return this;
    }

    public SAConfigurationImplBuilder withCutOffEnergyLevel(Double cutOffEnergyLevel) {
        this.cutOffEnergyLevel = cutOffEnergyLevel;
        return this;
    }

    public SAConfigurationImplBuilder withSolutionInitializerClass(Class<? extends SolutionInitializer> solutionInitializerClass) {
        this.solutionInitializerClass = solutionInitializerClass;
        return this;
    }

    public SAConfigurationImplBuilder withUncertainFlowshop(FlowShopWithUncertainty uncertainFlowShop) {
        this.uncertainFlowShop = uncertainFlowShop;
        return this;
    }

    public SAConfigurationImpl build() {
        return new SAConfigurationImpl(
                desiredInitialAcceptanceProbability,
                epocheLength,
                decayRate,
                endTemperature,
                errorThreshold,
                samplesCardinality,
                maxNumberOfIterations,
                maxIterationsWithoutImprovement,
                cutOffEnergyLevel,
                solutionInitializerClass,
                uncertainFlowShop
                );
    }
}