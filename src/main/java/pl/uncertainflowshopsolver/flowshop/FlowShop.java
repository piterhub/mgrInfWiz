package pl.uncertainflowshopsolver.flowshop;

import java.util.*;

/**
 * @author Piotr Kubicki, created on 17.04.2016.
 */
public class FlowShop implements Cloneable{

    private List<Task> taskList;

    public int getMachineCount() {
        if (this.getTaskList().isEmpty())
            return 0;
        return this.getTaskList().get(0).getTimeList().size();
    }

//    protected FlowShop(){}
    public FlowShop()
    {
        taskList = new ArrayList<>(Arrays.asList(
                new Task(Arrays.asList(77d,11d,82d),1),
                new Task(Arrays.asList(34d,92d, 8d),2),
                new Task(Arrays.asList(88d,36d,30d),3),
                new Task(Arrays.asList( 1d,98d, 9d),4)
        ));
    }

    public FlowShop(List<Task> taskList) {
        this.taskList = taskList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public Integer getTaskCount() {
        return taskList.size();
    }

    public void addTask(Task task)
    {
        this.taskList.add(task);
    }

    public Task getTask(int index)
    {
        return this.taskList.get(index);
    }

    public List<Integer> getTaskOrder()
    {
        List<Integer> taskOrder = new ArrayList<>();
        for (Task task : taskList) {
            taskOrder.add(task.getOriginalPosition());
        }
        return  taskOrder;
    }

    /**
     * Sort task by sum of its time on all machines.
     * @return new FlowShop object with this state.
     */
    public FlowShop sortDescending() {

        List<Task> listToSort = new ArrayList<Task>(taskList);

        Collections.sort(listToSort, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                double sum1 = 0, sum2 = 0;
                for (int i = 0; i<o1.getTimeList().size(); i++) {
                    sum1 += o1.getTimeList().get(i);
                    sum2 += o2.getTimeList().get(i);
                }

                if (sum1<sum2) {
                    return 1;
                } else if (sum1==sum2) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        return new FlowShop(listToSort);
    }

    /**
     * Puts element from old position to new position.
     * Elements on right have now index + 1
     * @param oldPosition int, old position in list
     * @param newPosition int, new position in list
     * @return new FlowShop state
     */
    public FlowShop move(int oldPosition, int newPosition) {
        List<Task> swapped = new ArrayList<Task>(taskList);
        Task task = swapped.get(oldPosition);
        swapped.remove(task);
        swapped.add(newPosition, task);
        return new FlowShop(swapped);
    }

    public FlowShop moveInPlace(int oldPosition, int newPosition) {
        Task task1 = taskList.get(oldPosition);
        Task task2 = taskList.get(newPosition);
        taskList.set(oldPosition, task2);
        taskList.set(newPosition, task1);
        return this;
    }

    /**
     * Returns end time of all tasks
     * @return time that passes between start and end of all tasks
     */
    public Double makeSpan() {
        return makeSpan(taskList.size()-1, false);
    }

    /**
     * Returns end time of specified task
     * @param taskNr int, returns time between start and task with this number
     * @return time that passes between start and end of defined task
     */
    public Double makeSpan(int taskNr) {
        return makeSpan(taskNr, false);
    }

    /**
     * Returns end time of specified task
     * @param printDebug boolean, true if there should be matrix result on console
     * @param taskNr int, returns time between start and task with this number
     * @return time that passes between start and end of defined task
     */
    public Double makeSpan(int taskNr, boolean printDebug) {
        int taskCount = taskNr+1;
        int machineCount = getMachineCount();
        double[][] matrix = new double[taskCount][machineCount];

        for (int t = 0; t<taskCount; t++) {
            Task task = taskList.get(t);
            for (int m=0; m<machineCount; m++) {
                double sum = 0;
                sum += task.getTimeList().get(m);
                if (t>0) { // Rows 1..n-1
                    if (m==0) {
                        sum += matrix[t-1][m];
                    } else {
                        sum += matrix[t - 1][m] > matrix[t][m - 1] ? matrix[t - 1][m] : matrix[t][m - 1];
                    }
                } else { // Row 0
                    if (m>0) {
                        sum  += matrix[t][m - 1];
                    }
                }
                matrix[t][m] = sum;
            }
        }

        // Print (for debug only)
        if (printDebug) {
            for (int t = 0; t<taskCount; t++) {
                System.out.println();
                for (int m = 0; m<machineCount; m++) {
                    System.out.print(matrix[t][m] + " ");
                }
            }
        }

        return matrix[taskCount-1][machineCount-1];
    }

    @Override
    public String toString() {
        String output = "";
        for (Task task : taskList) {
            output+=("Task" + task.getOriginalPosition() + ":");
            for (Double operation : task.getTimeList()) {
                output+=(" " + operation);
            }
            output+="\n";
        }
        return output;
    }

    @Override
    public FlowShop clone() {
        List<Task> tasks = new ArrayList<Task>(taskList);
        return new FlowShop(tasks);
    }

}
