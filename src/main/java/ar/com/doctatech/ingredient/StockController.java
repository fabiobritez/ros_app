package ar.com.doctatech.ingredient;

import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.ingredient.model.Ingredient;
import ar.com.doctatech.ingredient.model.Unit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
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
        addStock();
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

    @FXML private void onSelectChangedTab(){ updateLowStockIngredients(); }
    //endregion

    @Override public void initialize(URL location, ResourceBundle resources)
    {
        loadIngredients();
        loadLowStockIngredients();
        addListenersEvents();
    }

    //region METHODS
    private HashMap<String, Ingredient> mapIngredients;
    private ObservableList<String> obsIngredients;
    private FilteredList<String> flIngredients;
    private ObservableList<Ingredient> obsLowStockIngredients;

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

        obsLowStockIngredients = FXCollections.observableArrayList();
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
        FXTool.setTextFieldInteger(textfieldStock,7);
        FXTool.setTextFieldInteger(textfieldStockMin, 7);

        //INGREDIENTS SEARCHER
        textfieldSearchEngine.textProperty().addListener((obs, old, value) ->
                flIngredients.setPredicate(i -> i.contains(value.trim().toUpperCase()) ));

        //ITEMS SELECTION
        listViewIngredients.getSelectionModel().selectedItemProperty()
                .addListener(event -> selectIngredient() );
        //UX
        listViewIngredients.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.UP && listViewIngredients
                    .getSelectionModel().isSelected(0))
            { textfieldSearchEngine.requestFocus(); }
        });

        //ADD STOCK
        tableViewLowStockIngredients.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) addStock();
        });
        tableViewLowStockIngredients.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)
                    && event.getClickCount()==2) addStock();
        });

        //CONVERTER
        comboboxUnit.getSelectionModel().selectedItemProperty().addListener((observable) -> {
                    updateDiv1000(textfieldStock, textStock,comboboxUnit);
                    updateDiv1000(textfieldStockMin, textStockMin,comboboxUnit);
                });


        listViewIngredients.getSelectionModel().selectFirst();
        textfieldSearchEngine.requestFocus();
    }

    private void selectIngredient()
    {
        String description = listViewIngredients.getSelectionModel().getSelectedItem();

        if(description!=null)
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

                Ingredient ingredient = new Ingredient(
                        description,
                        Integer.parseInt(stock),
                        Integer.parseInt(stockMin),
                        comboboxUnit.getSelectionModel().getSelectedItem().toString()
                );
                ingredientDAO.save(ingredient);

                obsIngredients.add(ingredient.getDescription());
                mapIngredients.put(ingredient.getDescription(),ingredient);

                listViewIngredients.requestFocus();
                listViewIngredients.getSelectionModel().select(ingredient.getDescription());
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
        String unit        = comboboxUnit.getSelectionModel().getSelectedItem().toString();
        if( checkFields(description, stock, stockMin) )
        {
            try
            {
                Ingredient ingredient = mapIngredients.get(description);

                ingredient.setStock(Integer.parseInt(stock));
                ingredient.setStockMin(Integer.parseInt(stockMin));
                ingredient.setUnit(unit);
                ingredientDAO.update(ingredient);

                listViewIngredients.requestFocus();
                listViewIngredients.getSelectionModel().select(description);
                setProcess(VIEWING);
                labelMessage.setText("'" + description.toUpperCase() + "' ACTUALIZADO CORRECTAMENTE! ");
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }
    }

    private void deleteIngredient()
    {
        String description = textfieldDescription.getText().trim();

        if(!description.isEmpty())
        {
            try
            {
                Alert alert = new Alert(
                    Alert.AlertType.WARNING,
                    "Si elimina el ingrediente, \nse eliminará de todas las comidas",
                    ButtonType.YES,
                    ButtonType.CANCEL
                );
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/alerts.css").toExternalForm());

                alert.setHeaderText("¿Estas seguro que desea eliminar \na '"+description+"'?");
                alert.showAndWait();

                if (alert.getResult().equals(ButtonType.YES))
                {
                    ingredientDAO.remove(description);
                    obsIngredients.remove(description);
                    mapIngredients.remove(description);

                    listViewIngredients.requestFocus();
                    listViewIngredients.getSelectionModel().selectNext();
                }
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }
    }

    private void addStock()
    {
        Ingredient ingredient = tableViewLowStockIngredients.getSelectionModel().getSelectedItem();
        if(ingredient != null)
        {
            int addedStock = getStockDialog(ingredient);
            ingredient.addStock(addedStock);
            try
            {
                ingredientDAO.update(ingredient);
                updateLowStockIngredients();
                selectIngredient();
            }
            catch (SQLException e) { FXTool.alertException(e); ingredient.reduceStock(addedStock); }
        }
        else
            FXTool.alertInformation("NO HAY NIGUN INGREDIENTE SELECCIONADO", "");
    }

    //endregion

    private void setProcess(PROCESS process)
    {
        if(process.equals(ADDING))
        {
            textfieldDescription.setDisable(false);

            textfieldDescription.setText("");
            textfieldStockMin.setText("");
            textStock.setText("");
            textfieldStock.setText("");
            textStockMin.setText("");
            comboboxUnit.getSelectionModel().select(Unit.QUANTITY);

            buttonDelete.setDisable(true);
            buttonUpdate.setDisable(true);
            buttonSave.setDisable(false);

            textfieldDescription.requestFocus();
        }
        else if(process.equals(EDITING)
                || process.equals(VIEWING))
        {
            textfieldDescription.setDisable(true);

            buttonDelete.setDisable(false);
            buttonUpdate.setDisable(false);
            buttonSave.setDisable(true);
        }
        labelMessage.setText("");
    }
}
