package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.GUIController;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.testdata.InstanceGenerator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static pl.uncertainflowshopsolver.algo.util.SimulatedAnnealingConfigurationUtil.*;

/**
 * @author Piotr Kubicki, created on 24.04.2016.
 */
public class SimulatedAnnealing {

    private AlgorithmEventDispatcher eventDispatcher;
    private ConfigurationProvider configurationProvider;

    private SolutionInitializer initializer;
    private SAConfiguration configuration;
    private volatile boolean running;   //volatile to avoid "visibility" problem, when the updates of one thread are not visible to other threads.

    private FlowShopWithUncertainty uncertainFlowShop;
    private int L;  //epoche, equals to N -> see constructor
    private final double P1 = 0.627;
    private final double lambda = 0.908;
            //1.0 - Math.exp(-14.0);// 0.908;    //inaczej: alpha. Pempera: 0.995;
    private Random random = ThreadLocalRandom.current();
    //TODO PKU - 3. pomysł na pamiętanie 2óch ostatnich randomów random 1 i random2

//    private static final double DECAY_RATE = 1.0 - Math.exp(-14.0);

    private double DESIRED_INITIAL_ACCEPTANCE_PROBABILITY = 0.925;

    public SimulatedAnnealing(FlowShopWithUncertainty uncertainFlowShop)
    {
        this.uncertainFlowShop = uncertainFlowShop;
        this.L = uncertainFlowShop.getTaskCount();
    }

    public SimulatedAnnealing(GUIController guiController) {
        this.configurationProvider = guiController;
        this.eventDispatcher = new AlgorithmEventDispatcher(guiController);
    }

    public void start() {
        prepareConfiguration();
        running = true;
        eventDispatcher.dispatchAlgorithmStarted();
        solve();
    }

    public void stop() {
        running = false;
    }

    private void prepareConfiguration() {
        configuration = configurationProvider.getSAConfiguration();
        try {
            initializer = configuration.getSolutionInitializerClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cant create solution initializer", e);
        }
    }

