package pl.uncertainflowshopsolver.config.impl;


import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public class SAConfigurationImpl implements SAConfiguration {
    private int eliteSolutionsNumber;
    private int beesPerEliteSolution;
    private int bestSolutionsNumber;
    private int beesPerBestSolution;
    private int scoutBeesNumber;

    private int maxNumberOfIterations;
    private int maxIterationsWithoutImprovement;
    private int maxTimeOfExecuting;
    private int minFitness;

    private Class<? extends SolutionInitializer> solutionInitializerClass;
    private FlowShopWithUncertainty flowShop;

    SAConfigurationImpl(int eliteSolutionsNumber, int beesPerEliteSolution, int bestSolutionsNumber, int beesPerBestSolution, int scoutBeesNumber, int maxNumberOfIterations, int maxTimeOfExecuting, int minFitness, Class<? extends SolutionInitializer> solutionInitializerClass, FlowShopWithUncertainty flowShop, int maxIterationsWithoutImprovement) {
        this.eliteSolutionsNumber = eliteSolutionsNumber;
        this.beesPerEliteSolution = beesPerEliteSolution;
        this.bestSolutionsNumber = bestSolutionsNumber;
        this.beesPerBestSolution = beesPerBestSolution;
        this.scoutBeesNumber = scoutBeesNumber;
        this.maxNumberOfIterations = maxNumberOfIterations;
        this.maxTimeOfExecuting = maxTimeOfExecuting;
        this.minFitness = minFitness;
        this.solutionInitializerClass = solutionInitializerClass;
        this.flowShop = flowShop;
        this.maxIterationsWithoutImprovement = maxIterationsWithoutImprovement;
    }

    public static SAConfigurationImplBuilder newBuilder() {
        return new SAConfigurationImplBuilder();
    }




    @Override
    public FlowShopWithUncertainty getFlowShop() {
        return flowShop;
    }

    @Override
    public Class<? extends SolutionInitializer> getSolutionInitializerClass() {
        return solutionInitializerClass;
    }

    @Override
    public int getMaxTimeOfExecuting() {
        return 0;
    }

    @Override
    public int getMaxNumberOfIterations() {
        return 0;
    }

    @Override
    public int getMaxIterationsWithoutImprovement() {
        return maxIterationsWithoutImprovement;
    }

}
