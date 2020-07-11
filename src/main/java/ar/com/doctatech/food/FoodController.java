package ar.com.doctatech.food;

import ar.com.doctatech.food.model.Recipe;
import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

public class FoodController
        extends FoodServices implements Initializable {

    //region INGREDIENT

    @FXML
    private TextField textFieldSearchIngredients;

    @FXML
    private ListView<String> listViewIngredients;
    @FXML
    private TreeTableView<Recipe> treeTableViewIngredients;

    @FXML
    private TreeTableColumn<Recipe, String> columnQuantity, columnDescription;




    @FXML
    private void handleButtonNewIngredient(ActionEvent event)
    {
        addNewIngredient();
    }

    /**
     * TENER EN CUENTA AL ACTUALIZAR, GUARDAR Y ELIMINAR
     */


    private ObservableList<String> observableListIngredients;
    private HashMap<String, Ingredient> mapIngredients;

    private void loadListsIngredients()
    {
        try
        {
            mapIngredients = ingredientDAO.getAll();
            observableListIngredients = FXCollections.observableArrayList(mapIngredients.keySet());

            listViewIngredients.setItems(observableListIngredients);
        }
        catch (SQLException e)
        {
            FXTool.alertException(e);
        }
    }

    /**
     * Crear un nuevo ingrediente
     */
    private void addNewIngredient()
    {

        try {

           Ingredient newIngredient = getNewIngredient();
           if(newIngredient!=null)
           {

                   ingredientDAO.save(newIngredient);

                   mapIngredients.put(newIngredient.getDescription(), newIngredient);
                   observableListIngredients.add(newIngredient.getDescription());

           }

        } catch (Exception e) {
            FXTool.alertException(e);
        }
    }

    /**
     * Añadir un ingrediente al plato de comida.
     */
    private void addIngredientToFood()
    {

    }

    //endregion



    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        loadListsIngredients();
    }
}
