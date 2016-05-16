package pl.uncertainflowshopsolver;

import pl.uncertainflowshopsolver.algo.MIH;
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.testdata.InstanceGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainConsole {

    public static final int HOW_MANY_RANDOM_INSTANCES = 50;
    public static final int REPETITION_FOR_SIMULATED_ANNEALING = 5;
    public static final String PATH_TO_RESOURCES = "C:/Users/pkubicki/IdeaProjects/mgrInfWiz/resources";

    public static void main(String[] args) throws FileNotFoundException {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd-HH.mm-ss.SSS");
        final String timestamp = simpleDateFormat.format(new Date());

        int n = 7;

        PrintWriter pw = new PrintWriter(new File(PATH_TO_RESOURCES + "/" + n + "_test_" + timestamp +".csv"));
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < HOW_MANY_RANDOM_INSTANCES; i++) {
            InstanceGenerator instanceGenerator = new InstanceGenerator(3, n);
            final FlowShopWithUncertainty uncertainFlowShopInstance = instanceGenerator.generateUncertainFlowShopInstance(0, 100, 50);
            System.out.println(uncertainFlowShopInstance.toString() + "\n");

            uncertainFlowShopInstance.toFile(n + "_uncertainFlowShop_" + timestamp + ".txt");

            MIH mih = new MIH(uncertainFlowShopInstance);
            measureMIHLowerBound(mih, sb);
//            measureMIHUpperBound(mih, sb);

            SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(uncertainFlowShopInstance);
//            measureSALowerBound(simulatedAnnealing, sb);
            measureSAUpperBound(simulatedAnnealing, sb);
        }

        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");
    }

    private static void measureMIHLowerBound(MIH mih, StringBuilder sb) {
        Object[] MIHresult = mih.solve(true, false);

        sb.append("MIH solution lower bound: ");
        sb.append(';');
        sb.append(MIHresult[0].toString());
        sb.append(';');
        sb.append("MIH solution upper bound: ");
        sb.append(';');
        sb.append(MIHresult[1].toString());
        sb.append(';');
        sb.append("Calculation time: ");
        sb.append(';');
        sb.append(MIHresult[2]);
        sb.append(';');
//        sb.append('\n');
    }

//    private static void measureMIHUpperBound(MIH mih, StringBuilder sb) {
//        Object[] MIHresult = mih.solve(false, false);
//
//        sb.append("MIH solution upper bound: ");
//        sb.append(';');
//        sb.append(MIHresult[0].toString());
//        sb.append(';');
//        sb.append("Calculation time: ");
//        sb.append(';');
//        sb.append(MIHresult[1]);
//        sb.append('\n');
//    }

//    private static void measureSALowerBound(SimulatedAnnealing simulatedAnnealing, StringBuilder sb) {
//        final String[] measureSAResults = measureSA(simulatedAnnealing, true);
//
//        sb.append("SA solution lower bound: ");
//        sb.append(';');
//        sb.append(measureSAResults[0]);
//        sb.append(';');
//        sb.append("Calculation time: ");
//        sb.append(';');
//        sb.append(measureSAResults[1]);
//        sb.append('\n');
//    }

    private static void measureSAUpperBound(SimulatedAnnealing simulatedAnnealing, StringBuilder sb) {
        final String[] measureSAResults = measureSA(simulatedAnnealing, false); //jak tu false tzn ?e delty temp b?d? liczone dla upper

        sb.append("SA solution lower bound: ");
        sb.append(';');
        sb.append(measureSAResults[1]);
        sb.append(';');
        sb.append("SA solution upper bound: ");
        sb.append(';');
        sb.append(measureSAResults[0]);
        sb.append(';');
        sb.append("Calculation time: ");
        sb.append(';');
        sb.append(measureSAResults[2]);
        sb.append('\n');
    }

    private static String[] measureSA(SimulatedAnnealing simulatedAnnealing, boolean isLowerBound) {
        //Helper array for debug
        Object[][] debugHelperResults = new Object[REPETITION_FOR_SIMULATED_ANNEALING][4];

        //calculate sumOfMinZ4 of all 5 repetitions
        int sumOfMinZ4 = 0;
        int sumOfMinZ4LowerBound = 0;
        double sumOfMeasuredTimes = 0;
        for(int i=0; i < REPETITION_FOR_SIMULATED_ANNEALING ; i++)
        {
            System.out.println("measureSA : " + i);
            Object[] SAresult = simulatedAnnealing.solveSA(isLowerBound, false);
            sumOfMinZ4 += (int) SAresult[1];
            sumOfMinZ4LowerBound += (int) SAresult[4];
            sumOfMeasuredTimes += (double) SAresult[3];
            debugHelperResults[i] = SAresult;
        }
        //calculate average value
        double averageOfMinZ4 = sumOfMinZ4 / REPETITION_FOR_SIMULATED_ANNEALING;
        double averageOfMinZ4LowerBound = sumOfMinZ4LowerBound / REPETITION_FOR_SIMULATED_ANNEALING;
        double averageOfMeasuredTimes = sumOfMeasuredTimes / REPETITION_FOR_SIMULATED_ANNEALING;

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator(',');
        NumberFormat formatter = new DecimalFormat("#0.00");
        formatter.setGroupingUsed(false);

        String[] results = new String[3];
        results[0] = formatter.format(averageOfMinZ4);
        results[1] = formatter.format(averageOfMinZ4LowerBound);
        results[2] = formatter.format(averageOfMeasuredTimes);
        return  results;
    }

}
