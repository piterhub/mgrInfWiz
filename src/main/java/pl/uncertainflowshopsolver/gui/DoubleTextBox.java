package pl.uncertainflowshopsolver.gui;

import com.sun.deploy.util.StringUtils;
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
    private final Pattern doublePattern = Pattern.compile("[0-9]+|[\\.\\,]|[0-9]+[\\.\\,]([0-9]+)?");//general first
    private final Pattern secondDoublePattern = Pattern.compile("[0-9]+[\\,]([0-9]+)?");//to replace , with .
    private final Pattern thirdDoublePattern = Pattern.compile("[0-9]+[\\.\\,]");//to add "0" to x._ or x,_
    private final Pattern fourthDoublePattern = Pattern.compile("[0]([\\.\\,])?([0-9]+)?");//to allow only values < 1
    private DoubleProperty doubleProperty;

    public DoubleTextBox() {
        doubleProperty = new SimpleDoubleProperty();
        doubleProperty.setValue(0.0);
        this.insertText(0, "0.0");

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
        if (fourthDoublePattern.matcher(text).matches()) {
            super.insertText(0, text);
            doubleProperty.setValue(Double.parseDouble(this.getText()));
        }
//        String oldValue = getText();
//        if (doublePattern.matcher(text).matches()) {
//            super.insertText(0, text);
//            String newText = super.getText();
//            if (!doublePattern.matcher(newText).matches()) {
//                super.setText(oldValue);
//            }
//            else{
//                doubleProperty.setValue(Double.parseDouble(this.getText()));
//            }
//        }
    }

    @Override
    public void replaceSelection(String text) {
        String oldValue = getText();
        if (fourthDoublePattern.matcher(text).matches()) {
            super.replaceSelection(text);
            String newText = super.getText();
            if (!fourthDoublePattern.matcher(newText).matches()) {
                super.setText(oldValue);
            }
            else{
                String helper = this.getText();
                if(secondDoublePattern.matcher(helper).matches())
                {
                    org.apache.commons.lang3.StringUtils.replace(helper, ",", ".");
                }
                if(thirdDoublePattern.matcher(helper).matches())
                {
                    helper = helper + "0";
                }
                doubleProperty.setValue(Double.parseDouble(helper));
            }
        }
        else if (text == "")
        {
            super.replaceSelection("0.0");
            doubleProperty.setValue(Double.parseDouble("0.0"));
        }
    }

    @Override
    public void replaceText(int start, int end, String text) {
        String oldValue = getText();
        if (doublePattern.matcher(text).matches()) {
            super.replaceText(start, end, text);
            String newText = super.getText();
            if (!doublePattern.matcher(newText).matches()) {
                super.setText(oldValue);
            }
            else if(fourthDoublePattern.matcher(newText).matches())
            {
                String helper = this.getText();
                if(secondDoublePattern.matcher(helper).matches())
                {
                    helper = helper.replace(',', '.');
                }
                if(thirdDoublePattern.matcher(helper).matches())
                {
                    helper = helper + "0";
                }
                doubleProperty.setValue(Double.parseDouble(helper));
//                doubleProperty.setValue(Double.parseDouble(this.getText()));
            }
            else{
                super.setText(oldValue);
            }
        }
        else if(text == "")
        {
            super.replaceText(start, end, text);
            String helper = this.getText();
            if(helper == "")
            {
                super.insertText(0, "0.0");
                doubleProperty.setValue(Double.parseDouble("0.0"));
                return;
            }
            if(secondDoublePattern.matcher(helper).matches())
            {
                helper = helper.replace(',', '.');
            }
            if(thirdDoublePattern.matcher(helper).matches())
            {
                helper = helper + "0";
            }
            doubleProperty.setValue(Double.parseDouble(helper));
        }
    }

    @Override
    public void replaceText(IndexRange range, String text) {
        String oldValue = getText();
        if (doublePattern.matcher(text).matches()) {
            super.replaceText(range, text);
            String newText = super.getText();
            if (!doublePattern.matcher(newText).matches()) {
                super.setText(oldValue);
            }
            else{
                doubleProperty.setValue(Double.parseDouble(this.getText()));
            }
        }
    }

    public DoubleProperty doubleProperty() {
        return doubleProperty;
    }


}
