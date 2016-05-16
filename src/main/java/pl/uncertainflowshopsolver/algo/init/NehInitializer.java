package pl.uncertainflowshopsolver.algo.init;

import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NehInitializer implements SolutionInitializer {

    @Override
    public FlowShopWithUncertainty initialize(FlowShopWithUncertainty flowShop) {   //TODO
        List<TaskWithUncertainty> tasks = new ArrayList<>(flowShop.getTasks());
        Collections.shuffle(tasks);

        return new FlowShopWithUncertainty(tasks);
    }
}
