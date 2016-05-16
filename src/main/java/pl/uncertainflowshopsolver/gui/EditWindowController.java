package pl.uncertainflowshopsolver.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import pl.agh.bo.algo.FlowShop;
import pl.agh.bo.parser.FlowshopParser;

import java.net.URL;
import java.util.ResourceBundle;

public class EditWindowController implements Initializable {
    public TextArea configurationTextArea;
    public Button acceptButton;
    public TextArea exampleTextArea;

    private GUIController guiController;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        exampleTextArea.setText("<jobs> <machines> upper:xx lower:yy\njob 1 times\njob 2 times\n...\n\n3 6\n5 7 3 7 4 3\n5 7 3 6 8 5\n3 5 7 4 6 7\n\nUpper i lower nie sa brane pod uwage");
    }

    @FXML
    public void onAcceptButton(ActionEvent actionEvent) {
        if (FlowshopParser.validateText(configurationTextArea.getText())) {
            FlowShop flowShop = FlowshopParser.parseFlowShop(configurationTextArea.getText());
            guiController.setFlowShop(flowShop);
            stage.close();
        }
    }

    public void setGuiController(GUIController guiController) {
        this.guiController = guiController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
