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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.algo.init.NehInitializer;
import pl.uncertainflowshopsolver.algo.init.RandomInitializer;
import pl.uncertainflowshopsolver.algo.init.SolutionInitializer;
import pl.uncertainflowshopsolver.config.SAConfiguration;
import pl.uncertainflowshopsolver.config.ConfigurationProvider;
import pl.uncertainflowshopsolver.config.impl.SAConfigurationImpl;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventDispatcher;
import pl.uncertainflowshopsolver.gui.event.AlgorithmEventListener;

import java.io.*;
import java.net.URL;
import java.util.*;

public class GUIController implements ConfigurationProvider, AlgorithmEventListener, Initializable {
    public ChoiceBox<String> initializerChoiceBox;
    public IntegerTextBox eliteSolutionsIntegerTextBox;
    public IntegerTextBox bestSolutionsIntegerTextBox;
    public IntegerTextBox beesPerEliteSolutionIntegerTextBox;
    public IntegerTextBox beesPerBestSolutionIntegerTextBox;
    public IntegerTextBox scoutBeesIntegerTextBox;
    public IntegerTextBox iterationsWithoutImprovementIntegerTextBox;
    public IntegerTextBox maximumIterationsIntegerTextBox;
    public IntegerTextBox solutionFitnessIntegerTextBox;
    public Label totalBeesLabel;
    public Button importFlowShopFromFileButton;
    public Button editFlowshopManuallyButton;
    public ProgressBar progressBar;
    public Label progressLabel;
    public Button startStopButton;
    public TextArea logsTextArea;
    public TextArea bestSolutionTextArea;

    private FlowShopWithUncertainty flowShop;

    private Map<String, Class<? extends SolutionInitializer>> initializerNameClassMap;

    public LineChart lineChart;
    private XYChart.Series<Integer, Integer> series = new XYChart.Series<Integer, Integer>();

    private SAConfiguration activeSAConfiguration;
//    private BeesAlgorithm algorithm;
    private SimulatedAnnealing algorithm;

    private AlgorithmState algorithmState = AlgorithmState.STOPPED;
    private Thread algorithmThread;

    private Map<Integer, Integer> iterationFitnessMap = Collections.synchronizedMap(new HashMap<Integer, Integer>());
    private int bestSolutionIteration = -1;
    private FlowShopWithUncertainty bestSolution;

    private NumberBinding allBeesBinding;
    private NumberBinding eliteBeesProperty;
    private NumberBinding bestBeesProperty;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializerNameClassMap = new HashMap<String, Class<? extends SolutionInitializer>>();
        initializerNameClassMap.put("RandomInitializer", RandomInitializer.class);
        initializerNameClassMap.put("NehInitializer", NehInitializer.class);

        initializerChoiceBox.setItems(FXCollections.observableArrayList(initializerNameClassMap.keySet()));
        initializerChoiceBox.getSelectionModel().select(0);

