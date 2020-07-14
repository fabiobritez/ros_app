package ar.com.doctatech.stock;

import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.stock.ingredient.IngredientDAO;
import ar.com.doctatech.stock.ingredient.IngredientDAOMySQL;
import ar.com.doctatech.stock.ingredient.Unit;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

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

        GridPane grid = new GridPane();
        grid.add(labelDescription, 1,1);
        grid.add(textFieldStock,2,1);
        grid.add(comboBoxUnits, 3, 1);

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
}
