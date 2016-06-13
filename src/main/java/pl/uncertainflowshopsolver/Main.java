package pl.uncertainflowshopsolver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pl.uncertainflowshopsolver.algo.SimulatedAnnealing;
import pl.uncertainflowshopsolver.algo.TabuSearch;
import pl.uncertainflowshopsolver.gui.GUIController;

import java.io.InputStream;
import java.net.URL;

public class Main extends Application {
    public static final String GUI_FXML_PATH = "/view.fxml";
    public static final String APPLICATION_NAME = "Solver of Flow Shop problem with tasks intervals uncertainty";
    public static final int GUI_WIDTH = 1400;
    public static final int GUI_HEIGHT = 700;
    private GUIController guiController;
    private Scene mainScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(GUI_FXML_PATH));
        Parent root = (Parent) loader.load();
        guiController = loader.getController();

        prepareAlgorithm();

        ClassLoader classLoader = getClass().getClassLoader();
//        final URL iconURL = classLoader.getResource("/icon.png");
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("/icon.png");//.getResource("/icon.png");
        primaryStage.getIcons().add(new Image(resourceAsStream));
        primaryStage.setTitle(APPLICATION_NAME);
        mainScene = new Scene(root, GUI_WIDTH, GUI_HEIGHT);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public void prepareAlgorithm() {
//        BeesAlgorithm algorithm = new BeesAlgorithm(guiController);
        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(guiController);
        TabuSearch tabuSearch = new TabuSearch(guiController);
        guiController.setSAAlgorithm(simulatedAnnealing);
        guiController.setTSAlgorithm(tabuSearch);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
