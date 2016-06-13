package pl.uncertainflowshopsolver.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.uncertainflowshopsolver.algo.MIH;
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.algo.TabuSearch;
import pl.uncertainflowshopsolver.algo.util.WayToGenerateNeighborhoodEnum;
import pl.uncertainflowshopsolver.algo.util.WhichAlgorithmEnum;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.TSConfiguration;
import pl.uncertainflowshopsolver.config.impl.SAConfigurationImpl;
import pl.uncertainflowshopsolver.config.impl.TSConfigurationImpl;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventListener;
import pl.uncertainflowshopsolver.testdata.InstanceGenerator;
import pl.uncertainflowshopsolver.testdata.UncertainFlowShopParser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class GUIController implements ConfigurationProvider, AlgorithmEventListener, Initializable {
    private static final int MAXIMUM_NUMBER_OF_OPERATIONS = 500;

    public ChoiceBox<String> initializerChoiceBox;
    public DoubleTextBox0To1 desiredInitialAcceptanceProbabilityDoubleTextBox0To1;
    public IntegerTextBox epocheLengthIntegerTextBox;
    public DoubleTextBox0To1 decayRateDoubleTextBox0To1;
    public DoubleTextBox0To1 errorThresholdDoubleTextBox0To1;
    public IntegerTextBox samplesCardinalityIntegerTextBox;
    public IntegerTextBox maxNumberOfIterationsIntegerTextBox;
//    public IntegerTextBox maxIterationsWithoutImprovementIntegerTextBox;
//    public DoubleTextBox0To1 cutOffEnergyLevelDoubleTextBox;
    public ProgressBar progressBar;
    public Label progressLabel;
    public Button startStopButton;
    public TextArea logsTextArea;
    public TextArea bestSolutionTextArea;

    public Button generateFlowShopButton;
    public Button chooseFolderToSaveResultButton;
    public Button importFlowShopFromFileButton;
    public Button editFlowshopManuallyButton;

    public IntegerTextBox taskCount;
    public IntegerTextBox machineCount;
    public IntegerTextBox K;
    public IntegerTextBox C;
    public ChoiceBox algorithmChoiceBox;
    public TitledPane saOptionsTitledPane;
    public TitledPane chartTitledPane;
    public TitledPane tsOptionsTitledPane;
    public ChoiceBox initializerTSChoiceBox;
    public IntegerTextBox sizeOfNeighborhoodIntegerTextBox;
    public IntegerTextBox lengthOfTabuListIntegerTextBox;
    public IntegerTextBox maxIterWithoutImprovementAspirationIntegerTextBox;
    public IntegerTextBox maxIterWithoutImprovementDiversificationIntegerTextBox;
    public IntegerTextBox maxNumberOfIterationsAsStopTabuSearchIntegerTextBox;
    public IntegerTextBox SA_maxIterWithoutImproDiversificationIntegerTextBox;
    public LineChart lineChart;

    private XYChart.Series<Integer, Double> series = new XYChart.Series<>();

    private boolean isChartUpdateAllowed = true;
    private boolean isSANotTSChosen = true;

    private FlowShopWithUncertainty flowShop;
    private NumberBinding maxProblemProperty;

    public FlowShopWithUncertainty getFlowShop() {
        return flowShop;
    }

    private Map<String, WayToGenerateNeighborhoodEnum> initializerNameClassMap;
    private Map<String, WhichAlgorithmEnum> algorithmEnumMap;

    private SimulatedAnnealing SAalgorithm;
    private TabuSearch TSAlgorithm;

    private SAConfiguration activeSAConfiguration;
    private TSConfiguration activeTSConfiguration;

    private AlgorithmState algorithmState = AlgorithmState.STOPPED;
    private Thread algorithmThread;

    private Map<Integer, Double> iterationFitnessMap = Collections.synchronizedMap(new HashMap<Integer, Double>());
    private int bestSolutionIteration = -1;
    private FlowShopWithUncertainty bestSolution;

    /**
     * These NumberBindings must be class fields due to ChangeListeners, which use them!
     */
    private NumberBinding firstSAProperty;
    private NumberBinding secondSAProperty;
    private NumberBinding thirdSAProperty;
    private NumberBinding fourthSAProperty;
    private NumberBinding fifthSAProperty;

    private NumberBinding firstTSProperty;
    private NumberBinding secondTSProperty;
    private NumberBinding thirdTSProperty;
    private NumberBinding fourthTSProperty;

    private File defaultDirectory;
    private File selectedDirectory;

    /**
     * It is used in order to control, whether {@link FlowShopWithUncertainty} flowShop is chosen, or not.
     */
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializerNameClassMap = new HashMap<>();
        initializerNameClassMap.put("Swap", WayToGenerateNeighborhoodEnum.SWAP);
        initializerNameClassMap.put("Insert", WayToGenerateNeighborhoodEnum.INSERT);
        initializerNameClassMap.put("Fischer-Yates shuffle", WayToGenerateNeighborhoodEnum.FISCHER_YATES_SHUFFLE);
        initializerChoiceBox.setItems(FXCollections.observableArrayList(initializerNameClassMap.keySet()));
        initializerChoiceBox.getSelectionModel().select(0);
        initializerTSChoiceBox.setItems(FXCollections.observableArrayList(initializerNameClassMap.keySet()));
        initializerTSChoiceBox.getSelectionModel().selectFirst();

        algorithmEnumMap = new HashMap<>();
        algorithmEnumMap.put("Simulated Annealing", WhichAlgorithmEnum.SIMULATED_ANNEALING);
        algorithmEnumMap.put("MIH", WhichAlgorithmEnum.MIH);
        algorithmEnumMap.put("Tabu Search", WhichAlgorithmEnum.TABU_SEARCH);
        algorithmChoiceBox.setItems(FXCollections.observableArrayList(algorithmEnumMap.keySet()));
        algorithmChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                if(flowShop != null)
                {
                    if(algorithmChoiceBox.getItems().get((Integer) newValue) == "MIH")
                    {
                        if ((flowShop.getTaskCount() * flowShop.getM()) <= MAXIMUM_NUMBER_OF_OPERATIONS)
                        {
                            startStopButton.setDisable(false);
                        }
                        saOptionsTitledPane.setDisable(true);
                        tsOptionsTitledPane.setDisable(true);
                        chartTitledPane.setDisable(true);
                    }
                    else if(algorithmChoiceBox.getItems().get((Integer) newValue) == "Simulated Annealing")
                    {
                        enableEditingPartOfConfiguration();
                        startStopButton.setDisable(true);
                        saOptionsTitledPane.setDisable(false);
                        tsOptionsTitledPane.setDisable(true);
                        chartTitledPane.setDisable(false);
                        propertyChangeSupport.firePropertyChange("SAChosen",null, null);
                    }
                    else if(algorithmChoiceBox.getItems().get((Integer) newValue) == "Tabu Search")
                    {
                        enableEditingPartOfConfiguration();
                        startStopButton.setDisable(true);
                        saOptionsTitledPane.setDisable(true);
                        tsOptionsTitledPane.setDisable(false);
                        chartTitledPane.setDisable(false);
                        propertyChangeSupport.firePropertyChange("TSChosen",null, null);
                    }
                }
            }
        });
        algorithmChoiceBox.getSelectionModel().selectFirst();

        maxNumberOfIterationsIntegerTextBox.integerProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                progressLabel.setText("0/" + newValue.intValue());
                progressBar.setProgress(0.0);
                if(maxNumberOfIterationsIntegerTextBox.integerProperty().getValue() > 2000) {
                    lineChart.setDisable(true);
                    isChartUpdateAllowed = false;
                }
                else {
                    lineChart.setDisable(false);
                    isChartUpdateAllowed = true;
                }
            }
        });

        maxNumberOfIterationsAsStopTabuSearchIntegerTextBox.integerProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                progressLabel.setText("0/" + newValue.intValue());
                progressBar.setProgress(0.0);
                if(maxNumberOfIterationsAsStopTabuSearchIntegerTextBox.integerProperty().getValue() > 2000) {
                    lineChart.setDisable(true);
                    isChartUpdateAllowed = false;
                }
                else {
                    lineChart.setDisable(false);
                    isChartUpdateAllowed = true;
                }
            }
        });

        lineChart.getXAxis().setAutoRanging(false);
        lineChart.getData().add(series);
        lineChart.setAnimated(false);

        startStopButton.setDisable(true);
        generateFlowShopButton.setDisable(true);
        disableEditingPartOfConfiguration();
        prepareSABinding();
        prepareTSBinding();
        prepareMaxProblemBinding();
        algorithmChoiceBox.setDisable(true);

        this.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("FlowShopChosen")) {
//                    enableEditingPartOfConfiguration();
                    algorithmChoiceBox.setDisable(false);
                    algorithmChoiceBox.getSelectionModel().selectLast();
                }
                else if (event.getPropertyName().equals("SAChosen")) {
                    isSANotTSChosen = true;
                }
                else if (event.getPropertyName().equals("TSChosen")) {
                    isSANotTSChosen = false;
                }
            }
        });
