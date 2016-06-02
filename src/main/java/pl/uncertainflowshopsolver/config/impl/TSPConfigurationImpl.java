package pl.uncertainflowshopsolver.config.impl;


import pl.uncertainflowshopsolver.algo.util.WayToGenerateNeighborhoodEnum;
import pl.uncertainflowshopsolver.config.TSPConfiguration;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public class TSPConfigurationImpl implements TSPConfiguration{

    private int sizeOfNeighborhood;
    private int lengthOfTabuList;
    private int iterationsWithoutImprovementAsAdditionalAspirationCriterion;
    private int maxIterationsWithoutImprovementForDiversificationPurpose;
    private int maxIterationsAsStopCriterion;
    private WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum;
    private FlowShopWithUncertainty uncertainFlowShop;

    private TSPConfigurationImpl(int sizeOfNeighborhood, int lengthOfTabuList, int iterationsWithoutImprovementAsAdditionalAspirationCriterion, int maxIterationsWithoutImprovementForDiversificationPurpose, int maxIterationsAsStopCriterion, WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum, FlowShopWithUncertainty uncertainFlowShop) {
        this.sizeOfNeighborhood = sizeOfNeighborhood;
        this.lengthOfTabuList = lengthOfTabuList;
        this.iterationsWithoutImprovementAsAdditionalAspirationCriterion = iterationsWithoutImprovementAsAdditionalAspirationCriterion;
        this.maxIterationsWithoutImprovementForDiversificationPurpose = maxIterationsWithoutImprovementForDiversificationPurpose;
        this.maxIterationsAsStopCriterion = maxIterationsAsStopCriterion;
        this.wayToGenerateNeighborhoodEnum = wayToGenerateNeighborhoodEnum;
        this.uncertainFlowShop = uncertainFlowShop;
    }

    private TSPConfigurationImpl(){}

    public static TSPConfigurationImplBuilder newBuilder() {
        return new TSPConfigurationImplBuilder();
    }

    @Override
    public int getSizeOfNeighborhood() {
        return sizeOfNeighborhood;
    }

    @Override
    public int getLengthOfTabuList() {
        return lengthOfTabuList;
    }

    @Override
    public int getIterationsWithoutImprovementAsAdditionalAspirationCriterion() {
        return iterationsWithoutImprovementAsAdditionalAspirationCriterion;
    }

    @Override
    public int getMaxIterationsWithoutImprovementForDiversificationPurpose() {
        return maxIterationsWithoutImprovementForDiversificationPurpose;
    }

    @Override
    public int getMaxIterationsAsStopCriterion() {
        return maxIterationsAsStopCriterion;
    }

    public WayToGenerateNeighborhoodEnum getWayToGenerateNeighborhoodEnum() {
        return wayToGenerateNeighborhoodEnum;
    }

    @Override
    public FlowShopWithUncertainty getUncertainFlowShop() {
        return uncertainFlowShop;
    }


    public static class TSPConfigurationImplBuilder {

        TSPConfigurationImpl mTSPConfigurationImpl;

        public TSPConfigurationImplBuilder() {
            this.mTSPConfigurationImpl = new TSPConfigurationImpl();
        }

        public TSPConfigurationImplBuilder withSizeOfNeighborhood(int pSizeOfNeighborhood) {
            mTSPConfigurationImpl.sizeOfNeighborhood = pSizeOfNeighborhood;
            return this;
        }

        public TSPConfigurationImplBuilder withLengthOfTabuList(int pLengthOfTabuList) {
            mTSPConfigurationImpl.lengthOfTabuList = pLengthOfTabuList;
            return this;
        }

        public TSPConfigurationImplBuilder withIterationsWithoutImprovementAsAdditionalAspirationCriterion(int pIterationsWithoutImprovementAsAdditionalAspirationCriterion) {
            mTSPConfigurationImpl.iterationsWithoutImprovementAsAdditionalAspirationCriterion = pIterationsWithoutImprovementAsAdditionalAspirationCriterion;
            return this;
        }

        public TSPConfigurationImplBuilder withMaxIterationsWithoutImprovementForDiversificationPurpose(int pMaxIterationsWithoutImprovementForDiversificationPurpose) {
            mTSPConfigurationImpl.maxIterationsWithoutImprovementForDiversificationPurpose = pMaxIterationsWithoutImprovementForDiversificationPurpose;
            return this;
        }

        public TSPConfigurationImplBuilder withMaxIterationsAsStopCriterion(int pMaxIterationsAsStopCriterion) {
            mTSPConfigurationImpl.maxIterationsAsStopCriterion = pMaxIterationsAsStopCriterion;
            return this;
        }

        public TSPConfigurationImplBuilder withWayToGenerateNeighborhood(WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum) {
            mTSPConfigurationImpl.wayToGenerateNeighborhoodEnum = wayToGenerateNeighborhoodEnum;
            return this;
        }

        public TSPConfigurationImplBuilder withUncertainFlowshop(FlowShopWithUncertainty uncertainFlowShop) {
            mTSPConfigurationImpl.uncertainFlowShop = uncertainFlowShop;
            return this;
        }

        public TSPConfigurationImpl build() {
            return new TSPConfigurationImpl(
            );
        }
    }
}
