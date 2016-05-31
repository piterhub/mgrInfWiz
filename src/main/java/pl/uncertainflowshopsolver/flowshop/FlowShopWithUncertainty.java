package pl.uncertainflowshopsolver.flowshop;

import pl.uncertainflowshopsolver.algo.SubAlgorithm2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Piotr Kubicki, created on 18.04.2016.
 */
public class FlowShopWithUncertainty implements Cloneable {

    public static final String PATH_TO_RESOURCES = "C:/Users/pkubicki/IdeaProjects/mgrInfWiz/resources";    //TODO ?cie?ka b?dzie brana tak jak j? sobie User wybierze guzikiem
    private static final Random random = ThreadLocalRandom.current();
    private List<TaskWithUncertainty> taskWithUncertaintyList;
    private int m;
    private int n;
    private Integer upperBoundOfMinMaxRegretOptimalization;
    private Integer lowerBoundOfMinMaxRegretOptimalization;
    private double elapsedTime;

    public FlowShopWithUncertainty() {
        taskWithUncertaintyList = new ArrayList<>(Arrays.asList(
                new TaskWithUncertainty(Arrays.asList(3, 5, 4), Arrays.asList(4, 6, 7), 1),
                new TaskWithUncertainty(Arrays.asList(1, 7, 2), Arrays.asList(2, 8, 3), 2),
                new TaskWithUncertainty(Arrays.asList(3, 2, 1), Arrays.asList(5, 6, 5), 3)
        ));
    }

    public FlowShopWithUncertainty(List<TaskWithUncertainty> taskWithUncertaintyList) {
        this.taskWithUncertaintyList = taskWithUncertaintyList;
        this.m = getTask(0).getLowerTimeList().size();
        this.n = getTaskCount();
    }

