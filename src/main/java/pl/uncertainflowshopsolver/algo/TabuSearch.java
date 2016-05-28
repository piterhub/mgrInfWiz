package pl.uncertainflowshopsolver.algo;

import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.algo.util.SimulatedAnnealingConfigurationUtil;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.GUIController;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.testdata.InstanceGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static pl.uncertainflowshopsolver.algo.util.SimulatedAnnealingConfigurationUtil.calculateInitialTemperatureFromDesiredProbability;
import static pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty.swapRandomlyTwoTasks;
import static pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher.EndingReason;

/**
 * @author Piotr Kubicki, created on 24.04.2016.
 */
public class TabuSearch {

    private static final Random random = new Random();

    private AlgorithmEventDispatcher eventDispatcher;
    private ConfigurationProvider configurationProvider;

    private SolutionInitializer initializer;
    private SAConfiguration configuration;
    private volatile boolean running;   //volatile to avoid "visibility" problem, when the updates of one thread are not visible to other threads.

    private FlowShopWithUncertainty uncertainFlowShop;

            //1.0 - Math.exp(-14.0);// 0.908;    //inaczej: alpha. Pempera: 0.995;

    //TODO PKU - 3. pomysł na pamiętanie 2óch ostatnich randomów random 1 i random2

//    private static final double DECAY_RATE = 1.0 - Math.exp(-14.0);

//    private double desiredInitialAcceptanceProbability = 0.925;
    private int L;  //epoche, equals to L -> see constructor
    private double mInitialTemperature;
//    private double alpha = 0.908;
//    double endTemperature = 0.5;   //Double.MIN_NORMAL;
//    double initialTemperature = 1000;  //initial initialTemperature
//    double errorThreshold;
//    int samplesCardinality;
//    int maxNumberOfIterations;

    public TabuSearch(FlowShopWithUncertainty uncertainFlowShop)
    {
        this.uncertainFlowShop = uncertainFlowShop;
        this.L = uncertainFlowShop.getTaskCount();
    }

    public TabuSearch(GUIController guiController) {
        this.configurationProvider = guiController;
        this.eventDispatcher = new AlgorithmEventDispatcher(guiController);
    }

    public TabuSearch()
    {
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
        //TODO nie będzie inicjalizera, ale będzie sposób generowania sąsiedztwa
//        try {
//            initializer = configuration.getWayToGenerateNeighborhoodEnum().newInstance();
//        } catch (InstantiationException | IllegalAccessException e) {
//            throw new RuntimeException("Can't create solution initializer", e);
//        }
    }

    private void solve()
    {
        long startTime = System.currentTimeMillis();
        uncertainFlowShop = configuration.getUncertainFlowShop();

        /***********Global minimum**************/
        final Object[] result = SubAlgorithm2.solveGreedy(uncertainFlowShop, null, false);
        int globalMinimum = (int) result[1];
        int globalMinimumForLowerBound = (int) result[0];
        FlowShopWithUncertainty uncertainFlowShop_for_minimum = uncertainFlowShop.clone();
        uncertainFlowShop_for_minimum.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);
        uncertainFlowShop_for_minimum.setLowerBoundOfMinMaxRegretOptimalization(globalMinimumForLowerBound);
        /*************************/

        int valueBefore = globalMinimum;
        FlowShopWithUncertainty uncertainFlowShop_for_valueBefore = uncertainFlowShop.clone();
        uncertainFlowShop_for_valueBefore.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);     //TODO workaround. Może lepiej w clone dać kopiowanie tych fieldów
        uncertainFlowShop_for_valueBefore.setLowerBoundOfMinMaxRegretOptimalization(globalMinimumForLowerBound);

        int iterations = 0;
        int lastImprovementIteration = 0;

//        if (lastImprovementIteration % 10 == 0)
//            eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);

        /*************************/
        FlowShopWithUncertainty tempFlowShopForBenAmeurAlgoPurpose = uncertainFlowShop.clone(); //przepięcie
        ArrayList<FlowShopWithUncertainty> tempGenerationStates = new ArrayList<>();
        while (tempGenerationStates.size() < configuration.getSamplesCardinality()) {
//            FlowShopWithUncertainty curState = uncertainFlowShop.getNeighbour(1.0);   //przepięcie
            FlowShopWithUncertainty curState = tempFlowShopForBenAmeurAlgoPurpose.getNeighbour(1.0);    //przepięcie
            final Object[] results = SubAlgorithm2.solveGreedy(curState, false, false);
            curState.setUpperBoundOfMinMaxRegretOptimalization((int)results[0]);
            tempGenerationStates.add(curState);
            tempFlowShopForBenAmeurAlgoPurpose = curState.clone();  //przepięcie
        }
        double initialTemperature =
                SimulatedAnnealingConfigurationUtil.calculateFromDesiredProbability(
                        configuration.getDesiredInitialAcceptanceProbability(),
                        tempGenerationStates,
                        configuration.getErrorThreshold());
        System.out.printf(
                "Using init temp %.2f (target init acceptance prob %.3f)\n",
                initialTemperature, configuration.getDesiredInitialAcceptanceProbability());
        /*************************/
        mInitialTemperature = initialTemperature;

        long midTime_2 = System.currentTimeMillis();
        double elapsedTime_delta1 = (midTime_2 - startTime);
        long midTime_3 = 0L;
        double elapsedTime_delta3 = 0d;

        while (running/* && initialTemperature > configuration.getEndTemperature() && iterations < configuration.getMaxNumberOfIterations()*/){

            midTime_2 = System.currentTimeMillis(); //it is not bug! this line is important!

            if(midTime_3 != 0L)
            {
                double elapsedTime_delta2 = midTime_2 - midTime_3;
                elapsedTime_delta3 += elapsedTime_delta2;
            }

            if (lastImprovementIteration % 10 == 0)
                eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);

            midTime_3= System.currentTimeMillis();

