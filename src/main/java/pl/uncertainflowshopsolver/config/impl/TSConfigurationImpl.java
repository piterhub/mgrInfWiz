package pl.uncertainflowshopsolver.config.impl;


import pl.uncertainflowshopsolver.algo.util.WayToGenerateNeighborhoodEnum;
import pl.uncertainflowshopsolver.config.TSConfiguration;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

public class TSConfigurationImpl implements TSConfiguration {

    private int sizeOfNeighborhood;
    private int lengthOfTabuList;
    private int iterationsWithoutImprovementAsAdditionalAspirationCriterion;
    private int maxIterationsWithoutImprovementForDiversificationPurpose;
    private int maxIterationsAsStopCriterion;
    private int maxIterationsWithoutImprovementAsStopCriterion;


    private WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum;
    private FlowShopWithUncertainty uncertainFlowShop;

    private TSConfigurationImpl(){}

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
    public int getMaxIterationsWithoutImprovementAsStopCriterion() {
        return maxIterationsWithoutImprovementAsStopCriterion;
    }

    @Override
    public FlowShopWithUncertainty getUncertainFlowShop() {
        return uncertainFlowShop;
    }


    public static class TSPConfigurationImplBuilder {

        TSConfigurationImpl mTSPConfigurationImpl;

        public TSPConfigurationImplBuilder() {
            this.mTSPConfigurationImpl = new TSConfigurationImpl();
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

        public TSPConfigurationImplBuilder withMaxIterationsWithoutImprovementAsStopCriterion(int pMaxIterationsWithoutImprovementAsStopCriterion) {
            mTSPConfigurationImpl.maxIterationsWithoutImprovementAsStopCriterion = pMaxIterationsWithoutImprovementAsStopCriterion;
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

        public TSConfigurationImpl build() {
            if(mTSPConfigurationImpl.maxIterationsWithoutImprovementAsStopCriterion == 0) {
                mTSPConfigurationImpl.maxIterationsWithoutImprovementAsStopCriterion = mTSPConfigurationImpl.maxIterationsAsStopCriterion;
            }
            return mTSPConfigurationImpl;
        }
    }
}