//
//        desiredInitialAcceptanceProbabilityDoubleTextBox0To1.doubleProperty().set(0.925);
//        epocheLengthIntegerTextBox.insertText(0, "10");
//        decayRateDoubleTextBox0To1.insertText(0, "0.995");
//        errorThresholdDoubleTextBox0To1.insertText(0, "0.0001");
//        samplesCardinalityIntegerTextBox.insertText(0, "10000");
//        maxNumberOfIterationsIntegerTextBox.insertText(0, "1000");
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @FXML
    public void onStartStopButton(ActionEvent actionEvent) {

        if(algorithmChoiceBox.getSelectionModel().getSelectedItem() == "MIH")
        {
            if (algorithmState == AlgorithmState.STOPPED) {
                if (flowShop != null) {
                    MIH mih = new MIH(flowShop);
                    final Object[] resultMIH = mih.solve(true, false);
                    bestSolutionTextArea.setText("MIH result: " + resultMIH[0] + "\n" + "Sequence result:\n" + resultMIH[3].toString());
                }
            } else {
                TSAlgorithm.stop();
            }
        }
        else if(algorithmChoiceBox.getSelectionModel().getSelectedItem() == "Simulated Annealing")
        {
            activeSAConfiguration = getSAConfiguration();
            if (algorithmState == AlgorithmState.STOPPED) {
                if (flowShop != null) {
                    algorithmThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SAalgorithm.start();
                        }
                    });
                    algorithmThread.setDaemon(true);
                    algorithmThread.start();
                }
            } else {
                SAalgorithm.stop();
            }
        }
        else if(algorithmChoiceBox.getSelectionModel().getSelectedItem() == "Tabu Search")
        {
            activeTSConfiguration = getTSConfiguration();
            if (algorithmState == AlgorithmState.STOPPED) {
                if (flowShop != null) {
                    algorithmThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TSAlgorithm.start();
                        }
                    });
                    algorithmThread.setDaemon(true);
                    algorithmThread.start();
                }
            } else {
                TSAlgorithm.stop();
            }
        }
    }

    @FXML
    public void onFromFileButton(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        if(defaultDirectory != null)
        {
            chooser.setInitialDirectory(defaultDirectory);
        }
        File file = chooser.showOpenDialog(importFlowShopFromFileButton.getScene().getWindow());

        if (file != null) {
            try {
                setFlowShop(UncertainFlowShopParser.parseFileToFlowShopWithUncertainty(file.getAbsolutePath()));
                final Double value = desiredInitialAcceptanceProbabilityDoubleTextBox0To1.doubleProperty().getValue();
                String valueString = value+"";
                int count = valueString.length();
                desiredInitialAcceptanceProbabilityDoubleTextBox0To1.replaceText(0, count, valueString);
                defaultDirectory = file.getParentFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onEditFlowshopManuallyButton(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit_window.fxml"));
        try {
            Parent root = (Parent) loader.load();
            EditWindowController editWindowController = loader.getController();
            editWindowController.setGuiController(this);

            Stage s = new Stage();
            Scene scene = new Scene(root);
            s.setScene(scene);
            editWindowController.setStage(s);
            s.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFlowShop(FlowShopWithUncertainty flowShop) {
        FlowShopWithUncertainty oldFlowShop = this.flowShop;
        this.flowShop = flowShop;
        final Double value = desiredInitialAcceptanceProbabilityDoubleTextBox0To1.doubleProperty().getValue();
        String valueString = value+"";
        int count = valueString.length();
        desiredInitialAcceptanceProbabilityDoubleTextBox0To1.replaceText(0, count, valueString);
        propertyChangeSupport.firePropertyChange("FlowShopChosen",oldFlowShop, flowShop);
    }

    @Override
    public SAConfiguration getSAConfiguration() {
        final WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum = initializerNameClassMap.get(initializerChoiceBox.getValue());
        this.activeSAConfiguration = SAConfigurationImpl.newBuilder()
                .withDesiredInitialAcceptanceProbability(desiredInitialAcceptanceProbabilityDoubleTextBox0To1.getValue())
                .withEpocheLength(epocheLengthIntegerTextBox.getValue())
                .withDecayRate(decayRateDoubleTextBox0To1.getValue())
                .withErrorThreshold(errorThresholdDoubleTextBox0To1.getValue())
                .withSamplesCardinality(samplesCardinalityIntegerTextBox.getValue())
                .withMaxNumberOfIterations(maxNumberOfIterationsIntegerTextBox.getValue())
                .withMaxIterationsWithoutImprovementForDiversificationPurpose(SA_maxIterWithoutImproDiversificationIntegerTextBox.getValue())
                .withWayToGenerateNeighborhood(wayToGenerateNeighborhoodEnum)
                .withUncertainFlowshop(flowShop)
                .build();

        return activeSAConfiguration;
    }

    @Override
    public TSConfiguration getTSConfiguration() {
        final WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum = initializerNameClassMap.get(initializerChoiceBox.getValue());
        this.activeTSConfiguration = TSConfigurationImpl.newBuilder()
                .withSizeOfNeighborhood(sizeOfNeighborhoodIntegerTextBox.getValue())
                .withLengthOfTabuList(lengthOfTabuListIntegerTextBox.getValue())
                .withIterationsWithoutImprovementAsAdditionalAspirationCriterion(maxIterWithoutImprovementAspirationIntegerTextBox.getValue())
                .withMaxIterationsWithoutImprovementForDiversificationPurpose(maxIterWithoutImprovementDiversificationIntegerTextBox.getValue())
                .withMaxIterationsAsStopCriterion(maxNumberOfIterationsAsStopTabuSearchIntegerTextBox.getValue())
                .withWayToGenerateNeighborhood(wayToGenerateNeighborhoodEnum)
                .withUncertainFlowshop(flowShop)
                .build();

        return activeTSConfiguration;
    }

    @Override
    public void onManyIterationBatchUpdated(final SortedMap<Integer, FlowShopWithUncertainty> iterations) {
        for (Map.Entry<Integer, FlowShopWithUncertainty> entry : iterations.entrySet()) {
            onIterationUpdated(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void onIterationUpdated(int iteration, FlowShopWithUncertainty flowShop) {
        iterationFitnessMap.put(iteration, flowShop.getUpperBoundOfMinMaxRegretOptimalization());
        if (solutionIsBest(flowShop)) {
            bestSolutionIteration = iteration;
            bestSolution = flowShop;

            bestSolutionTextArea.clear();
            bestSolutionTextArea.appendText("Iteration: " + bestSolutionIteration + ":\n");
            bestSolutionTextArea.appendText("Result for UB: " + bestSolution.getUpperBoundOfMinMaxRegretOptimalization() + "\n");
            bestSolutionTextArea.appendText("Result for LB: " + bestSolution.getLowerBoundOfMinMaxRegretOptimalization() + "\n");
            bestSolutionTextArea.appendText(flowShop.toString() + "\n");
        }
        logsTextArea.appendText("Iteration " + iteration + ":\n");
        logsTextArea.appendText("Result for UB: " + iterationFitnessMap.get(iteration) + "\n");
        logsTextArea.appendText(flowShop.toString() + "\n");

        updateChart(iteration);
        updateProgressBar(iteration);
    }

    @Override
    public void onAlgorithmStarted() {
        if(flowShop != null)
        {
            algorithmState = AlgorithmState.RUNNING;
            startStopButton.setText("Stop");
            bestSolutionIteration = -1;
            bestSolution = null;
            logsTextArea.clear();
            bestSolutionTextArea.clear();
            clearChart();
            disableEditingConfiguration();
        }
    }

    @Override
    public void onAlgorithmEnded(AlgorithmEventDispatcher.EndingReason reason, double elapsedTime, FlowShopWithUncertainty flowShopWithUncertainty, double initialTemperature) {
        algorithmState = AlgorithmState.STOPPED;
        startStopButton.setText("Start");
        enableEditingConfiguration();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd-HH.mm-ss.SSS");
        final String timestamp = simpleDateFormat.format(new Date());

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator(',');
        NumberFormat formatter = new DecimalFormat("#0.00");
        formatter.setGroupingUsed(false);

        if(selectedDirectory != null)
        {
            final String absolutePath = selectedDirectory.getAbsolutePath();
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new File(absolutePath + "/" +
                        "n" + flowShopWithUncertainty.getN() +
                        " m" + flowShopWithUncertainty.getM() +
                        "_research_" + timestamp +".csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showExceptionPopup();
            }

            if(pw != null)
            {
                StringBuilder sb = new StringBuilder();

                sb.append("SA solution lower bound: ");
                sb.append(';');
                sb.append(flowShopWithUncertainty.getLowerBoundOfMinMaxRegretOptimalization());
                sb.append(';');
                sb.append("SA solution upper bound: ");
                sb.append(';');
                sb.append(flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization());
                sb.append(';');
                sb.append("Calculation time: ");
                sb.append(';');
                sb.append(formatter.format(elapsedTime));
                sb.append('\n');

                pw.write(sb.toString());
                pw.close();
                System.out.println("done!");
            }
        }

        switch (reason) {
            case ALL_ITERATIONS:
                logsTextArea.appendText("The initial temperature was: " + formatter.format(initialTemperature) + "\nEnded: All iterations executed. Elapsed time: " + formatter.format(elapsedTime) + " seconds\n");
                final String bestSolutionTextAreaText = bestSolutionTextArea.getText();
                bestSolutionTextArea.setText("The initial temperature was: " + formatter.format(initialTemperature) + "\nElapsed time: " + formatter.format(elapsedTime) + " seconds\n" + bestSolutionTextAreaText);
                break;
            case CANCELLED:
                logsTextArea.appendText("The initial temperature was: " + formatter.format(initialTemperature) + "\nEnded: User cancelled." + "\n");
                break;
            case WITHOUT_PROGRESS:
                logsTextArea.appendText("The initial temperature was: " + formatter.format(initialTemperature) + "\nEnded: " + /**activeSAConfiguration.getMaxIterationsWithoutImprovement() +*/ " iterations without improvement" + "\n");
                break;
        }
    }

    @Override
    public void onAlgorithmEnded(AlgorithmEventDispatcher.EndingReason reason, double elapsedTime, FlowShopWithUncertainty flowShopWithUncertainty) {
        algorithmState = AlgorithmState.STOPPED;
        startStopButton.setText("Start");
        enableEditingConfiguration();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd-HH.mm-ss.SSS");
        final String timestamp = simpleDateFormat.format(new Date());

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator(',');
        NumberFormat formatter = new DecimalFormat("#0.00");
        formatter.setGroupingUsed(false);

        if(selectedDirectory != null)
        {
            final String absolutePath = selectedDirectory.getAbsolutePath();
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new File(absolutePath + "/" +
                        "n" + flowShopWithUncertainty.getN() +
                        " m" + flowShopWithUncertainty.getM() +
                        "_research_" + timestamp +".csv"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showExceptionPopup();
            }

            if(pw != null)
            {
                StringBuilder sb = new StringBuilder();

                sb.append("TS solution lower bound: ");
                sb.append(';');
                sb.append(flowShopWithUncertainty.getLowerBoundOfMinMaxRegretOptimalization());
                sb.append(';');
                sb.append("TS solution upper bound: ");
                sb.append(';');
                sb.append(flowShopWithUncertainty.getUpperBoundOfMinMaxRegretOptimalization());
                sb.append(';');
                sb.append("Calculation time: ");
                sb.append(';');
                sb.append(formatter.format(elapsedTime));
                sb.append('\n');

                pw.write(sb.toString());
                pw.close();
                System.out.println("done!");
            }
        }

        switch (reason) {
            case ALL_ITERATIONS:
//                logsTextArea.appendText("The initial temperature was: " + formatter.format(initialTemperature) + "\nEnded: All iterations executed. Elapsed time: " + formatter.format(elapsedTime) + " seconds\n");
//                final String bestSolutionTextAreaText = bestSolutionTextArea.getText();
//                bestSolutionTextArea.setText("The initial temperature was: " + formatter.format(initialTemperature) + "\nElapsed time: " + formatter.format(elapsedTime) + " seconds\n" + bestSolutionTextAreaText);
                break;
            case CANCELLED:
//                logsTextArea.appendText("The initial temperature was: " + formatter.format(initialTemperature) + "\nEnded: User cancelled." + "\n");
                break;
            case WITHOUT_PROGRESS:
//                logsTextArea.appendText("The initial temperature was: " + formatter.format(initialTemperature) + "\nEnded: " + /**activeSAConfiguration.getMaxIterationsWithoutImprovement() +*/ " iterations without improvement" + "\n");
                break;
        }
    }

    private void showExceptionPopup() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setScene(new Scene(VBoxBuilder.create().
                children(new Text("An FileNotFoundException occurs..."), new Button("Got it")).
                alignment(Pos.CENTER).padding(new Insets(5)).build()));
        dialogStage.show();
    }

    private void clearChart() {
        iterationFitnessMap.clear();
        series.getData().clear();
        if(isSANotTSChosen)
        {
            ((NumberAxis) lineChart.getXAxis()).setUpperBound((double) activeSAConfiguration.getMaxNumberOfIterations());
        }
        else
        {
            ((NumberAxis) lineChart.getXAxis()).setUpperBound((double) activeTSConfiguration.getMaxIterationsAsStopCriterion());
        }
    }

    private void updateChart(int iteration) {
        if(isChartUpdateAllowed)
            series.getData().add(new XYChart.Data<>(iteration, iterationFitnessMap.get(iteration)));
    }

    private void prepareSABinding() {
        firstSAProperty = Bindings.multiply(desiredInitialAcceptanceProbabilityDoubleTextBox0To1.doubleProperty(), decayRateDoubleTextBox0To1.doubleProperty());
        secondSAProperty = Bindings.multiply(epocheLengthIntegerTextBox.integerProperty(), errorThresholdDoubleTextBox0To1.doubleProperty());
        thirdSAProperty = Bindings.multiply(samplesCardinalityIntegerTextBox.integerProperty(), maxNumberOfIterationsIntegerTextBox.integerProperty());
        fourthSAProperty = Bindings.multiply(firstSAProperty, secondSAProperty);
        fifthSAProperty = Bindings.multiply(fourthSAProperty, thirdSAProperty);
        fifthSAProperty.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                if(firstSAProperty.greaterThan(0).getValue())
                {
                    if(flowShop != null)
                    {
                        if (flowShop.getTaskCount() * flowShop.getM() <= MAXIMUM_NUMBER_OF_OPERATIONS)
                        {
                            startStopButton.setDisable(false);
                        }
                    }
                }
            }
        });
    }

    private void prepareTSBinding() {
        firstTSProperty = Bindings.multiply(sizeOfNeighborhoodIntegerTextBox.integerProperty(), lengthOfTabuListIntegerTextBox.integerProperty());
        secondTSProperty = Bindings.multiply(maxIterWithoutImprovementAspirationIntegerTextBox.integerProperty(), maxIterWithoutImprovementDiversificationIntegerTextBox.integerProperty());
        thirdTSProperty = Bindings.multiply(maxNumberOfIterationsAsStopTabuSearchIntegerTextBox.integerProperty(), secondTSProperty);
        fourthTSProperty = Bindings.multiply(firstTSProperty, thirdTSProperty);
        fourthTSProperty.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                if(fourthTSProperty.greaterThan(0).getValue())
                {
                    if(flowShop != null)
                    {
                        if (flowShop.getTaskCount() * flowShop.getM() <= MAXIMUM_NUMBER_OF_OPERATIONS)
                        {
                            startStopButton.setDisable(false);
                        }
                    }
                }
            }
        });
    }

    private void prepareMaxProblemBinding() {
        maxProblemProperty = Bindings.multiply(taskCount.integerProperty(), machineCount.integerProperty());
        maxProblemProperty.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                if(maxProblemProperty.greaterThan(MAXIMUM_NUMBER_OF_OPERATIONS).getValue())
                {
                    startStopButton.setDisable(true);
                }
                else
                {
                    startStopButton.setDisable(false);
                }
            }
        });
    }

    private void enableEditingConfiguration() {
        setEditingAlgorithmsOptionsConfiguration(false);
        setEditingInputAndOutputConfiguration(false);

    }

    private void disableEditingConfiguration() {
        setEditingAlgorithmsOptionsConfiguration(true);
        setEditingInputAndOutputConfiguration(true);
    }

    private void enableEditingPartOfConfiguration() {
        setEditingAlgorithmsOptionsConfiguration(false);

    }

    private void disableEditingPartOfConfiguration() {//TODO rename
        setEditingAlgorithmsOptionsConfiguration(true);
    }

    private void setEditingAlgorithmsOptionsConfiguration(boolean isNotEnabled) {
        desiredInitialAcceptanceProbabilityDoubleTextBox0To1.setDisable(isNotEnabled);
        epocheLengthIntegerTextBox.setDisable(isNotEnabled);
        decayRateDoubleTextBox0To1.setDisable(isNotEnabled);
        SA_maxIterWithoutImproDiversificationIntegerTextBox.setDisable(isNotEnabled);
        errorThresholdDoubleTextBox0To1.setDisable(isNotEnabled);
        samplesCardinalityIntegerTextBox.setDisable(isNotEnabled);
        maxNumberOfIterationsIntegerTextBox.setDisable(isNotEnabled);
//        cutOffEnergyLevelDoubleTextBox.setDisable(isNotEnabled);
        initializerChoiceBox.setDisable(isNotEnabled);
//        algorithmChoiceBox.setDisable(isNotEnabled); TODO 1
        initializerTSChoiceBox.setDisable(isNotEnabled);
        sizeOfNeighborhoodIntegerTextBox.setDisable(isNotEnabled);
        lengthOfTabuListIntegerTextBox.setDisable(isNotEnabled);
        maxIterWithoutImprovementAspirationIntegerTextBox.setDisable(isNotEnabled);
        maxIterWithoutImprovementDiversificationIntegerTextBox.setDisable(isNotEnabled);
        maxNumberOfIterationsAsStopTabuSearchIntegerTextBox.setDisable(isNotEnabled);
    }

    private void setEditingInputAndOutputConfiguration(boolean isNotEnabled)
    {
        if(selectedDirectory != null && !isNotEnabled)
        {
            generateFlowShopButton.setDisable(isNotEnabled);
        }
        chooseFolderToSaveResultButton.setDisable(isNotEnabled);
        taskCount.setDisable(isNotEnabled);
        machineCount.setDisable(isNotEnabled);
        K.setDisable(isNotEnabled);
        C.setDisable(isNotEnabled);
        editFlowshopManuallyButton.setDisable(isNotEnabled);
        importFlowShopFromFileButton.setDisable(isNotEnabled);
    }

    public void setSAAlgorithm(SimulatedAnnealing algorithm) {
        this.SAalgorithm = algorithm;
    }

    private void updateProgressBar(int iteration) {

        if(isSANotTSChosen)
        {
            if(iteration > activeSAConfiguration.getMaxNumberOfIterations())
                return;
            progressLabel.setText(String.valueOf(iteration) + "/" + activeSAConfiguration.getMaxNumberOfIterations());
            progressBar.setProgress((double) iteration / activeSAConfiguration.getMaxNumberOfIterations());
        }
        else
        {
            if(iteration > activeTSConfiguration.getMaxIterationsAsStopCriterion())
                return;
            progressLabel.setText(String.valueOf(iteration) + "/" + activeTSConfiguration.getMaxIterationsAsStopCriterion());
            progressBar.setProgress((double) iteration / activeTSConfiguration.getMaxIterationsAsStopCriterion());
        }
    }


    private boolean solutionIsBest(FlowShopWithUncertainty flowShop) {
        return bestSolutionIteration == -1 || bestSolution.getUpperBoundOfMinMaxRegretOptimalization() > flowShop.getUpperBoundOfMinMaxRegretOptimalization();
    }

    @FXML
    public void chooseAFolderToSaveButton(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose a folder to save the result");
        if(defaultDirectory == null)
        {
            defaultDirectory = new File("c:/");
        }
        chooser.setInitialDirectory(defaultDirectory);
        final File folder = chooser.showDialog(chooseFolderToSaveResultButton.getScene().getWindow());

        if (folder != null) {
            selectedDirectory = folder;
            generateFlowShopButton.setDisable(false);
            defaultDirectory = folder;
        }
    }

    @FXML
    public void generateUncertainFlowShopButton(ActionEvent event) {
        InstanceGenerator instanceGenerator = new InstanceGenerator(machineCount.getValue(), taskCount.getValue());
        final FlowShopWithUncertainty flowShopWithUncertainty = instanceGenerator.generateUncertainFlowShopInstance(0, K.getValue(), C.getValue());

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd-HH.mm-ss.SSS");
        final String timestamp = simpleDateFormat.format(new Date());
        final String fileName = "m" + machineCount.getValue() + "_n" + taskCount.getValue() + "_uncertainFlowShop_" + timestamp + ".txt";
        flowShopWithUncertainty.toFile(fileName, Paths.get(selectedDirectory.getAbsolutePath()));
    }

    public void setTSAlgorithm(TabuSearch TSAlgorithm) {
        this.TSAlgorithm = TSAlgorithm;
    }

    private enum AlgorithmState {
        RUNNING, STOPPED
    }

//    private static void measureSAUpperBound(SimulatedAnnealing simulatedAnnealing, StringBuilder sb) {
//        final String[] measureSAResults = measureSA(simulatedAnnealing, false); //jak tu false tzn ?e delty temp b?d? liczone dla upper
//
//
//    }
}
