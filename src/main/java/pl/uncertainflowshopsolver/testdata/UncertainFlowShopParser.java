package pl.uncertainflowshopsolver.testdata;

import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * [0] - http://stackoverflow.com/questions/9260126/what-are-the-differences-between-char-literals-n-and-r-in-java
 *
 * @author Piotr Kubicki, created on 03.05.2016.
 */
public class UncertainFlowShopParser {

//    public void readFile(String filePath) throws IOException {
//        FileReader fileReader = new FileReader(filePath);
//        BufferedReader bufferedReader = new BufferedReader(fileReader);
//
//        try {
//            String textLine = bufferedReader.readLine();
//            do {
//                System.out.println(textLine);
//
//                textLine = bufferedReader.readLine();
//            } while (textLine != null);
//        } finally {
//            bufferedReader.close();
//        }
//    }

    public static FlowShopWithUncertainty parseTextToFlowShop(String text) {
        if (validateText(text)) {
            String[] lines = text.split("\\r?\\n"); //Optional r (CR) due to windows usage of file. More [0]

            String[] firstLine = lines[0].split("\\s+");
//            int numberOfJobs = Integer.parseInt(firstLine[0]);
            int numberOfMachines = Integer.parseInt(firstLine[0]);

//            List<TaskWithUncertainty> taskWithUncertaintyList = new ArrayList<>(numberOfJobs);
            List<TaskWithUncertainty> taskWithUncertaintyList = new ArrayList<>();

            int originalPosition = 0;
            for (int i = 1; i < lines.length; i++) {
                List<Integer> lowerTimeList = new ArrayList<>();
                List<Integer> upperTimeList = new ArrayList<>();

                // Regex to scan for 1 or more whitespace characters
                String[] toks = lines[i].split("\\s+");

                for (int j = 1; j < numberOfMachines+1; j++) {
                    String[] timesOnMachine = toks[j].split("\\|");
                    lowerTimeList.add(Integer.parseInt(timesOnMachine[0]));
                    upperTimeList.add(Integer.parseInt(timesOnMachine[1]));
                }
                TaskWithUncertainty uncertainTask = new TaskWithUncertainty(lowerTimeList, upperTimeList, originalPosition);
                taskWithUncertaintyList.add(uncertainTask);
                originalPosition++;
            }

            FlowShopWithUncertainty uncertainFlowShop = new FlowShopWithUncertainty(taskWithUncertaintyList);
            return uncertainFlowShop;
        } else {
            return null;
        }
    }

    public static boolean validateText(String text) {
        return true;    //TODO PKU not important at this moment
    }

    public static FlowShopWithUncertainty parseFileToFlowShopWithUncertainty(String filePath) throws IOException {

        List<TaskWithUncertainty> taskWithUncertaintyList = new ArrayList<>();

        FileInputStream fin = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fin = new FileInputStream(filePath);
            isr = new InputStreamReader(fin, "UTF-8");
            br = new BufferedReader(isr);
            String line = br.readLine();
            if(line == null)
                return null;
            Integer m = Integer.parseInt(line);
            line = br.readLine();
            int originalPosition = 0;
            while (line != null) {
                List<Integer> lowerTimeList = new ArrayList<>();
                List<Integer> upperTimeList = new ArrayList<>();

                // Regex to scan for 1 or more whitespace characters
                String[] toks = line.split("\\s+");

                for (int i = 1; i < m+1; i++) {
                    String[] timesOnMachine = toks[i].split("\\|");
                    lowerTimeList.add(Integer.parseInt(timesOnMachine[0]));
                    upperTimeList.add(Integer.parseInt(timesOnMachine[1]));
                }
                TaskWithUncertainty uncertainTask = new TaskWithUncertainty(lowerTimeList, upperTimeList, originalPosition);
                taskWithUncertaintyList.add(uncertainTask);
                originalPosition++;
                line = br.readLine();
            }
            FlowShopWithUncertainty uncertainFlowShop = new FlowShopWithUncertainty(taskWithUncertaintyList);
            return uncertainFlowShop;
//            System.out.println(uncertainFlowShop.toString());
        }
        catch (IOException e)
        {
            return null;
        }
        finally {
            if (br != null)  { br.close();  }
            if (isr != null) { isr.close(); }
            if (fin != null) { fin.close(); }
        }
    }

    private static List<Integer> parseLine(String line) {
//        String[] times = line.split("\\s+");
        String[] times = line.split("\\|");

        List<Integer> integerTimes = new ArrayList<Integer>();
        for (String time : times) {
            integerTimes.add(Integer.valueOf(time));
        }

        return integerTimes;
    }

    public static void main(String[] args)
    {
//        try {
//            final FlowShopWithUncertainty flowShopWithUncertainty = flowShopParser.parseFileToFlowShopWithUncertainty("C:/Users/pkubicki/Documents/TestFiles/11_uncertainFlowShop_05.10-14.38-13.858.txt");
//            System.out.println(flowShopWithUncertainty.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        String text =
                "3\n" +
                        "Task0: 93|115 46|60 100|123\n" +
                        "Task1: 63|104 58|60 64|72\n" +
                        "Task2: 64|111 75|113 55|76\n" +
                        "Task3: 15|20 18|51 45|80\n" +
                        "Task4: 98|117 13|33 73|123\n" +
                        "Task5: 30|45 15|33 46|80\n" +
                        "Task6: 9|19 52|68 33|52\n" +
                        "Task7: 88|109 91|108 29|75\n" +
                        "Task8: 36|76 44|73 70|118\n" +
                        "Task9: 91|131 38|50 7|39\n" +
                        "Task10: 0|15 74|122 29|35";

        final FlowShopWithUncertainty flowShopWithUncertainty = UncertainFlowShopParser.parseTextToFlowShop(text);
        System.out.println(flowShopWithUncertainty.toString());
    }
}