        maximumIterationsIntegerTextBox.integerProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                progressLabel.setText("0/" + newValue.intValue());
                progressBar.setProgress(0.0);
            }
        });

        lineChart.getXAxis().setAutoRanging(false);
        lineChart.getData().add(series);
        lineChart.setAnimated(false);

        prepareTotalBeesBinding();
    }

    @FXML
    public void onStartStopButton(ActionEvent actionEvent) {
        if(maximumIterationsIntegerTextBox.getValue() == 0) {
            maximumIterationsIntegerTextBox.setText("1000");
        }
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
        File file = chooser.showOpenDialog(importFlowShopFromFileButton.getScene().getWindow());

        //TODO
//        if (file != null) {
//            try {
//                flowShop = FlowshopParser.parseFlowShop(new BufferedReader(new FileReader(file)));
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @FXML
    public void onEditFlowshopManuallyButton(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/edit_window.fxml"));
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
        this.flowShop = flowShop;
    }

    @Override
    public SAConfiguration getSAConfiguration() {
        Class<? extends SolutionInitializer> solutionInitializerClass = initializerNameClassMap.get(initializerChoiceBox.getValue());
        this.activeSAConfiguration = SAConfigurationImpl.newBuilder()
                .withEliteSolutionsNumber(eliteSolutionsIntegerTextBox.getValue())
                .withBestSolutionsNumber(bestSolutionsIntegerTextBox.getValue())
                .withBeesPerEliteSolution(beesPerEliteSolutionIntegerTextBox.getValue())
                .withBeesPerBestSolution(beesPerBestSolutionIntegerTextBox.getValue())
                .withScoutBeesNumber(scoutBeesIntegerTextBox.getValue())
                .withSolutionInitializerClass(solutionInitializerClass)
                .withMaxNumberOfIterations(maximumIterationsIntegerTextBox.getValue())
                .withMaxIterationsWithoutImprovement(iterationsWithoutImprovementIntegerTextBox.getValue())
                .withMinFitness(solutionFitnessIntegerTextBox.getValue())
                .withFlowshop(flowShop)
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
//        iterationFitnessMap.put(iteration, flowShop.makeSpan());
        iterationFitnessMap.put(iteration, flowShop.g);
        if (solutionIsBest(flowShop)) {
            bestSolutionIteration = iteration;
            bestSolution = flowShop;

            bestSolutionTextArea.clear();
            bestSolutionTextArea.appendText("Iteration: " + bestSolutionIteration + ":\n");
            bestSolutionTextArea.appendText("Makespan: " + bestSolution.makeSpan() + "\n");
            bestSolutionTextArea.appendText(flowShop.toString() + "\n");
        }
        logsTextArea.appendText("Iteration " + iteration + ":\n");
        logsTextArea.appendText("Makespan: " + iterationFitnessMap.get(iteration) + "\n");
        logsTextArea.appendText(flowShop.toString() + "\n");

        updateChart(iteration);
        updateProgressBar(iteration);
    }

    @Override
    public void onAlgorithmStarted() {
        algorithmState = AlgorithmState.RUNNING;
        startStopButton.setText("Stop");
        bestSolutionIteration = -1;
        bestSolution = null;
        logsTextArea.clear();
        bestSolutionTextArea.clear();
        clearChart();
        disableEditingConfiguration();
    }

    @Override
    public void onAlgorithmEnded(AlgorithmEventDispatcher.EndingReason reason) {
        algorithmState = AlgorithmState.STOPPED;
        startStopButton.setText("Start");
        enableEditingConfiguration();

        switch (reason) {
            case ALL_ITERATIONS:
                logsTextArea.appendText("\nEnded: All iterations executed.");
                break;
            case CANCELLED:
                logsTextArea.appendText("\nEnded: User cancelled.");
                break;
            case WITHOUT_PROGRESS:
                logsTextArea.appendText("\nEnded: " + activeSAConfiguration.getMaxIterationsWithoutImprovement() + " iterations without improvement");
                break;
        }
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
        eliteBeesProperty = Bindings.multiply(eliteSolutionsIntegerTextBox.integerProperty(), beesPerEliteSolutionIntegerTextBox.integerProperty());
        bestBeesProperty = Bindings.multiply(bestSolutionsIntegerTextBox.integerProperty(), beesPerBestSolutionIntegerTextBox.integerProperty());
        allBeesBinding = Bindings.add(Bindings.add(scoutBeesIntegerTextBox.integerProperty(), eliteBeesProperty), bestBeesProperty);
        allBeesBinding.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                totalBeesLabel.setText("Total bees: " + newValue);
            }
        });
    }

    private void enableEditingConfiguration() {
        setEditingConfiguration(false);
    }

    private void disableEditingConfiguration() {
        setEditingConfiguration(true);
    }

    private void setEditingConfiguration(boolean isEnabled) {
        eliteSolutionsIntegerTextBox.setDisable(isEnabled);
        bestSolutionsIntegerTextBox.setDisable(isEnabled);
        beesPerEliteSolutionIntegerTextBox.setDisable(isEnabled);
        beesPerBestSolutionIntegerTextBox.setDisable(isEnabled);
        scoutBeesIntegerTextBox.setDisable(isEnabled);
        iterationsWithoutImprovementIntegerTextBox.setDisable(isEnabled);
        maximumIterationsIntegerTextBox.setDisable(isEnabled);
        solutionFitnessIntegerTextBox.setDisable(isEnabled);
        editFlowshopManuallyButton.setDisable(isEnabled);
        importFlowShopFromFileButton.setDisable(isEnabled);
        initializerChoiceBox.setDisable(isEnabled);
    }

    public void setSAAlgorithm(SimulatedAnnealing algorithm) {
        this.algorithm = algorithm;
    }

    private void updateProgressBar(int iteration) {
//        progressLabel.setText(String.valueOf(iteration) + "/" + activeSAConfiguration.getMaxNumberOfIterations());
//        progressBar.setProgress((double) iteration / activeSAConfiguration.getMaxNumberOfIterations());
    }


    private boolean solutionIsBest(FlowShopWithUncertainty flowShop) {
        return bestSolutionIteration == -1 || bestSolution.makeSpan() > flowShop.makeSpan();
    }

    private enum AlgorithmState {
        RUNNING, STOPPED
    }
}