//            System.out.println(iterations);
//            System.out.println(uncertainFlowShop.getUpperBoundOfMinMaxRegretOptimalization());
//            System.out.println(uncertainFlowShop.getLowerBoundOfMinMaxRegretOptimalization());
//            System.out.println(uncertainFlowShop.toString());

            iterations++;

            for (int i = 0; i < configuration.getEpocheLength(); i++) {

                final FlowShopWithUncertainty neighbour = uncertainFlowShop.getNeighbour(1.0);
                final Object[] resultInside = SubAlgorithm2.solveGreedy(neighbour, null, false);
                int currentValue = (int) resultInside[1];  //upper bound

                if (valueBefore >= currentValue) {
                    valueBefore = currentValue;
                    uncertainFlowShop_for_valueBefore = neighbour.clone();
                    if (globalMinimum > currentValue) {
                        globalMinimum = currentValue;
                        uncertainFlowShop_for_minimum = neighbour.clone();
                        uncertainFlowShop_for_minimum.setUpperBoundOfMinMaxRegretOptimalization(globalMinimum);
                        uncertainFlowShop_for_minimum.setLowerBoundOfMinMaxRegretOptimalization((int) resultInside[0]);
                    }
                } else {
                    int delta = currentValue - valueBefore;
                    double probability = Math.exp(-delta / initialTemperature);
                    double zeroToOne = random.nextInt(1001) / 1000.0;

                    if (zeroToOne <= probability) {
                        valueBefore = currentValue;
                        uncertainFlowShop_for_valueBefore = neighbour.clone();
                    } else {
                        uncertainFlowShop = uncertainFlowShop_for_valueBefore.clone();
                        uncertainFlowShop.setUpperBoundOfMinMaxRegretOptimalization(uncertainFlowShop_for_valueBefore.getUpperBoundOfMinMaxRegretOptimalization());
                    }
                }
            }
            initialTemperature = configuration.getDecayRate() * initialTemperature;

            // stop conditions
            if (configuration.getMaxNumberOfIterations() != 0 && iterations > configuration.getMaxNumberOfIterations()) {
                break;
            }
//            if (configuration.getMaxIterationsWithoutImprovement() != 0 &&
//                    iterations - lastImprovementIteration > configuration.getMaxIterationsWithoutImprovement()) {
//                break;
//            }
        }
        long stopTime = System.currentTimeMillis();
        double elapsedLastPeriodOfTime = (stopTime - midTime_3) / 1000d; //in seconds
        double elapsedTime_delta4 = elapsedTime_delta3 + elapsedTime_delta1;
        double elapsedTime = (elapsedLastPeriodOfTime + elapsedTime_delta4) / 1000d; //in seconds
        uncertainFlowShop_for_minimum.setElapsedTime(elapsedTime);

        // Last update
        eventDispatcher.dispatchIterationUpdated(iterations, uncertainFlowShop_for_minimum);

        if (iterations >= configuration.getMaxNumberOfIterations()) {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.ALL_ITERATIONS, elapsedTime, uncertainFlowShop_for_minimum, mInitialTemperature);
        } else if (running) {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.WITHOUT_PROGRESS, elapsedTime, uncertainFlowShop_for_minimum, mInitialTemperature);
        } else {
            eventDispatcher.dispatchAlgorithmEnded(EndingReason.CANCELLED, elapsedTime, uncertainFlowShop_for_minimum, mInitialTemperature);
        }

    }

    public Object[] solveSA(boolean lowerBound, boolean printDebug) {

        long startTime = System.currentTimeMillis();

        Object[] result = new Object[5];

        int delta;
        double probability;



//        initializeSA(initialTemperature, endTemperature);
//        double initialTemperature = getInitialTemperature(getTaskCount(), alpha, endTemperature);  //initial initialTemperature

        int SACounter = 0;

        final Object[] resultInside3 = SubAlgorithm2.solveGreedy(uncertainFlowShop, null, printDebug);
        int valueBefore = (int) resultInside3[1];

        FlowShopWithUncertainty uncertainFlowShop_for_valueBefore = uncertainFlowShop.clone();

        int minimum = valueBefore;
        FlowShopWithUncertainty uncertainFlowShop_for_minimum = uncertainFlowShop.clone();

        int currentValue = valueBefore;
        int minimumForLowerBound=(int) resultInside3[0];




//        ArrayList<State> tempGenerationStates = new ArrayList<State>();
//        while (tempGenerationStates.size() < 10000) {
//            TSPState curState = initialState.getNeighbour(1.0);
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
            swapRandomlyTwoTasks(uncertainFlowShop_temp, uncertainFlowShop_temp.getTaskCount());

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
                        configuration.getDesiredInitialAcceptanceProbability(),
                        pED,
                        configuration.getErrorThreshold());
        System.out.printf(
                "Using init temp %.2f (target init acceptance prob %.3f)\n",
                initialTemperature, configuration.getDesiredInitialAcceptanceProbability());


