package pl.uncertainflowshopsolver.flowshop;

import java.util.List;

/**
 * @author Piotr Kubicki, created on 17.04.2016.
 */
public class Task {

    private List<Integer> timeList;

    private Integer originalPosition;

    public Task(List<Integer> timeList, int originalPosition) {
        this.originalPosition = originalPosition;
        this.timeList = timeList;
    }

    public List<Integer> getTimeList() {
        return timeList;
    }

    public Integer getOriginalPosition() {
        return originalPosition;
    }

    public void setTimeOfOperation(int index, Integer timeOfOperation)
    {
        this.timeList.set(index, timeOfOperation);
    }

    public Integer getTimeOfOperation(int index)
    {
        return this.timeList.get(index);
    }
}