    public Object[] solveSA(boolean lowerBound, boolean printDebug) {

        long startTime = System.currentTimeMillis();

        Object[] result = new Object[5];

        int delta;
        double probability;

        double endTemperature = 0.5;   //Double.MIN_NORMAL;
        double temperature = 1000;  //initial temperature

//        initializeSA(temperature, endTemperature);
//        double temperature = getInitialTemperature(getTaskCount(), lambda, endTemperature);  //initial temperature

        int SACounter = 0;

        final Object[] resultInside3 = SubAlgorithm2.solveGreedy(uncertainFlowShop, null, printDebug);
        int valueBefore = (int) resultInside3[1];

        FlowShopWithUncertainty uncertainFlowShop_for_valueBefore = uncertainFlowShop.clone();

        int minimum = valueBefore;
        FlowShopWithUncertainty uncertainFlowShop_for_minimum = uncertainFlowShop.clone();

        int currentValue = valueBefore;
        int minimumForLowerBound=(int) resultInside3[0];;




//        ArrayList<State> tempGenerationStates = new ArrayList<State>();
//        while (tempGenerationStates.size() < 10000) {
//            TSPState curState = initialState.getNeighbor(1.0);
//            tempGenerationStates.add(curState);
//        }


        final int number = uncertainFlowShop.getM() * uncertainFlowShop.getN();
        final int factorial = partOfFactorial(number, Math.round((float)number / 1.005f));

//        double[] positiveEnergyDeltas = new double[factorial];
        List<Double> positiveEnergyDeltas = new ArrayList<>();


        int positiveEnergyDeltasCounter=0;
        final Object[] resultInside1 = SubAlgorithm2.solveGreedy(uncertainFlowShop, lowerBound, false);//TODO PKU zmienić wiecznie to samo smallValueBEfore - musi być przepięcie!
        int smallValueBefore = (int) resultInside1[0];
        FlowShopWithUncertainty uncertainFlowShop_temp = uncertainFlowShop.clone();

        int counter=0;
        while (positiveEnergyDeltasCounter < factorial && (isNotMaxIterationsReached(counter, number) || positiveEnergyDeltas.isEmpty()))   //TODO PKU 4.-Wstawić to jako rozbiegowe do głównej pętli
        {
            counter++;
            swapRandomlyTwoTasks(uncertainFlowShop_temp);

            final Object[] resultInside2 = SubAlgorithm2.solveGreedy(uncertainFlowShop_temp, lowerBound, false);
            int smallCurrentValue =(int) resultInside2[0];

            if (smallValueBefore < smallCurrentValue)
            {
                double positiveEnergyDelta = smallCurrentValue - smallValueBefore;
                if (positiveEnergyDelta < Double.MIN_NORMAL) {
                    throw new IllegalArgumentException(
                            "All energy deltas must be positive" + positiveEnergyDelta);
                }
                positiveEnergyDeltas.add(positiveEnergyDelta);

                smallValueBefore = smallCurrentValue;

                positiveEnergyDeltasCounter++;
            }
        }

        double []pED = new double[positiveEnergyDeltas.size()];
        for (int i = 0; i < pED.length; i++) {
            pED[i] = positiveEnergyDeltas.get(i);
        }

        double initialTemperature =
                calculateInitialTemperatureFromDesiredProbability(
                        DESIRED_INITIAL_ACCEPTANCE_PROBABILITY,
                        pED,
                        0.0001);
        System.out.printf(
                "Using init temp %.2f (target init acceptance prob %.3f)\n",
                initialTemperature, DESIRED_INITIAL_ACCEPTANCE_PROBABILITY);


//
//        System.out.println("SACounter:" + SACounter);   //TODO PKU test debug
////        System.out.println("SA current:" + currentValue);   //TODO PKU test debug
//        System.out.println("SA before:" + valueBefore);   //TODO PKU test debug
//        System.out.println("SA result:" + minimum + "\n");   //TODO PKU test debug
//        System.out.println("*****************************" + "\n");   //TODO PKU test debug


        /**
         * log0,995(0,5/1000)=1517 więc wszystkich podejść w górę możemy zrealizować 1518(sic!)
         * Nie wiemy, czy wyliczenie wszystkich n! permutacji nie będzie przebiegało ciągle w dół, tzn. że nie
         * będziemy ani razu podchodzić w górę. Nie wiemy też, czy n! > 1528, może być mniejsze. Stąd
         * warunek dojścia do temperatury 0,5 nie jest wystarczającym kryterium stopu. Maksymalnym rozsądnym
         * dla każdego przypadku kryterium stopu to n! (n - liczba miast na trasie = way.length). Przyjmujemy tu jako kryterium stopu wartość
         * howManyTimesYouWant - tyle razy obliczamy (można tu wstawić n!) - i do tego dodajemy kryterium temperaturowe
         */
//        while (temperature > endTemperature) {  //warunek końca algorytmu
        while (initialTemperature > endTemperature) {  //warunek końca algorytmu
            //TODO PKU - 2. dla małych instancji, np F3, |J|=3, nie ma sensu liczyć 1517 iteracji (poza tym czas jest wykonania algo jest ten sam). Może warto rozważyć zakończenie po jakiejś stałej określonej maksymalnej liczbie niepoprawiająych zmian?

            for (int i = 0; i < L; i++) {
                //TODO PKU - 1. czy może zamiast swapować 2óch sąsiadów, lepiej Collections.shuffle(arrlist)? [Fisher–Yates shuffle]

                swapRandomlyTwoTasks(uncertainFlowShop);

                SACounter++;
//                System.out.println("SACounter:" + SACounter);   //TODO PKU test debug

                //TODO PKU - 1end.

                final Object[] resultInside4 = SubAlgorithm2.solveGreedy(uncertainFlowShop, null, printDebug);
                currentValue = (int) resultInside4[1];  //upper bound

                if (valueBefore >= currentValue) {
                    valueBefore = currentValue;
                    uncertainFlowShop_for_valueBefore = uncertainFlowShop.clone();
                    if (minimum > currentValue) {
                        minimum = currentValue;
                        uncertainFlowShop_for_minimum = uncertainFlowShop.clone();
                        minimumForLowerBound = (int) resultInside4[0];
                    }
                } else {
                    delta = currentValue - valueBefore;
                    probability = Math.exp(-delta / temperature);

                    Random generator = new Random();
                    double zeroToOne = generator.nextInt(1001) / 1000.0;

//                    System.out.println("probability: " + probability + ", zeroToOne: " + zeroToOne);   //TODO PKU test debug

                    if (zeroToOne <= probability) {
                        valueBefore = currentValue;
                        uncertainFlowShop_for_valueBefore = uncertainFlowShop.clone();
//                        System.out.println("zeroToOne <= probability. Zaakceptowane gorsze rozwiązanie ");   //TODO PKU test debug
                    } else {
                        uncertainFlowShop = uncertainFlowShop_for_valueBefore.clone();
                        //pudło! policzony uncertainFlowShop ma większe z4, zostaje więc stary
//                        System.out.println("zeroToOne > probability. Bez akceptacji");   //TODO PKU test debug
                    }
                }
            }

//            temperature = lambda * temperature; //geometryczny schemat chłodzenia
            initialTemperature = lambda * initialTemperature;

            //howManyTimesYouWant--;


//            System.out.println("SA current:" + currentValue);   //TODO PKU test debug
//            System.out.println("SA before:" + valueBefore);   //TODO PKU test debug
//            System.out.println("SA result:" + minimum + "\n");   //TODO PKU test debug
//            System.out.println("*****************************" + "\n");   //TODO PKU test debug
        }

        long stopTime = System.currentTimeMillis();
        double elapsedTime = (stopTime - startTime) / 1000d; //in seconds


        result[0] = uncertainFlowShop_for_minimum;
        result[1] = minimum;
        result[2] = positiveEnergyDeltas;
        result[3] = elapsedTime;  //It will be in seconds
//        result[3] = Double.parseDouble(formatter.format(elapsedTime / 1000d));  //It will be in seconds
        result[4] = minimumForLowerBound;
        return result;
    }

