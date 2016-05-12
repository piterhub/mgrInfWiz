package pl.uncertainflowshopsolver.testdata;

import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.flowshop.TaskWithUncertainty;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Piotr Kubicki, created on 03.05.2016.
 */
public class Parser {

    public void readFile(String filePath) throws IOException {
        FileReader fileReader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        try {
            String textLine = bufferedReader.readLine();
            do {
                System.out.println(textLine);

                textLine = bufferedReader.readLine();
            } while (textLine != null);
        } finally {
            bufferedReader.close();
        }
    }

    public FlowShopWithUncertainty readFileToFlowShopWithUncertainty(String filePath) throws IOException {

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

    public static void main(String[] args)
    {
        Parser parser = new Parser();
        try {
            final FlowShopWithUncertainty flowShopWithUncertainty = parser.readFileToFlowShopWithUncertainty("C:/Users/pkubicki/Documents/TestFiles/testFile.txt");
            System.out.println(flowShopWithUncertainty.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