//
//        System.out.println("SACounter:" + SACounter);   //
////        System.out.println("SA current:" + currentValue);   //
//        System.out.println("SA before:" + valueBefore);   //
//        System.out.println("SA result:" + minimum + "\n");   //
//        System.out.println("*****************************" + "\n");   //


        /**
         * log0,995(0,5/1000)=1517 więc wszystkich podejść w górę możemy zrealizować 1518(sic!)
         * Nie wiemy, czy wyliczenie wszystkich n! permutacji nie będzie przebiegało ciągle w dół, tzn. że nie
         * będziemy ani razu podchodzić w górę. Nie wiemy też, czy n! > 1528, może być mniejsze. Stąd
         * warunek dojścia do temperatury 0,5 nie jest wystarczającym kryterium stopu. Maksymalnym rozsądnym
         * dla każdego przypadku kryterium stopu to n! (n - liczba miast na trasie = way.length). Przyjmujemy tu jako kryterium stopu wartość
         * howManyTimesYouWant - tyle razy obliczamy (można tu wstawić n!) - i do tego dodajemy kryterium temperaturowe
         */
//        while (initialTemperature > endTemperature) {  //warunek końca algorytmu
        while (initialTemperature > configuration.getEndTemperature()) {  //warunek końca algorytmu
            //TODO PKU - 2. dla małych instancji, np F3, |J|=3, nie ma sensu liczyć 1517 iteracji (poza tym czas jest wykonania algo jest ten sam). Może warto rozważyć zakończenie po jakiejś stałej określonej maksymalnej liczbie niepoprawiająych zmian?

            for (int i = 0; i < L; i++) {
                //TODO PKU - 1. czy może zamiast swapować 2óch sąsiadów, lepiej Collections.shuffle(arrlist)? [Fisher–Yates shuffle]

                swapRandomlyTwoTasks(uncertainFlowShop, uncertainFlowShop.getTaskCount());

                SACounter++;
//                System.out.println("SACounter:" + SACounter);   //

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
                    probability = Math.exp(-delta / initialTemperature);

                    Random generator = new Random();
                    double zeroToOne = generator.nextInt(1001) / 1000.0;

//                    System.out.println("probability: " + probability + ", zeroToOne: " + zeroToOne);   //

                    if (zeroToOne <= probability) {
                        valueBefore = currentValue;
                        uncertainFlowShop_for_valueBefore = uncertainFlowShop.clone();
//                        System.out.println("zeroToOne <= probability. Zaakceptowane gorsze rozwiązanie ");   //
                    } else {
                        uncertainFlowShop = uncertainFlowShop_for_valueBefore.clone();
                        //pudło! policzony uncertainFlowShop ma większe z4, zostaje więc stary
//                        System.out.println("zeroToOne > probability. Bez akceptacji");   //
                    }
                }
            }

//            initialTemperature = alpha * initialTemperature; //geometryczny schemat chłodzenia
            initialTemperature = configuration.getDecayRate() * initialTemperature;

            //howManyTimesYouWant--;


//            System.out.println("SA current:" + currentValue);   //
//            System.out.println("SA before:" + valueBefore);   //
//            System.out.println("SA result:" + minimum + "\n");   //
//            System.out.println("*****************************" + "\n");   //
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
        TabuSearch simulatedAnnealing = new TabuSearch(uncertainFlowShopInstance);
        Object[] result = simulatedAnnealing.solveSA(true, false);
        System.out.println("SA solution: ");
        System.out.println(result[0].toString());
        System.out.println("SA result: " + result[1]);


        TabuSearch simulatedAnnealing2 = new TabuSearch(uncertainFlowShopInstance);
        Object[] result2 = simulatedAnnealing2.solveSA(true, false);
        System.out.println("SA solution: ");
        System.out.println(result2[0].toString());
        System.out.println("SA result: " + result2[1]);

        System.out.println(Arrays.equals((double[])result[3], (double[]) result2[3]));
    }

    public void setEventDispatcher(AlgorithmEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }
}
