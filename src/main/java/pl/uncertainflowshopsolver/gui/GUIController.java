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
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.algo.util.WayToGenerateNeighborhoodEnum;
import pl.uncertainflowshopsolver.algo.util.WhichAlgorithmEnum;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.impl.SAConfigurationImpl;
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
    public ChoiceBox<String> initializerChoiceBox;
    public DoubleTextBox desiredInitialAcceptanceProbabilityDoubleTextBox;
    public IntegerTextBox epocheLengthIntegerTextBox;
    public DoubleTextBox decayRateDoubleTextBox;
    public DoubleTextBox endTemperatureDoubleTextBox;
    public DoubleTextBox errorThresholdDoubleTextBox;
    public IntegerTextBox samplesCardinalityIntegerTextBox;
    public IntegerTextBox maxNumberOfIterationsIntegerTextBox;
//    public IntegerTextBox maxIterationsWithoutImprovementIntegerTextBox;
//    public DoubleTextBox cutOffEnergyLevelDoubleTextBox;
    public Label totalBeesLabel;
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
    public TitledPane algorithmOptionsTitledPane;
    public TitledPane chartTitledPane;
    private NumberBinding thirdProperty;
    private NumberBinding fourthProperty;
    private NumberBinding fifthProperty;
    private File defaultDirectory;

    public FlowShopWithUncertainty getFlowShop() {
        return flowShop;
    }

    private FlowShopWithUncertainty flowShop;

    private Map<String, WayToGenerateNeighborhoodEnum> initializerNameClassMap;
    private Map<String, WhichAlgorithmEnum> algorithmEnumMap;

    public LineChart lineChart;
    private XYChart.Series<Integer, Integer> series = new XYChart.Series<Integer, Integer>();

    private SAConfiguration activeSAConfiguration;
    private SimulatedAnnealing algorithm;

    private AlgorithmState algorithmState = AlgorithmState.STOPPED;
    private Thread algorithmThread;

    private Map<Integer, Integer> iterationFitnessMap = Collections.synchronizedMap(new HashMap<Integer, Integer>());
    private int bestSolutionIteration = -1;
    private FlowShopWithUncertainty bestSolution;

    private NumberBinding firstProperty;
    private NumberBinding secondProperty;
    private File selectedDirectory;

    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializerNameClassMap = new HashMap<>();
        initializerNameClassMap.put("Swap", WayToGenerateNeighborhoodEnum.SWAP);
        initializerNameClassMap.put("Insert", WayToGenerateNeighborhoodEnum.INSERT);
        initializerNameClassMap.put("Fischer-Yates shuffle", WayToGenerateNeighborhoodEnum.FISCHER_YATES_SHUFFLE);
        initializerChoiceBox.setItems(FXCollections.observableArrayList(initializerNameClassMap.keySet()));
        initializerChoiceBox.getSelectionModel().select(0);

        algorithmEnumMap = new HashMap<>();
        algorithmEnumMap.put("Simulated Annealing", WhichAlgorithmEnum.SIMULATED_ANNEALING);
        algorithmEnumMap.put("MIH", WhichAlgorithmEnum.MIH);
        algorithmEnumMap.put("Tabu Search", WhichAlgorithmEnum.TABU_SEARCH);
        algorithmChoiceBox.setItems(FXCollections.observableArrayList(algorithmEnumMap.keySet()));
        algorithmChoiceBox.getSelectionModel().select(0);
        algorithmChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                if(flowShop != null)
                {
                    if(algorithmChoiceBox.getItems().get((Integer) number2) == "MIH")
                    {
                        startStopButton.setDisable(false);
                        algorithmOptionsTitledPane.setDisable(true);
                        chartTitledPane.setDisable(true);
                    }
                    else if(algorithmChoiceBox.getItems().get((Integer) number2) == "Simulated Annealing")
                    {
                        startStopButton.setDisable(true);
                        algorithmOptionsTitledPane.setDisable(false);
                        chartTitledPane.setDisable(false);
                    }
                    //TODO Tabu Search
                }
            }
        });

        maxNumberOfIterationsIntegerTextBox.integerProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                progressLabel.setText("0/" + newValue.intValue());
                progressBar.setProgress(0.0);
                if(maxNumberOfIterationsIntegerTextBox.integerProperty().getValue() > 2000)
                    lineChart.setDisable(true);
                else
                    lineChart.setDisable(false);
            }
        });

        lineChart.getXAxis().setAutoRanging(false);
        lineChart.getData().add(series);
        lineChart.setAnimated(false);

        startStopButton.setDisable(true);
        generateFlowShopButton.setDisable(true);
        disableEditingPartOfConfiguration();
        prepareTotalBeesBinding();

        this.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("FlowShopChosen")) {
                    enableEditingPartOfConfiguration();
                }
            }
        });
