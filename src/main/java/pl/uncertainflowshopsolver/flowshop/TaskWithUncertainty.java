package pl.uncertainflowshopsolver.flowshop;

import java.util.List;

/**
 * @author Piotr Kubicki, created on 18.04.2016.
 */
public class TaskWithUncertainty{

    private List<Integer> lowerTimeList;
    private List<Integer> upperTimeList;
    private Integer originalPosition;

    public TaskWithUncertainty(List<Integer> lowerTimeList, List<Integer> upperTimeList, int originalPosition) {
        this.lowerTimeList = lowerTimeList;
        this.upperTimeList = upperTimeList;
        this.originalPosition = originalPosition;
    }

    public List<Integer> getLowerTimeList() {
        return lowerTimeList;
    }

    public void setLowerTimeList(List<Integer> lowerTimeList) {
        this.lowerTimeList = lowerTimeList;
    }

    public List<Integer> getUpperTimeList() {
        return upperTimeList;
    }

    public void setUpperTimeList(List<Integer> upperTimeList) {
        this.upperTimeList = upperTimeList;
    }

    public Integer getOriginalPosition() {
        return originalPosition;
    }

    public void setOriginalPosition(Integer originalPosition) {
        this.originalPosition = originalPosition;
    }

}
