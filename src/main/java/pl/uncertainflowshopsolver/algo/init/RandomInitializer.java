package pl.uncertainflowshopsolver.algo.init;

import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * returns randomly shuffled tasks in flow shop
 */
public class RandomInitializer implements SolutionInitializer{

    @Override
    public FlowShopWithUncertainty initialize(FlowShopWithUncertainty flowShop) {
        List<TaskWithUncertainty> tasks = new ArrayList<>(flowShop.getTasks());
        Collections.shuffle(tasks);

        return new FlowShopWithUncertainty(tasks);
    }
}