//
//        desiredInitialAcceptanceProbabilityDoubleTextBox.doubleProperty().set(0.925);
//        epocheLengthIntegerTextBox.insertText(0, "10");
//        decayRateDoubleTextBox.insertText(0, "0.995");
//        endTemperatureDoubleTextBox.insertText(0, "0.5");
//        errorThresholdDoubleTextBox.insertText(0, "0.0001");
//        samplesCardinalityIntegerTextBox.insertText(0, "10000");
//        maxNumberOfIterationsIntegerTextBox.insertText(0, "1000");
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @FXML
    public void onStartStopButton(ActionEvent actionEvent) {
        activeSAConfiguration = getSAConfiguration();
        if (algorithmState == AlgorithmState.STOPPED) {
            if (flowShop != null) {
                algorithmThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        algorithm.start();
                    }
                });
                algorithmThread.setDaemon(true);
                algorithmThread.start();
            }
        } else {
            algorithm.stop();
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
                final Double value = desiredInitialAcceptanceProbabilityDoubleTextBox.doubleProperty().getValue();
                String valueString = value+"";
                int count = valueString.length();
                desiredInitialAcceptanceProbabilityDoubleTextBox.replaceText(0, count, valueString);
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
        final Double value = desiredInitialAcceptanceProbabilityDoubleTextBox.doubleProperty().getValue();
        String valueString = value+"";
        int count = valueString.length();
        desiredInitialAcceptanceProbabilityDoubleTextBox.replaceText(0, count, valueString);
        propertyChangeSupport.firePropertyChange("FlowShopChosen",oldFlowShop, flowShop);
    }

    @Override
    public SAConfiguration getSAConfiguration() {
        final WayToGenerateNeighborhoodEnum wayToGenerateNeighborhoodEnum = initializerNameClassMap.get(initializerChoiceBox.getValue());
        this.activeSAConfiguration = SAConfigurationImpl.newBuilder()
                .withDesiredInitialAcceptanceProbability(desiredInitialAcceptanceProbabilityDoubleTextBox.getValue())
                .withEpocheLength(epocheLengthIntegerTextBox.getValue())
                .withDecayRate(decayRateDoubleTextBox.getValue())
                .withEndTemperature(endTemperatureDoubleTextBox.getValue())
                .withErrorThreshold(errorThresholdDoubleTextBox.getValue())
                .withSamplesCardinality(samplesCardinalityIntegerTextBox.getValue())
                .withMaxNumberOfIterations(maxNumberOfIterationsIntegerTextBox.getValue())
//                .withCutOffEnergyLevel(cutOffEnergyLevelDoubleTextBox.getValue())
                .withWayToGenerateNeighborhood(wayToGenerateNeighborhoodEnum)
                .withUncertainFlowshop(flowShop)
                .build();

        return activeSAConfiguration;
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
        ((NumberAxis) lineChart.getXAxis()).setUpperBound((double) activeSAConfiguration.getMaxNumberOfIterations());
    }

    private void updateChart(int iteration) {
        series.getData().add(new XYChart.Data<>(iteration, iterationFitnessMap.get(iteration)));
    }

    private void prepareTotalBeesBinding() {
        firstProperty = Bindings.multiply(desiredInitialAcceptanceProbabilityDoubleTextBox.doubleProperty(), decayRateDoubleTextBox.doubleProperty());
        secondProperty = Bindings.multiply(epocheLengthIntegerTextBox.integerProperty(), errorThresholdDoubleTextBox.doubleProperty());
        thirdProperty = Bindings.multiply(samplesCardinalityIntegerTextBox.integerProperty(), maxNumberOfIterationsIntegerTextBox.integerProperty());
        fourthProperty = Bindings.multiply(firstProperty, secondProperty);
        fifthProperty = Bindings.multiply(fourthProperty, thirdProperty);
        //TODO jak maxNumberOfIterationsIntegerTextBox.integerProperty() > 2000 to wy??cza? chart'a!
        fifthProperty.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                if(firstProperty.greaterThan(0).getValue())
                {
                    if(flowShop != null)
                        startStopButton.setDisable(false);
                }
            }
        });
    }

    private void enableEditingConfiguration() {
        setEditingPartOfConfiguration(false);
        setEditingSecondPartOfConfiguration(false);

    }

    private void disableEditingConfiguration() {
        setEditingPartOfConfiguration(true);
        setEditingSecondPartOfConfiguration(true);
    }

    private void enableEditingPartOfConfiguration() {
        setEditingPartOfConfiguration(false);

    }

    private void disableEditingPartOfConfiguration() {
        setEditingPartOfConfiguration(true);
    }

    private void setEditingPartOfConfiguration(boolean isNotEnabled) {
        desiredInitialAcceptanceProbabilityDoubleTextBox.setDisable(isNotEnabled);
        epocheLengthIntegerTextBox.setDisable(isNotEnabled);
        decayRateDoubleTextBox.setDisable(isNotEnabled);
        endTemperatureDoubleTextBox.setDisable(isNotEnabled);
        errorThresholdDoubleTextBox.setDisable(isNotEnabled);
        samplesCardinalityIntegerTextBox.setDisable(isNotEnabled);
        maxNumberOfIterationsIntegerTextBox.setDisable(isNotEnabled);
//        cutOffEnergyLevelDoubleTextBox.setDisable(isNotEnabled);
        initializerChoiceBox.setDisable(isNotEnabled);
        algorithmChoiceBox.setDisable(isNotEnabled);
    }

    private void setEditingSecondPartOfConfiguration(boolean isNotEnabled)
    {
        generateFlowShopButton.setDisable(isNotEnabled);
        chooseFolderToSaveResultButton.setDisable(isNotEnabled);
        taskCount.setDisable(isNotEnabled);
        machineCount.setDisable(isNotEnabled);
        K.setDisable(isNotEnabled);
        C.setDisable(isNotEnabled);
        editFlowshopManuallyButton.setDisable(isNotEnabled);
        importFlowShopFromFileButton.setDisable(isNotEnabled);
    }

    public void setSAAlgorithm(SimulatedAnnealing algorithm) {
        this.algorithm = algorithm;
    }

    private void updateProgressBar(int iteration) {
        progressLabel.setText(String.valueOf(iteration) + "/" + activeSAConfiguration.getMaxNumberOfIterations());
        progressBar.setProgress((double) iteration / activeSAConfiguration.getMaxNumberOfIterations());
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

    private enum AlgorithmState {
        RUNNING, STOPPED
    }

//    private static void measureSAUpperBound(SimulatedAnnealing simulatedAnnealing, StringBuilder sb) {
//        final String[] measureSAResults = measureSA(simulatedAnnealing, false); //jak tu false tzn ?e delty temp b?d? liczone dla upper
//
//
//    }
}
