package pl.uncertainflowshopsolver.config.impl;


import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public class SAConfigurationImplBuilder {
    private int eliteSolutionsNumber;
    private int beesPerEliteSolution;
    private int bestSolutionsNumber;
    private int beesPerBestSolution;
    private int scoutBeesNumber;
    private int maxIterationsWithoutImprovement;
    private int maxNumberOfIterations;
    private int maxTimeOfExecuting;
    private int minFitness;
    private Class<? extends SolutionInitializer> solutionInitializerClass;
    private FlowShopWithUncertainty flowshop;

    public SAConfigurationImplBuilder withEliteSolutionsNumber(int eliteSolutionsNumber) {
        this.eliteSolutionsNumber = eliteSolutionsNumber;
        return this;
    }

    public SAConfigurationImplBuilder withBeesPerEliteSolution(int beesPerEliteSolution) {
        this.beesPerEliteSolution = beesPerEliteSolution;
        return this;
    }

    public SAConfigurationImplBuilder withBestSolutionsNumber(int bestSolutionsNumber) {
        this.bestSolutionsNumber = bestSolutionsNumber;
        return this;
    }

    public SAConfigurationImplBuilder withBeesPerBestSolution(int beesPerBestSolution) {
        this.beesPerBestSolution = beesPerBestSolution;
        return this;
    }

    public SAConfigurationImplBuilder withScoutBeesNumber(int scoutBeesNumber) {
        this.scoutBeesNumber = scoutBeesNumber;
        return this;
    }

    public SAConfigurationImplBuilder withMaxIterationsWithoutImprovement(int maxIterationsWithoutImprovement) {
        this.maxIterationsWithoutImprovement = maxIterationsWithoutImprovement;
        return this;
    }

    public SAConfigurationImplBuilder withMaxNumberOfIterations(int maxNumberOfIterations) {
        this.maxNumberOfIterations = maxNumberOfIterations;
        return this;
    }

    public SAConfigurationImplBuilder withMaxTimeOfExecuting(int maxTimeOfExecuting) {
        this.maxTimeOfExecuting = maxTimeOfExecuting;
        return this;
    }

    public SAConfigurationImplBuilder withMinFitness(int minFitness) {
        this.minFitness = minFitness;
        return this;
    }

    public SAConfigurationImplBuilder withSolutionInitializerClass(Class<? extends SolutionInitializer> solutionInitializerClass) {
        this.solutionInitializerClass = solutionInitializerClass;
        return this;
    }

    public SAConfigurationImplBuilder withFlowshop(FlowShopWithUncertainty flowshop) {
        this.flowshop = flowshop;
        return this;
    }

    public SAConfigurationImpl build() {
        return new SAConfigurationImpl(eliteSolutionsNumber, beesPerEliteSolution, bestSolutionsNumber, beesPerBestSolution, scoutBeesNumber, maxNumberOfIterations, maxTimeOfExecuting, minFitness, solutionInitializerClass, flowshop, maxIterationsWithoutImprovement);
    }
}