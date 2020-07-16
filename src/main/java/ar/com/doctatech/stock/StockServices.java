package ar.com.doctatech.stock;

import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.stock.ingredient.IngredientDAO;
import ar.com.doctatech.stock.ingredient.IngredientDAOMySQL;
import ar.com.doctatech.stock.ingredient.Unit;
import javafx.scene.Node;
import javafx.scene.control.*;
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

        dialog.setTitle("Añade stock de tu ingrediente");

        dialog.setHeaderText("Ingresa el stock que quieres añadir");
        dialog.setResizable(true);

        Label labelDescription = new Label("Stock por añadir: ");
        TextField textFieldStock = new TextField("0");
        ComboBox<Unit> comboBoxUnits = new ComboBox<>();
        comboBoxUnits.setItems(
                observableArrayList( Unit.values() )
        );
        comboBoxUnits.getSelectionModel().select(Unit.getUnit( ingredient.getUnit() ));
        comboBoxUnits.setDisable(true);

        Text textDiv = new Text("0 " + getDivUnit(comboBoxUnits));

        GridPane grid = new GridPane();
        grid.add(labelDescription, 1,1);
        grid.add(textFieldStock,2,1);
        grid.add(comboBoxUnits, 3, 1);
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
            else
                {
                addStockButton.setDisable(newValue.trim().isEmpty());

            }
            updateDiv1000(textFieldStock,textDiv,comboBoxUnits);
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
