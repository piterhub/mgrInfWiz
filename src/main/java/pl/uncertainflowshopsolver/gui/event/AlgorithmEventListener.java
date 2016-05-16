package pl.uncertainflowshopsolver.gui.event;


import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

import java.util.SortedMap;

public interface AlgorithmEventListener {
    public void onManyIterationBatchUpdated(SortedMap<Integer, FlowShopWithUncertainty> iterations);
    public void onIterationUpdated(int iteration, FlowShopWithUncertainty bee);
    public void onAlgorithmStarted();
    public void onAlgorithmEnded(AlgorithmEventDispatcher.EndingReason reason);
}
