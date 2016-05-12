package pl.uncertainflowshopsolver.flowshop;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Piotr Kubicki, created on 24.04.2016.
 */
public interface TaskInterface {

    List<Integer> getTimeList();
    Integer getOriginalPosition();
}
