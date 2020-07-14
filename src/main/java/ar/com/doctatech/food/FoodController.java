package ar.com.doctatech.food;

import ar.com.doctatech.food.model.Food;
import ar.com.doctatech.food.model.ItemRecipe;
import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.utilities.FXPath.IMAGE_FOOD_DEFAULT;
import static ar.com.doctatech.shared.utilities.FileUtil.deleteFile;

public class FoodController
        extends FoodServices implements Initializable
{
    @FXML private ListView<String> listViewFood;

    @FXML private TextField textfieldSearchFood;

    @FXML private TextField textfieldID, textfieldName, textfieldCost,
            textfieldProfit, textfieldPrice;

    @FXML private Button buttonNew, buttonEdit, buttonUpdate,
            buttonSave, buttonDelete,buttonSelectImage;

    @FXML private ImageView imageView;
    @FXML private Label labelPathImage;

    @FXML private TableView<ItemRecipe> tableViewIngredients;

    @FXML private TableColumn<ItemRecipe, Integer> columnQuantity;

    @FXML private TableColumn<ItemRecipe, String> columnDescription;

    @FXML private void handleClickedListFood(MouseEvent event)
    {
        selectFood( listViewFood.getSelectionModel().getSelectedItem() );
    }

    @FXML private void handleTextFieldSearchFood(KeyEvent keyEvent)
    {


    }

    @FXML private void handleButtonSelectImage(ActionEvent event)
    {
        selectImage();
    }

    @FXML private void handleButtonNew(ActionEvent event)
    {
        setProcess(PROCESS.ADDING);
    }

    @FXML private void handleButtonEdit(ActionEvent event)
    {
        setProcess(PROCESS.EDITING);
    }
    @FXML private void handleButtonUpdate (ActionEvent event)
    {
        updateFood();
    }

    @FXML private void handleButtonSave(ActionEvent event)
    {
        saveFood();
    }

    @FXML private void handleButtonDelete(ActionEvent event)
    {
        deleteFood();
    }

    private HashMap<String, Food> mapFood;
    private ObservableList<String> observableListFood;
    private ObservableList<ItemRecipe> observableListItemRecipe;
    private final String IMAGE_DEFAULT = getClass().getResource(IMAGE_FOOD_DEFAULT).getPath();
    private void loadLists()
    {
        try {
            mapFood = foodDAO.getAll();
            observableListFood = FXCollections.observableArrayList( mapFood.keySet() );
            observableListItemRecipe = FXCollections.observableArrayList();

            listViewFood.setItems(observableListFood);

            columnDescription.setCellValueFactory( new PropertyValueFactory<>("description") );
            columnQuantity.setCellValueFactory( new PropertyValueFactory<>("quantity"));
            tableViewIngredients.setItems(observableListItemRecipe);
        }
        catch (SQLException e)
        {
            FXTool.alertException(e);
        }
    }

    /**
     * Obtiene el Food del HashMap y rellena el formulario.
     * Tambien la tabla de ingredientes.
     * @param name AS
     */
    private void selectFood(String name)
    {
        Food food = mapFood.get(name);

        textfieldID.setText    ( food.getFoodID() + "");
        textfieldName.setText  ( food.getName()   + "");
        textfieldCost.setText  ( food.getCost()   + "");
        textfieldProfit.setText( food.getProfit() + "");
        textfieldPrice.setText ( food.getPrice()  + "");

        if(food.getImage() != null && !food.getImage().isEmpty() )
        {
            imageView.setImage(new Image( food.getImage() ));
        }

        observableListItemRecipe.clear();
        observableListItemRecipe.addAll(food.getRecipe());

        setProcess(PROCESS.VIEWING);
    }

    /**
     * Abre un File Chooser y obtiene la imagen seleccionada.
     * Luego la muestra por el ImageView
     */
    private void selectImage()
    {
        File fileSelected = getFromImageChooser(imageView);

        if(fileSelected != null)
        {
            labelPathImage.setText( fileSelected.getPath() );
            imageView.setImage (
                    new Image(addProtocolImage( fileSelected.getPath() ))
            );
        }
    }

    /**
     * Verifica que los campos requeridos esten rellenados.
     * Crea un Food con los campos.
     * Almacena en la base de datos.
     * Cambia PROCESS::VIEWING
     * OBS: No se agregan ingredientes en este metodo.
     */
    private void saveFood()
    {
        String name   = textfieldName.getText().trim().toUpperCase();
        String cost   = textfieldCost.getText().trim();
        String profit = textfieldProfit.getText().trim();
        String price  = textfieldPrice.getText().trim();

        if(   name.isEmpty()   || cost.isEmpty()
           || profit.isEmpty() || price.isEmpty())
        {
            FXTool.alertInformation(
                    "RELLENA LOS CAMPOS REQUERIDOS" ,
                    "Los campos requeridos son: \n" +
                           "Nombre, Costo, Ganancia, Precio"
            );
        }
        else
        {
            try
            {
                String originalImage = labelPathImage.getText();

                if(!originalImage.equals(IMAGE_DEFAULT))
                {
                    String newImagePath = copyImageToHomeFood(originalImage , name);
                    if(newImagePath != null)
                    {
                        labelPathImage.setText(newImagePath);
                        imageView.setImage( new Image(addProtocolImage( newImagePath )) );
                    }
                }

                Food food = new Food(0, name,
                        Integer.parseInt(cost),
                        Integer.parseInt(profit),
                        Integer.parseInt(price),
                        addProtocolImage(labelPathImage.getText()),
                        true
                );

                foodDAO.save(food);

                textfieldID.setText( food.getFoodID() + "" );
                observableListFood.add(name);
                mapFood.put(name, food);

                setProcess(PROCESS.VIEWING);
            }
            catch (SQLException e)
            {
                FXTool.alertException(e);
            }
        }
    }

    private void updateFood()
    {
        String foodID = textfieldID.getText().trim();
        String name   = textfieldName.getText().trim().toUpperCase();
        String cost   = textfieldCost.getText().trim();
        String profit = textfieldProfit.getText().trim();
        String price  = textfieldPrice.getText().trim();

        if(   name.isEmpty()   || cost.isEmpty()
                || profit.isEmpty() || price.isEmpty())
        {
            FXTool.alertInformation(
                    "RELLENA LOS CAMPOS REQUERIDOS" ,
                    "Los campos requeridos son: \n" +
                            "Nombre, Costo, Ganancia, Precio"
            );
        }
        else
        {
            try
            {
                Food food = foodDAO.get( Integer.parseInt( foodID ) );
                String oldName = food.getName();

                //TODO updating food
                food.setName(name);
                food.setCost  ( Double.parseDouble(cost) );
                food.setProfit( Double.parseDouble(profit) );
                food.setPrice ( Double.parseDouble(price) );

                String newImage = addProtocolImage( labelPathImage.getText() );
                String currentImage = food.getImage();
                if(!newImage.equals( currentImage ))
                {
                    //TODO delete oldImage
                    String copyNewImage = copyImageToHomeFood(removeProtocolImage(newImage) , name);

                    if(copyNewImage != null)
                    {
                        deleteFile(removeProtocolImage(currentImage));

                        food.setImage( addProtocolImage(copyNewImage) );
                        labelPathImage.setText(copyNewImage);
                        imageView.setImage( new Image( food.getImage() ) );
                    }
                }

                foodDAO.update(food);

                if(!observableListFood.contains(name))
                {
                    observableListFood.remove(oldName);
                    observableListFood.add   (name);
                }
                mapFood.put(name, food);
            }
            catch (SQLException | NotFoundException exception)
            {
                FXTool.alertException(exception);
            }

        }
    }


    private void deleteFood()
    {
        String foodID = textfieldID.getText();
        String name = textfieldName.getText().trim();

        if(!foodID.isEmpty() && textfieldID.isVisible() && !name.isEmpty())
        {
            try {
                foodDAO.remove(  Integer.parseInt(foodID));

                deleteFile( labelPathImage.getText() );
                mapFood.remove(name);
                observableListFood.remove(name);
                FXTool.alertInformation("Operación realizada", "Se ha eliminado con exito");
            }catch (Exception exception)
            {
                FXTool.alertException(exception);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        loadLists();
        loadListsIngredients();
        FXTool.setTextFieldDouble(textfieldCost);
        FXTool.setTextFieldDouble(textfieldProfit);
        FXTool.setTextFieldDouble(textfieldPrice);

        listViewFood.getSelectionModel().selectedItemProperty().addListener((observable) ->
                selectFood(listViewFood.getSelectionModel().getSelectedItem()));
        listViewFood.getSelectionModel().select(0);
        setProcess(PROCESS.VIEWING);
    }


    //region INGREDIENT

    @FXML
    private TextField textFieldSearchIngredients;

    @FXML private Tab tabIngredient;
    @FXML
    private void handleButtonNewIngredient(ActionEvent event)
    {
        addNewIngredient();
    }

    @FXML
    private ListView<String> listViewIngredients;

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

    private void setProcess(PROCESS process)
    {
        if(process.equals(PROCESS.VIEWING) )
        {
            textfieldID.setVisible      (true);
            textfieldName.setEditable   (false);
            textfieldCost.setEditable   (false);
            textfieldProfit.setEditable (false);
            textfieldPrice.setEditable  (false);

            buttonDelete.setDisable (false);
            buttonEdit.setDisable   (false);
            buttonSave.setDisable   (true);
            buttonUpdate.setDisable (true);

            buttonSelectImage.setVisible(false);
            tabIngredient.setDisable(false);
        }
        else if(process.equals(PROCESS.EDITING))
        {
            textfieldID.setVisible      (true);
            textfieldName.setEditable   (true);
            textfieldCost.setEditable   (true);
            textfieldProfit.setEditable (true);
            textfieldPrice.setEditable  (true);

            buttonDelete.setDisable (true);
            buttonSave.setDisable   (true);
            buttonUpdate.setDisable (false);
            buttonEdit.setDisable   (true);

            buttonSelectImage.setVisible(true);
            tabIngredient.setDisable(false);
        }
        else if(process.equals(PROCESS.ADDING))
        {
            textfieldID.setVisible      (false);
            textfieldName.setEditable   (true);
            textfieldCost.setEditable   (true);
            textfieldProfit.setEditable (true);
            textfieldPrice.setEditable  (true);

            textfieldName.setText   ("");
            textfieldCost.setText   ("0");
            textfieldProfit.setText ("0");
            textfieldPrice.setText  ("0");

            buttonDelete.setDisable (true);
            buttonSave.setDisable   (false);
            buttonUpdate.setDisable (true);
            buttonEdit.setDisable   (true);

            buttonSelectImage.setVisible(true);
            labelPathImage.setText(IMAGE_DEFAULT);
            Image defaultImage = new Image(addProtocolImage( IMAGE_DEFAULT ));
            imageView.setImage(defaultImage);
            tabIngredient.setDisable(true);
        }
    }

}
