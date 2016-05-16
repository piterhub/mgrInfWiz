package pl.uncertainflowshopsolver.flowshop;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Piotr Kubicki, created on 18.04.2016.
 */
public class FlowShopWithUncertainty implements Cloneable {

    public static final String PATH_TO_RESOURCES = "C:/Users/pkubicki/IdeaProjects/mgrInfWiz/resources";
    private List<TaskWithUncertainty> taskWithUncertaintyList;
    private int m;
    private int n;
    private Integer resultOfMinMaxRegretOptimalization;

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

    public List<TaskWithUncertainty> getTaskWithUncertaintyList() {
        return taskWithUncertaintyList;
    }

    public int getM() {
        return m;
    }

    public int getN() {
        return n;
    }

    public Integer getResultOfMinMaxRegretOptimalization() {
        return resultOfMinMaxRegretOptimalization;
    }

    public void setResultOfMinMaxRegretOptimalization(Integer resultOfMinMaxRegretOptimalization) {
        this.resultOfMinMaxRegretOptimalization = resultOfMinMaxRegretOptimalization;
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
     */
    public void toFile(String fileName) {
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

        createAndBufferedWrite(strings, fileName);
    }

    private void createAndBufferedWrite(List<String> content, String fileName) {

        Path directoryPath = Paths.get(PATH_TO_RESOURCES);

        if (canCreateAFileInGivenDirectory(directoryPath)) {
            if (canWriteToGivenFile(directoryPath, fileName)) {
                Path pathToFile = Paths.get(directoryPath.toString(), fileName);
                bufferedWrite(content, pathToFile.toAbsolutePath().toString());
            }
        }
    }

    private boolean canWriteToGivenFile(Path directoryPath, String fileName) {
        Path pathToFile = Paths.get(directoryPath.toString(), fileName);

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
}
