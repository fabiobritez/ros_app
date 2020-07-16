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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.PROCESS.*;
import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;

public class StockController extends StockServices
        implements Initializable
{
    //region FXML REFERENCES
    @FXML private TextField textFieldSearchIngredients;

    @FXML private ListView<String> listViewIngredients;

    @FXML private TextField textfieldDescription, textfieldStock, textfieldStockMin;

    @FXML private ComboBox<Unit> comboboxUnit;

    @FXML private Button buttonDelete, buttonUpdate, buttonSave;

    @FXML private Label labelMessage;

    @FXML private Text textStockMin, textStock;

    @FXML private TableView<Ingredient> tableViewLowStockIngredients;

    @FXML private TableColumn<Ingredient, String> columnDescription, columnUnit;

    @FXML private TableColumn<Ingredient, Integer> columnStock, columnStockMin;

    //<------------------------METHODS------------------------------>

    @FXML private void handleButtonAddStock(ActionEvent event)
    {
        addStock( tableViewLowStockIngredients.getSelectionModel().getSelectedItem() );
    }

    @FXML private void handleButtonNew(ActionEvent event)
    {
        setProcess(ADDING);
    }

    @FXML private void handleButtonDelete(ActionEvent event)
    {
        deleteIngredient();
    }

    @FXML private void handleButtonSave(ActionEvent event)
    {
        saveIngredient();
    }

    @FXML private void handleButtonUpdate(ActionEvent event)
    {
        updateIngredient();
    }

    @FXML private void handleClickedListIngredients(MouseEvent event)
    {
        selectIngredient( listViewIngredients.getSelectionModel().getSelectedItem() );
    }

    @FXML private void handleTextfieldStockReleased(KeyEvent event)
    {
        updateDiv1000(textfieldStock, textStock,comboboxUnit);
    }

    @FXML private void handleTextfieldStockMinReleased(KeyEvent event)
    {
        updateDiv1000(textfieldStockMin, textStockMin,comboboxUnit);
    }
    //endregion

    @Override public void initialize(URL location, ResourceBundle resources)
    {
        loadLists();
        setListenersProperty();
        loadTableViewLowStock();
    }

    //region GUI METHODS
    private ObservableList<String> observableListIngredients;
    private HashMap<String, Ingredient> mapIngredients;
    FilteredList<String> filteredListIngredients;
    ObservableList<Ingredient> observableLowStockIngredients = FXCollections.observableArrayList();

    private void loadLists()
    {
        try
        {
            mapIngredients = ingredientDAO.getAll();
            observableListIngredients = FXCollections.observableArrayList(mapIngredients.keySet());
            filteredListIngredients = new FilteredList<>(observableListIngredients, s -> true);

            listViewIngredients.setItems(filteredListIngredients);
            comboboxUnit.setItems(FXCollections.observableArrayList( Unit.values() ));
            comboboxUnit.getSelectionModel().select(Unit.QUANTITY);
        }
        catch (SQLException e)
        {
            FXTool.alertException(e);
        }
    }

    private void loadTableViewLowStock()
    {
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        columnStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMin"));
        columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

        updateLowStockIngredients();
        tableViewLowStockIngredients.setItems(observableLowStockIngredients);
    }

    private void setListenersProperty()
    {
        FXTool.setTextFieldInteger(textfieldStock);
        FXTool.setTextFieldInteger(textfieldStockMin);

        listViewIngredients.getSelectionModel().selectedItemProperty().addListener(event ->
                selectIngredient( listViewIngredients.getSelectionModel().getSelectedItem() ) );

        textFieldSearchIngredients.textProperty().addListener((observable, oldValue, newValue) ->
                filteredListIngredients.setPredicate(ingred ->
                        ingred.contains(newValue.trim().toUpperCase())
                ));

        comboboxUnit.getSelectionModel().selectedItemProperty().addListener((observable) -> {
                    updateDiv1000(textfieldStock, textStock,comboboxUnit);
                    updateDiv1000(textfieldStockMin, textStockMin,comboboxUnit);
                });

    }

    private void selectIngredient(String description)
    {
        Ingredient ingredient = mapIngredients.get(description);

        if(ingredient != null)
        {
            textfieldDescription.setText(description);
            textfieldStock    .setText(ingredient.getStock()  + "");
            textfieldStockMin .setText(ingredient.getStockMin()+"");
            comboboxUnit.getSelectionModel().select(Unit.getUnit( ingredient.getUnit() ));

            updateDiv1000(textfieldStock, textStock, comboboxUnit);
            updateDiv1000(textfieldStockMin, textStockMin, comboboxUnit);

            setProcess(VIEWING);
        }
    }

    private void saveIngredient()
    {
        String description = textfieldDescription.getText().trim();
        String stock       = textfieldStock.getText().trim();
        String stockMin    = textfieldStockMin.getText().trim();

        if( checkFields(description, stock, stockMin) )
        {
            try {

                Ingredient i = new Ingredient(
                        description,
                        Integer.parseInt(stock),
                        Integer.parseInt(stockMin),
                        comboboxUnit.getSelectionModel().getSelectedItem().toString()
                );
                ingredientDAO.save(i);

                observableListIngredients.add(i.getDescription());
                mapIngredients.put(i.getDescription(),i);
                updateLowStockIngredients();

                setProcess(VIEWING);
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }
    }

    private void updateIngredient()
    {
        String description = textfieldDescription.getText().trim();
        String stock       = textfieldStock.getText().trim();
        String stockMin    = textfieldStockMin.getText().trim();

        if( checkFields(description, stock, stockMin) )
        {
            try
            {
                Ingredient ingredient = mapIngredients.get(description);

                ingredient.setStock(Integer.parseInt(stock));
                ingredient.setStockMin(Integer.parseInt(stockMin));
                ingredient.setUnit(comboboxUnit.getSelectionModel().getSelectedItem().toString());

                ingredientDAO.update(ingredient);

                setProcess(VIEWING);
                labelMessage.setText("'" + description.toUpperCase() + "' ACTUALIZADO CORRECTAMENTE! ");
            }
            catch (SQLException e) { FXTool.alertException(e); }
            updateLowStockIngredients();
        }
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
                setProcess(VIEWING);
                updateLowStockIngredients();
            }
        }
        catch (SQLException e) { FXTool.alertException(e); }
    }

    private void updateLowStockIngredients()
    {
        observableLowStockIngredients.clear();
        for (Ingredient ingredient: mapIngredients.values())
        {
            if( ingredient.getStock()<=ingredient.getStockMin()
                    && !observableLowStockIngredients.contains(ingredient) )
            {
                observableLowStockIngredients.add(ingredient);
            }
        }
    }

    private void addStock(Ingredient ingredient)
    {
        if(ingredient != null)
        {
            int addedStock = getStockDialog(ingredient);
            ingredient.addStock(addedStock);
            try
            {
                ingredientDAO.update(ingredient);
                updateLowStockIngredients();
            }
            catch (SQLException e) { FXTool.alertException(e); ingredient.reduceStock(addedStock); }
        }
        else
        {
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
