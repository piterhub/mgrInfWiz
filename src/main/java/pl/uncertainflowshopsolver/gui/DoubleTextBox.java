package pl.uncertainflowshopsolver.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.regex.Pattern;

public class DoubleTextBox extends TextField {
    private final Pattern intPattern = Pattern.compile("[0-9]+");
    private DoubleProperty doubleProperty;

    public DoubleTextBox() {
        doubleProperty = new SimpleDoubleProperty();
        doubleProperty.setValue(0);
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

    public DoubleTextBox(String s) {
        super(s);
    }

    public double getValue() {
        return doubleProperty.getValue();
    }

    @Override
    public void insertText(int i, String text) {
        if (intPattern.matcher(text).matches()) {
            super.insertText(0, text);
            doubleProperty.setValue(Double.parseDouble(this.getText()));
        }
    }

    @Override
    public void replaceSelection(String text) {
        if (intPattern.matcher(text).matches()) {
            super.replaceSelection(text);
            doubleProperty.setValue(Double.parseDouble(this.getText()));
        }
    }

    @Override
    public void replaceText(int start, int end, String text) {
        if (intPattern.matcher(text).matches()) {
            super.replaceText(start, end, text);
            doubleProperty.setValue(Double.parseDouble(this.getText()));
        }
    }

    @Override
    public void replaceText(IndexRange range, String text) {
        if (intPattern.matcher(text).matches()) {
            super.replaceText(range, text);
            doubleProperty.setValue(Double.parseDouble(this.getText()));
        }
    }

    public DoubleProperty doubleProperty() {
        return doubleProperty;
    }


}