    private boolean isNotMaxIterationsReached(int counter, int number) {
        final int factorial = partOfFactorial(number, Math.round((float)number / 1.05f));
        return counter < factorial;
    }

    private void swapRandomlyTwoTasks(FlowShopWithUncertainty uncertainFlowShop) {
        int random1 = random.nextInt(getTaskCount());
        int random2 = random.nextInt(getTaskCount());
        while (random2 == random1) {
            random2 = random.nextInt(getTaskCount());
        }
        Collections.swap(uncertainFlowShop.getTasks(), random1, random2);
    }

    private void initializeSA(double initialTemperature, double finalTemperature) {

    }

    private Integer getTaskCount() {
        return uncertainFlowShop.getTaskCount();
    }

    public static double getInitialTemperature(int taskCount, double lambda, double endTemperature)
    {
        return endTemperature / Math.pow(lambda, taskCount * (taskCount - 1));
    }

    public static int partOfFactorial(int number, int partOfNumber) {
        if (number <= 1)
            return 1;
        else if(number <= partOfNumber)
            return partOfNumber;
        else
            return number * partOfFactorial(number - 1, partOfNumber);
    }

    public static void main(String[] args) {
//        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(new FlowShopWithUncertainty());
//        Object[] result = simulatedAnnealing.solveSA(true, true);
//        System.out.println("SA solution: ");
//        System.out.println(resu[0].toString());
//        System.out.println("SA result: " + result[1]);

        InstanceGenerator instanceGenerator = new InstanceGenerator(3, 6);
        final FlowShopWithUncertainty uncertainFlowShopInstance = instanceGenerator.generateUncertainFlowShopInstance(0, 100, 50);
//        uncertainFlowShopInstance.toFile("qpa1.txt");
        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(uncertainFlowShopInstance);
        Object[] result = simulatedAnnealing.solveSA(true, false);
        System.out.println("SA solution: ");
        System.out.println(result[0].toString());
        System.out.println("SA result: " + result[1]);


        SimulatedAnnealing simulatedAnnealing2 = new SimulatedAnnealing(uncertainFlowShopInstance);
        Object[] result2 = simulatedAnnealing2.solveSA(true, false);
        System.out.println("SA solution: ");
        System.out.println(result2[0].toString());
        System.out.println("SA result: " + result2[1]);

        System.out.println(Arrays.equals((double[])result[3], (double[]) result2[3]));
    }
}