    public static void swapRandomlyTwoTasks(FlowShopWithUncertainty uncertainFlowShop, int taskCount) {
        int random1 = random.nextInt(taskCount);
        int random2 = random.nextInt(taskCount);
        while (random2 == random1) {
            random2 = random.nextInt(taskCount);
        }
        Collections.swap(uncertainFlowShop.getTasks(), random1, random2);
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Integer getUpperBoundOfMinMaxRegretOptimalization() {
        return upperBoundOfMinMaxRegretOptimalization;
    }

    public void setUpperBoundOfMinMaxRegretOptimalization(Integer resultOfMinMaxRegretOptimalization) {
        this.upperBoundOfMinMaxRegretOptimalization = resultOfMinMaxRegretOptimalization;
    }

    public Integer getLowerBoundOfMinMaxRegretOptimalization() {
        return lowerBoundOfMinMaxRegretOptimalization;
    }

    public void setLowerBoundOfMinMaxRegretOptimalization(Integer lowerBoundOfMinMaxRegretOptimalization) {
        this.lowerBoundOfMinMaxRegretOptimalization = lowerBoundOfMinMaxRegretOptimalization;
    }

    public List<TaskWithUncertainty> getTaskWithUncertaintyList() {
        return taskWithUncertaintyList;
    }

    public int getM() {
        return m;
    }

    public int getN() {
        return n;
    }

    public List<TaskWithUncertainty> getTasks() {
        return taskWithUncertaintyList;
    }

    public TaskWithUncertainty getTask(int index) {
        return taskWithUncertaintyList.get(index);
    }

    public Integer getTaskCount() {
        return taskWithUncertaintyList.size();
    }

    //    @Override
//    public Integer getTaskCount() {
//        return taskWithUncertaintyList.size();
//    }
//
//    @Override
//    public FlowShop sortDescending() {
////        return super.sortDescending();
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public FlowShopWithUncertainty move(int oldPosition, int newPosition) {
////        return super.move(oldPosition, newPosition);
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public FlowShop moveInPlace(int oldPosition, int newPosition) {
////        return super.moveInPlace(oldPosition, newPosition);
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Integer makeSpan() {
////        return super.makeSpan();
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Integer makeSpan(int taskNr) {
////        return super.makeSpan(taskNr);
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Integer makeSpan(int taskNr, boolean printDebug) {
////        return super.makeSpan(taskNr, printDebug);
//        throw new UnsupportedOperationException();
//    }
    public void setTaskOrder(List<Integer> taskOrder) {

    }

    /**
     * Saves instance of {@link FlowShopWithUncertainty} to file in fixed destination with given file name,
     * only when no file with the same name already exist (no override file).
     *
     * @param fileName - given file name.
     * @param directoryPath
     */
    public void toFile(String fileName, Path directoryPath) {
        List<String> strings = new ArrayList<>();

        strings.add(String.valueOf(m));
//        strings.add(String.valueOf(n)); TODO PKU remove if really unnecessary

        for (TaskWithUncertainty task : taskWithUncertaintyList) {
            String output = "";
            output += ("Task" + task.getOriginalPosition() + ":");
            for (int i = 0; i < task.getLowerTimeList().size(); i++) {
                output += (" " + task.getLowerTimeList().get(i));
                output += ("|" + task.getUpperTimeList().get(i));
            }
            strings.add(output);
        }
        if(directoryPath == null)
            directoryPath = Paths.get(PATH_TO_RESOURCES);

        createAndBufferedWrite(strings, fileName, directoryPath);
    }

    private void createAndBufferedWrite(List<String> content, String fileName, Path directoryPath) {

        if (canCreateAFileInGivenDirectory(directoryPath)) {
            Path pathToFile = Paths.get(directoryPath.toString(), fileName);
            if (canWriteToGivenFile(pathToFile)) {
                bufferedWrite(content, pathToFile.toAbsolutePath().toString());
            }
            else
            {
                System.err.println("Cannot write to given file: " + pathToFile.toString());
            }
        }
    }

    private boolean canWriteToGivenFile(Path pathToFile) {
        if (!Files.exists(pathToFile)) {
            try {
                Files.createFile(pathToFile);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    private boolean canCreateAFileInGivenDirectory(Path directoryPath) {
        if (!Files.exists(directoryPath)) {
            try {
                Files.createDirectory(directoryPath);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Write a big list of Strings to a file - Use a BufferedWriter
     */
    private void bufferedWrite(List<String> content, String filePath) {

        Path fileP = Paths.get(filePath);
        Charset charset = Charset.forName("utf-8");

        try (BufferedWriter writer = Files.newBufferedWriter(fileP, charset)) {

            for (String line : content) {
                writer.write(line, 0, line.length());
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        String output = "";
        for (TaskWithUncertainty task : taskWithUncertaintyList) {
            output += ("Task" + task.getOriginalPosition() + ":");
            for (int i = 0; i < task.getLowerTimeList().size(); i++) {
                output += (" " + task.getLowerTimeList().get(i));
                output += ("|" + task.getUpperTimeList().get(i));
            }
            output += "\n";
        }
        return output;
    }

    @Override
    public FlowShopWithUncertainty clone() {
        List<TaskWithUncertainty> tasks = new ArrayList<>(taskWithUncertaintyList);
        return new FlowShopWithUncertainty(tasks);
    }

    public FlowShopWithUncertainty getNeighbourAndEvaluateIt(double temperature) {
//        swapRandomlyTwoTasks(this, getTaskCount());
        FlowShopWithUncertainty newFlowShop = new FlowShopWithUncertainty(this.getTasks());
        swapRandomlyTwoTasks(newFlowShop, getTaskCount());
        final Object[] objects = SubAlgorithm2.solveGreedy(this, null, false);
        newFlowShop.setUpperBoundOfMinMaxRegretOptimalization((int) objects[1]);//todo it's absolutely unnecessary. Przecie? robisz to ju? w SA. EDIT: jest potrzebne do initialTempHelpera
        newFlowShop.setLowerBoundOfMinMaxRegretOptimalization((int) objects[0]);
        return newFlowShop;
    }

    public FlowShopWithUncertainty getNeighbour(double temperature) {
        swapRandomlyTwoTasks(this, getTaskCount());
        FlowShopWithUncertainty newFlowShop = new FlowShopWithUncertainty(this.getTasks());
        newFlowShop.setUpperBoundOfMinMaxRegretOptimalization((int)SubAlgorithm2.solveGreedy(this, false, false)[0]);
        return newFlowShop;
    }


//    public FlowShopWithUncertainty getBestNeighbour(int probe) {
//        FlowShopWithUncertainty minimumHelperFlowShop = this.clone();
//        swapRandomlyTwoTasks(this, getTaskCount());
//        FlowShopWithUncertainty newFlowShop = new FlowShopWithUncertainty(this.getTasks());
//        newFlowShop.setUpperBoundOfMinMaxRegretOptimalization((int)SubAlgorithm2.solveGreedy(this, false, false)[0]);
//        return newFlowShop;
//    }
}
