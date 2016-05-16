package pl.uncertainflowshopsolver.testdata;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Piotr Kubicki, created on 10.05.2016.
 */
public class TimeMeasureDemo {


    public static void main(String[] args)
    {
        long startTime = System.currentTimeMillis();

        try {
            Thread.sleep(2506);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        long stopTime = System.currentTimeMillis();
        double elapsedTime = (stopTime - startTime) / 1000d; //in seconds

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator(',');
        NumberFormat formatter = new DecimalFormat("#0.00");
        formatter.setGroupingUsed(false);
        System.out.println(formatter.format(elapsedTime));
    }
}
