package pl.uncertainflowshopsolver.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pl.uncertainflowshopsolver.flowshop.FlowShopWithUncertainty;
import pl.uncertainflowshopsolver.testdata.UncertainFlowShopParser;

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
        exampleTextArea.setText("<machines> \njob 1 times\njob 2 times\n...\n\n3  \nTask0: 93|115 46|60 100|123  \nTask1: 63|104 58|60 64|72  \nTask2: 64|111 75|113 55|76  \nTask3: 15|20 18|51 45|80  \nTask4: 98|117 13|33 73|123  \nTask5: 30|45 15|33 46|80  \nTask6: 9|19 52|68 33|52  \nTask7: 88|109 91|108 29|75  \nTask8: 36|76 44|73 70|118  \nTask9: 91|131 38|50 7|39  \nTask10: 0|15 74|122 29|35");
        configurationTextArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if (newValue != "")
                    acceptButton.setDisable(false);
            }
        });
        acceptButton.setDisable(true);
    }

    @FXML
    public void onAcceptButton(ActionEvent actionEvent) {
        try{
            FlowShopWithUncertainty flowShop = UncertainFlowShopParser.parseTextToFlowShop(configurationTextArea.getText());
            guiController.setFlowShop(flowShop);
            stage.close();
        }catch (NumberFormatException ex)
        {
            ex.printStackTrace();
        }
    }

    public void setGuiController(GUIController guiController) {
        this.guiController = guiController;
        if(guiController.getFlowShop() != null)
        {
            configurationTextArea.setText(guiController.getFlowShop().getM() + "\n" + guiController.getFlowShop().toString());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
