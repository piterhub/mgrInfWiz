package pl.uncertainflowshopsolver.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.regex.Pattern;

public class IntegerTextBox extends TextField {
    private final Pattern intPattern = Pattern.compile("[0-9]+");
    private IntegerProperty integerProperty;

    public IntegerTextBox() {
        integerProperty = new SimpleIntegerProperty();
        integerProperty.setValue(0);
        this.insertText(0, "0");

        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean isFocuedNow) {
                if (isFocuedNow) {
                    selectPositionCaret(0);
                    selectAll();
                }
            }
        });

        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                selectPositionCaret(0);
                selectAll();
            }
        });
    }

    public IntegerTextBox(String s) {
        super(s);
    }

    public int getValue() {
        return integerProperty.getValue();
    }

    @Override
    public void insertText(int i, String text) {
        if (intPattern.matcher(text).matches()) {
            super.insertText(0, text);
            integerProperty.setValue(Integer.parseInt(this.getText()));
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (intPattern.matcher(text).matches()) {
            super.replaceSelection(text);
            integerProperty.setValue(Integer.parseInt(this.getText()));
        }
    }

    @Override
    public void replaceText(int start, int end, String text) {
        if (intPattern.matcher(text).matches()) {
            super.replaceText(start, end, text);
            integerProperty.setValue(Integer.parseInt(this.getText()));
        }
    }

    @Override
    public void replaceText(IndexRange range, String text) {
        if (intPattern.matcher(text).matches()) {
            super.replaceText(range, text);
            integerProperty.setValue(Integer.parseInt(this.getText()));
        }
    }

    public IntegerProperty integerProperty() {
        return integerProperty;
    }


}
