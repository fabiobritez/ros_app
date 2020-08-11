package ar.com.doctatech.ingredient;

import ar.com.doctatech.ingredient.model.Ingredient;
import ar.com.doctatech.ingredient.model.IngredientDAO;
import ar.com.doctatech.ingredient.model.IngredientDAOMySQL;
import ar.com.doctatech.ingredient.model.Unit;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.util.Optional;

import static javafx.collections.FXCollections.observableArrayList;


public class StockServices
{
    IngredientDAO ingredientDAO;

    {
        ingredientDAO = new IngredientDAOMySQL();
    }

    protected int getStockDialog(Ingredient ingredient)
    {
        Dialog<Integer> dialog = new Dialog<>();

        dialog.setTitle("Agrega stock");

        dialog.setHeaderText("Ingresa cantidad a añadir");
        dialog.setResizable(true);

        GridPane grid = new GridPane();
        Label labelDescription = new Label("Añadir: ");
        TextField textFieldStock = new TextField("0");
        Label labelUnit = new Label(ingredient.getUnit());
        Text textDiv = new Text("0 " + ingredient.getUnitDiv());

        grid.add(labelDescription, 1,1);
        grid.add(textFieldStock,2,1);
        grid.add(labelUnit, 3, 1);
        grid.add(textDiv,2,2);
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeAdd = new ButtonType("Agregar", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeAdd);

        Node addStockButton = dialog.getDialogPane().lookupButton(buttonTypeAdd);
        addStockButton.setDisable(true);

        textFieldStock.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[0-9]*"))
            {
                textFieldStock.setText(oldValue);
                addStockButton.setDisable(
                        oldValue.trim().isEmpty()
                );
            }
            else addStockButton.setDisable(newValue.trim().isEmpty());
            updateDiv1000(textFieldStock,textDiv,ingredient.getUnitDiv());
        });

        textFieldStock.setOnKeyPressed(event ->
        {
            String sValue =  textFieldStock.getText().trim();
            if(event.getCode() == KeyCode.ENTER && !sValue.isEmpty())
            {
                dialog.setResult( Integer.parseInt(sValue) );
                dialog.close();
            }
        });

        dialog.setResultConverter(buttonType ->
        {
            if(buttonType == buttonTypeAdd)
            {
                return Integer.parseInt( textFieldStock.getText().trim() );
            }
            return null;
        });

        Optional<Integer> result = dialog.showAndWait();

        return result.orElse(0);
    }

    private void updateDiv1000(TextField textField, Text text,String unitDiv)
    {
        if(!unitDiv.equals("unidades"))
            if (!textField.getText().isEmpty()) {
                double stockmin = Integer.parseInt(textField.getText());
                text.setText(String.format("%.2f", stockmin / 1000) + " " + unitDiv);
            } else text.setText("0.00 " + unitDiv);
        else
            text.setText(textField.getText() + " unidades");
    }


    protected void updateDiv1000(TextField textField, Text text, ComboBox<Unit> comboboxUnit)
    {
        if(!getDivUnit(comboboxUnit).equals("unidades"))
        {
            if(!textField.getText().isEmpty())
            {
                double stockmin = Integer.parseInt(textField.getText());
                text.setText(String.format("%.2f", stockmin / 1000) +" "+ getDivUnit(comboboxUnit));
            }else
            {
                text.setText("0.00" + getDivUnit(comboboxUnit));
            }
        }else
        {
            text.setText(textField.getText() + " unidades");
        }
    }

    private String getDivUnit(ComboBox<Unit> comboboxUnit)
    {
        if(comboboxUnit.getSelectionModel().getSelectedItem().equals(Unit.GRAMS))
        {
            return "kg";
        }else if(comboboxUnit.getSelectionModel().getSelectedItem().equals(Unit.MILLILITERS))
        {
            return "litros";
        }
        return "unidades";
    }

}
