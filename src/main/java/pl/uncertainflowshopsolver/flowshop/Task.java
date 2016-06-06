package pl.uncertainflowshopsolver.flowshop;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Piotr Kubicki, created on 17.04.2016.
 */
public class Task {

    private List<Double> timeList;

    private Integer originalPosition;

    public Task(List<Double> timeList, int originalPosition) {
        this.originalPosition = originalPosition;
        this.timeList = timeList;
    }

    public List<Double> getTimeList() {
        return timeList;
    }

    public Integer getOriginalPosition() {
        return originalPosition;
    }

    public void setTimeOfOperation(int index, Double timeOfOperation)
    {
        this.timeList.set(index, timeOfOperation);
    }

    public Double getTimeOfOperation(int index)
    {
        return this.timeList.get(index);
    }

    public void addOperation(Double timeOfOperation)
    {
        List<Double> newList = new ArrayList<>();
        newList.addAll(this.timeList);
        newList.add(timeOfOperation);
        this.timeList = newList;
    }

    @Override
    public Task clone()
    {
        List<Double> timesOfOperations = this.timeList;
        Integer originalPosition = this.getOriginalPosition();
        return new Task(timesOfOperations, originalPosition);
    }
}
