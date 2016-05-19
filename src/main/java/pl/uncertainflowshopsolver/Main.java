package pl.uncertainflowshopsolver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.gui.GUIController;

public class Main extends Application {
    public static final String GUI_FXML_PATH = "/view.fxml";
    public static final String APPLICATION_NAME = "Solver of Flow Shop problem with tasks intervals uncertainty";
    public static final int GUI_WIDTH = 1200;
    public static final int GUI_HEIGHT = 600;
    private GUIController guiController;
    private Scene mainScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(GUI_FXML_PATH));
        Parent root = (Parent) loader.load();
        guiController = loader.getController();

        prepareAlgorithm();

        primaryStage.setTitle(APPLICATION_NAME);
        mainScene = new Scene(root, GUI_WIDTH, GUI_HEIGHT);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public void prepareAlgorithm() {
        SimulatedAnnealing algorithm = new SimulatedAnnealing(guiController);
//        BeesAlgorithm algorithm = new BeesAlgorithm(guiController);
        guiController.setSAAlgorithm(algorithm);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
