package ar.com.doctatech.stock;

import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.stock.ingredient.Unit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @FXML private TextField textfieldSearchEngine;

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
    @FXML private void onKeyPressedSearchEngine(KeyEvent event)
    {
        if(event.getCode() == KeyCode.DOWN)
        {
            listViewIngredients.getSelectionModel().selectFirst();
            listViewIngredients.requestFocus();
        }
    }

    @FXML private void onActionClear()
    {
        textfieldSearchEngine.setText("");
        textfieldSearchEngine.requestFocus();
    }

    @FXML private void onActionAddStock()
    {
        addStock( tableViewLowStockIngredients.getSelectionModel().getSelectedItem() );
    }

    @FXML private void onActionNewIngredient()
    {
        setProcess(ADDING);
    }

    @FXML private void onActionDeleteIngredient()
    {
        deleteIngredient();
    }

    @FXML private void onActionSaveIngredient()
    {
        saveIngredient();
    }

    @FXML private void onActionUpdateIngredient()
    {
        updateIngredient();
    }

    @FXML private void onReleasedTextfieldStock()
    {
        updateDiv1000(textfieldStock, textStock,comboboxUnit);
    }

    @FXML private void onReleasedTextfieldStockMin()
    {
        updateDiv1000(textfieldStockMin, textStockMin,comboboxUnit);
    }

    @FXML private void onSelectChangedTab(){updateLowStockIngredients();}
    //endregion

    @Override public void initialize(URL location, ResourceBundle resources)
    {
        loadIngredients();
        addListenersEvents();
        loadLowStockIngredients();
    }

    //region METHODS

    private HashMap<String, Ingredient> mapIngredients;
    private ObservableList<String> obsIngredients;
    private FilteredList<String> flIngredients;
    private ObservableList<Ingredient> obsLowStockIngredients = FXCollections.observableArrayList();

    private void loadIngredients()
    {
        try
        {
            mapIngredients = ingredientDAO.getAll();
            obsIngredients = FXCollections.observableArrayList(mapIngredients.keySet());
            flIngredients = new FilteredList<>(obsIngredients, s -> true);

            listViewIngredients.setItems(flIngredients);
            comboboxUnit.setItems(FXCollections.observableArrayList( Unit.values() ));
            comboboxUnit.getSelectionModel().select(Unit.QUANTITY);
        }
        catch (SQLException e) { FXTool.alertException(e); }
    }

    private void loadLowStockIngredients()
    {
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        columnStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMin"));
        columnUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

        updateLowStockIngredients();
        tableViewLowStockIngredients.setItems(obsLowStockIngredients);
    }

    private void updateLowStockIngredients()
    {
        obsLowStockIngredients.clear();
        for (Ingredient ingredient: mapIngredients.values())
        {
            if( ingredient.getStock()<=ingredient.getStockMin()
                    && !obsLowStockIngredients.contains(ingredient) )
            {
                obsLowStockIngredients.add(ingredient);
            }
        }
    }

    private void addListenersEvents()
    {
        FXTool.setTextFieldInteger(textfieldStock);
        FXTool.setTextFieldInteger(textfieldStockMin);

        //ITEMS SELECTION
        listViewIngredients.getSelectionModel().selectedItemProperty().addListener(event ->
                selectIngredient() );

        //UX
        listViewIngredients.setOnKeyPressed(event ->
        {
            if(event.getCode() == KeyCode.UP &&
                listViewIngredients.getSelectionModel().isSelected(0))
            {
                textfieldSearchEngine.requestFocus();
            }
        });

        //INGREDIENTS SEARCHER
        textfieldSearchEngine.textProperty().addListener((observable, oldValue, newValue) ->
                flIngredients.setPredicate(ingred ->
                        ingred.contains(newValue.trim().toUpperCase())
                ));

        //CONVERTER
        comboboxUnit.getSelectionModel().selectedItemProperty().addListener((observable) -> {
                    updateDiv1000(textfieldStock, textStock,comboboxUnit);
                    updateDiv1000(textfieldStockMin, textStockMin,comboboxUnit);
                });
    }

    private void selectIngredient()
    {
        String description = listViewIngredients.getSelectionModel().getSelectedItem();

        if(checkFields(description))
        {
            Ingredient ingredient = mapIngredients.get(description);

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

                obsIngredients.add(i.getDescription());
                mapIngredients.put(i.getDescription(),i);

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
                textfieldSearchEngine.requestFocus();
                labelMessage.setText("'" + description.toUpperCase() + "' ACTUALIZADO CORRECTAMENTE! ");
            }
            catch (SQLException e) { FXTool.alertException(e); }
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

                obsIngredients.remove(description);
                mapIngredients.remove(description);

                FXTool.alertInformation("Eliminado","Ingrediente eliminado correctamente!");

                listViewIngredients.getSelectionModel().selectFirst();
                setProcess(VIEWING);
            }
        }
        catch (SQLException e) { FXTool.alertException(e); }
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
            buttonDelete.setDisable(true);
            buttonUpdate.setDisable(true);
            buttonSave.setDisable(false);
        }
        else if(process.equals(EDITING)
                || process.equals(VIEWING))
        {
            textfieldDescription.setEditable(false);
            buttonDelete.setDisable(false);
            buttonUpdate.setDisable(false);
            buttonSave.setDisable(true);
        }
        labelMessage.setText("");
    }
}
