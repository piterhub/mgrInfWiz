package pl.uncertainflowshopsolver.gui.event;

import javafx.application.Platform;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

/**
 * Class used to dispatch events to GUI. Can't execute on GUI thread directly.
 * Must use Platform.runLater
 */
public class AlgorithmEventDispatcher {
    public static final int EVENT_BATCH_SIZE = 100;
    private SortedMap<Integer, FlowShopWithUncertainty> iterationCache = new TreeMap<>();
    private AlgorithmEventListener eventListener;

    public AlgorithmEventDispatcher(AlgorithmEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void dispatchIterationUpdated(final int iteration, final FlowShopWithUncertainty flowShop) {//TODO PKU change from FlowShop to SubA2
        iterationCache.put(iteration, flowShop);

        if (iterationCache.size() >= EVENT_BATCH_SIZE) {
            final SortedMap<Integer, FlowShopWithUncertainty> copiedMap = new TreeMap<>(iterationCache);

            final CountDownLatch doneLatch = new CountDownLatch(1);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    eventListener.onManyIterationBatchUpdated(copiedMap);
                    doneLatch.countDown();
                }
            });

            try {
                doneLatch.await();//TODO czy chcemy czeka?, a? si? sko?czy eventListener.onManyIterationBatchUpdated na w?tku JavaFX?
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            iterationCache.clear();
        }
    }

    public void dispatchAlgorithmEnded(final EndingReason reason, final double elapsedTime, final FlowShopWithUncertainty flowShopWithUncertainty, final double initialTemperature) {
        final SortedMap<Integer, FlowShopWithUncertainty> copiedMap = new TreeMap<>(iterationCache);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                eventListener.onManyIterationBatchUpdated(copiedMap);
                eventListener.onAlgorithmEnded(reason, elapsedTime, flowShopWithUncertainty, initialTemperature);
            }
        });
        iterationCache.clear();
    }

    /**
     * For TS purpose.
     * @param reason r
     * @param elapsedTime e
     * @param flowShopWithUncertainty f
     */
    public void dispatchAlgorithmEnded(final EndingReason reason, final double elapsedTime, final FlowShopWithUncertainty flowShopWithUncertainty) {
        final SortedMap<Integer, FlowShopWithUncertainty> copiedMap = new TreeMap<>(iterationCache);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                eventListener.onManyIterationBatchUpdated(copiedMap);
                eventListener.onAlgorithmEnded(reason, elapsedTime, flowShopWithUncertainty);
            }
        });
        iterationCache.clear();
    }

    public void dispatchAlgorithmStarted() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                eventListener.onAlgorithmStarted();
            }
        });
    }

    public enum EndingReason {
        CANCELLED, ALL_ITERATIONS, WITHOUT_PROGRESS
    }
}
