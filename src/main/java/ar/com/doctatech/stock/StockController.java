package ar.com.doctatech.stock;

import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.stock.ingredient.Unit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.PROCESS.*;

public class StockController extends StockServices
        implements Initializable {


    //region FXML REFERENCES 1
    @FXML
    private TextField textFieldSearchIngredients;

    @FXML
    private ListView<String> listViewIngredients;

    @FXML
    private TextField textfieldDescription, textfieldStock, textfieldStockMin;

    @FXML
    private ComboBox<Unit> comboboxUnit;

    @FXML
    private Button buttonDelete, buttonUpdate, buttonSave;

    @FXML
    private Label labelMessage;


    @FXML
    private void handleButtonNew(ActionEvent event)
    {
        newIngredient();
    }

    @FXML
    private void handleButtonDelete(ActionEvent event)
    {
        deleteIngredient();
    }

    @FXML
    private void handleButtonSave(ActionEvent event)
    {
        saveIngredient();
    }

    @FXML
    private void handleButtonUpdate(ActionEvent event)
    {
        updateIngredient();
    }

    @FXML
    private void handleClickedListIngredients(MouseEvent event)
    {
        selectIngredient( listViewIngredients.getSelectionModel().getSelectedItem() );
    }

    //endregion

    //region GUI METHODS 1
    private ObservableList<String> observableListIngredients;
    private HashMap<String, Ingredient> mapIngredients;

    private void loadLists()
    {
        try
        {
            mapIngredients = ingredientDAO.getAll();
            observableListIngredients = FXCollections.observableArrayList(mapIngredients.keySet());

            //AHORA ESTA CON FILTEREDLIST
            //listViewIngredients.setItems(observableListIngredients);
        }
        catch (SQLException e)
        {
            FXTool.alertException(e);
        }
        comboboxUnit.setItems(FXCollections.observableArrayList( Unit.values() ));
    }

    private void selectIngredient(String description)
    {
        Ingredient ingredient = mapIngredients.get(description);

        textfieldDescription.setText(description);
        textfieldStock.setText(ingredient.getStock()+"");
        textfieldStockMin.setText(ingredient.getStockMin()+"");
        comboboxUnit.getSelectionModel().select(Unit.getUnit( ingredient.getUnit() ));
        setProcess(VIEWING);
    }

    private void newIngredient()
    {
        setProcess(ADDING);
    }

    private void deleteIngredient()
    {
        try
        {
            String description = textfieldDescription.getText();

            Alert alert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Si elimina el ingrediente, se eliminará de todas las comidas",
                    ButtonType.YES,
                    ButtonType.CANCEL
            );
            alert.setHeaderText("¿Estas seguro que desea eliminar a '"+description+"'?");
            alert.showAndWait();

            if (alert.getResult().equals(ButtonType.YES))
            {
                ingredientDAO.remove(description);
                observableListIngredients.remove(description);
                mapIngredients.remove(description);


                FXTool.alertInformation("Eliminado","Ingrediente eliminado correctamente!");

                listViewIngredients.getSelectionModel().selectFirst();
            }
        }
        catch (SQLException e) {
            FXTool.alertException(e);
        }
        updateLowStockIngredients();

        setProcess(VIEWING);
    }

    private void saveIngredient()
    {
        if(textfieldDescription.getText().trim().isEmpty() ||
                 textfieldStock.getText().trim().isEmpty() ||
                 textfieldStockMin.getText().trim().isEmpty() )
        {
            FXTool.alertInformation(
                    "Campos requeridos",
                    "El usuario debe tener nombre de descripcion y stocks"
            );
        }else{
            try {

                Ingredient i = new Ingredient(
                    textfieldDescription.getText().trim(),
                    Integer.parseInt(textfieldStock.getText().trim()),
                    Integer.parseInt(textfieldStockMin.getText().trim()),
                    comboboxUnit.getSelectionModel().getSelectedItem().toString()
                );
                ingredientDAO.save(i);

                observableListIngredients.add(i.getDescription());
                mapIngredients.put(i.getDescription(),i);
                setProcess(VIEWING);
            }
            catch (SQLException e)
            {
                FXTool.alertException(e);
            }
        }
        updateLowStockIngredients();
    }

    private void updateIngredient()
    {
        String description = textfieldDescription.getText().trim();
        try
        {
            Ingredient ingredient = mapIngredients.get(description);

            ingredient.setStock( Integer.parseInt( textfieldStock.getText().trim() ) );
            ingredient.setStockMin( Integer.parseInt( textfieldStockMin.getText().trim() ));
            ingredient.setUnit( comboboxUnit.getSelectionModel().getSelectedItem().toString() );

            ingredientDAO.update(ingredient);
            setProcess(VIEWING);
            labelMessage.setText("'"+description.toUpperCase()+ "' ACTUALIZADO CORRECTAMENTE! ");
        }
        catch (SQLException exception)
        {
            FXTool.alertException(exception);
        }
        updateLowStockIngredients();
    }

    private void setSearchIngredients()
    {
        FilteredList<String> filteredList = new FilteredList<>(observableListIngredients, s -> true);
        textFieldSearchIngredients.textProperty().
                addListener((observable, oldValue, newValue) -> filteredList.setPredicate(ing ->
                      ing.trim().toLowerCase().contains(newValue.trim().toLowerCase())
                ));
        listViewIngredients.setItems(filteredList);
    }

    //endregion

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        loadLists();
        listViewIngredients.getSelectionModel().selectedItemProperty().addListener(event ->
                selectIngredient( listViewIngredients.getSelectionModel().getSelectedItem() ));

        FXTool.setTextFieldInteger(textfieldStock);
        FXTool.setTextFieldInteger(textfieldStockMin);
        setSearchIngredients();
        loadTableView();
    }

    //region FXML REFERENCES 2


    @FXML
    private TableView<Ingredient> tableViewLowStockIngredients;

    @FXML
    private TableColumn<Ingredient, String> columnDescription;

    @FXML
    private TableColumn<Ingredient, Integer> columnStock, columnStockMin;

    @FXML
    private TableColumn<Ingredient, String> columnUnit;

    @FXML
    private void handleButtonAddStock(ActionEvent event)
    {
        addStock( tableViewLowStockIngredients.getSelectionModel().getSelectedItem() );
    }

    ObservableList<Ingredient> lowStockIngredients = FXCollections.observableArrayList();

    private void loadTableView()
    {
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        columnStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMin"));
        columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

        updateLowStockIngredients();

        tableViewLowStockIngredients.setItems(lowStockIngredients);
    }

    private void updateLowStockIngredients()
    {
        for (Ingredient ingredient: mapIngredients.values())
        {
            if( ingredient.getStock()<=ingredient.getStockMin()
                 && !lowStockIngredients.contains(ingredient) )
            {
                lowStockIngredients.add(ingredient);
            }else if(ingredient.getStock()>ingredient.getStockMin()){
                lowStockIngredients.remove(ingredient);
            }
        }
    }

    private void addStock(Ingredient ingredient)
    {
        if(ingredient != null)
        {
            int addedStock = getStockDialog(ingredient);
            ingredient.addStock(addedStock);

            try {
                ingredientDAO.update(ingredient);
                updateLowStockIngredients();
            } catch (SQLException e) {
                FXTool.alertException(e);
                ingredient.reduceStock(addedStock);
            }
        }else{
            FXTool.alertInformation("NO HAY NIGUN INGREDIENTE SELECCIONADO", "");
        }


    }

    //endregion

    private void setProcess(PROCESS process)
    {

        if(process.equals(ADDING))
        {
            textfieldDescription.setEditable(true);
            textfieldDescription.setText("");
            textfieldDescription.requestFocus();

            textfieldStockMin.setText("0");
            textfieldStock.setText("0");
            comboboxUnit.getSelectionModel().select(Unit.QUANTITY);
            buttonDelete.setVisible(false);
            buttonSave.toFront();

        }
        else if(process.equals(EDITING)
                || process.equals(VIEWING))
        {
            textfieldDescription.setEditable(false);
            buttonDelete.setVisible(true);
            buttonUpdate.toFront();
        }
        labelMessage.setText("");
    }
}
