package pl.uncertainflowshopsolver.testdata;

import pl.uncertainflowshopsolver.MainConsoleWithMocks;
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static pl.uncertainflowshopsolver.MainConsoleWithMocks.PATH_TO_FILE_WITH_UNCERTAIN_FLOWSHOP;

/**
 * @author Piotr Kubicki, created on 10.05.2016.
 */
public class TimeMeasureDemo {


    public static final String PATH_TO_RESOURCES = "C:/Users/pkubicki/IdeaProjects/mgrInfWiz/resources";
    private static int dupa;

    public static void main(String[] args) throws FileNotFoundException {
//        long startTime = System.currentTimeMillis();
//
//        try {
//            Thread.sleep(2506);                 //1000 milliseconds is one second.
//        } catch(InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }
//
//        long stopTime = System.currentTimeMillis();
//        double elapsedTime = (stopTime - startTime) / 1000d; //in seconds
//
//        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
//        otherSymbols.setDecimalSeparator(',');
//        NumberFormat formatter = new DecimalFormat("#0.00");
//        formatter.setGroupingUsed(false);
//        System.out.println(formatter.format(elapsedTime));

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM-HH.mm-ss");
        final String timestamp = simpleDateFormat.format(new Date());
        PrintWriter pw = new PrintWriter(new File(PATH_TO_RESOURCES + "/demo_" + timestamp +".csv"));
        StringBuilder sb = new StringBuilder();

        dupa = 5;
        for (int instance = 1; instance <= dupa; instance++) {

            sb.append(instance);
            sb.append(';');

            for (int repetition = 0; repetition < 5; repetition++) {

                sb.append(repetition+1);
                sb.append(';');

                for (int alfa = 0; alfa < 5; alfa++) {

                    sb.append("alfa");
                    sb.append(';');
                }

                sb.append('\n');
                if(repetition < dupa-1)
                {
                    sb.append(instance);
                    sb.append(';');
                }

//                MIH mih = new MIH(uncertainFlowShopInstance);
//                measureMIHLowerBound(mih, sb);
////            measureMIHUpperBound(mih, sb);
//
//                SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(uncertainFlowShopInstance);
////            measureSALowerBound(simulatedAnnealing, sb);
//                measureSAUpperBound(simulatedAnnealing, sb);
            }
        }



        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");
    }
}
