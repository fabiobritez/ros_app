package ar.com.doctatech.food;

import ar.com.doctatech.food.model.Food;
import ar.com.doctatech.food.model.ItemRecipe;
import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import java.security.Key;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;
import static ar.com.doctatech.shared.utilities.FXPath.IMAGE_FOOD_DEFAULT;
import static ar.com.doctatech.shared.utilities.FileUtil.deleteFile;

public class FoodController
        extends FoodServices implements Initializable
{
    //TODO BUSQUEDA, DAO, CAMBIAR CON TECLAS, CON TECLA ABAJO BAJAR , ETC
    //region FXML REFERENCES

    @FXML private ListView<String> listViewFood;

    @FXML private TextField textfieldSearchFood;

    @FXML private CheckBox checkBoxPrice;

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
    @FXML private void handleTextFieldProfit (KeyEvent event)
    {
        if(!textfieldProfit.getText().trim().isEmpty() && !textfieldCost.getText().trim().isEmpty())
        {
            double value = Double.parseDouble(textfieldProfit.getText());
            double cost = Double.parseDouble(textfieldCost.getText());
            textfieldPrice.setText( Double.sum(cost, cost*(value/100) ) + "");
        }else
        {
         if(textfieldCost.getText().isEmpty())
            textfieldPrice.setText("0");
         if(textfieldProfit.getText().isEmpty())
             textfieldPrice.setText(textfieldCost.getText());
        }
    }

    @FXML private void handleTextFieldPrice(KeyEvent event)
    {
        if(!textfieldPrice.getText().trim().isEmpty() && !textfieldCost.getText().trim().isEmpty())
        {
            double price = Double.parseDouble(textfieldPrice.getText());
            double cost = Double.parseDouble(textfieldCost.getText());
            double profit = (price-cost)*100/cost;
            textfieldProfit.setText(profit+"");
        }else
        {
            textfieldProfit.setText("0.0");
        }

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

    //endregion
    @FXML
    private ListView<String> listViewIngredients;

    public void initialize(URL location, ResourceBundle resources)
    {
        loadLists();
        setListenersProperty();

        setProcess(PROCESS.VIEWING);
    }

    private HashMap<String, Food> mapFood;
    private ObservableList<String> observableListFood;

    private HashMap<String, Ingredient> mapIngredients;
    private ObservableList<String> observableListIngredients;

    private ObservableList<ItemRecipe> observableListItemRecipe;

    private final String IMAGE_DEFAULT = getClass().getResource(IMAGE_FOOD_DEFAULT).toExternalForm();

    private void loadLists()
    {
        try {
            //FOOD
            mapFood = foodDAO.getAll();
            observableListFood = FXCollections.observableArrayList( mapFood.keySet() );

            //ALL INGREDIENTS
            mapIngredients = ingredientDAO.getAll();
            observableListIngredients = FXCollections.observableArrayList(mapIngredients.keySet());
            listViewIngredients.setItems(observableListIngredients);

            //ITEMS FOR RECIPE
            columnDescription.setCellValueFactory( new PropertyValueFactory<>("description") );
            columnQuantity.setCellValueFactory( new PropertyValueFactory<>("quantity"));
            observableListItemRecipe = FXCollections.observableArrayList();
            tableViewIngredients.setItems(observableListItemRecipe);
        }
        catch (SQLException e)
        {
            FXTool.alertException(e);
        }
    }

    private void setListenersProperty()
    {
        FXTool.setTextFieldDouble(textfieldCost);
        FXTool.setTextFieldDouble(textfieldProfit);
        FXTool.setTextFieldDouble(textfieldPrice);

        //CAMBIO EN EL ITEM SELECCIONADO
        listViewFood.getSelectionModel().selectedItemProperty()
                .addListener((observable) ->
                        selectFood(listViewFood.getSelectionModel().getSelectedItem()));

        //BUSCADOR
        FilteredList<String> filteredList = new FilteredList<>(observableListFood, s -> true);
        textfieldSearchFood.textProperty().
                addListener((observable, oldValue, newValue) -> filteredList.setPredicate(f ->
                        f.contains(newValue.toUpperCase().trim() )
                ));
        listViewFood.setItems(filteredList);
        listViewFood.getSelectionModel().selectFirst();

        //TEXTFIELD PRECIO
        checkBoxPrice.selectedProperty().addListener((observable, oldValue, newValue) -> textfieldPrice.setDisable(!newValue));
    }


    /**
     * Obtiene el Food del HashMap y rellena el formulario.
     * Tambien la tabla de ingredientes.
     * @param name Nombre del plato de comida
     */
    private void selectFood(String name)
    {
        if(name !=null)
        {
            Food food = mapFood.get(name);

            textfieldID.setText    ( food.getFoodID() + "");
            textfieldName.setText  ( food.getName()   + "");
            textfieldCost.setText  ( food.getCost()   + "");
            textfieldProfit.setText( food.getProfit() + "");
            textfieldPrice.setText ( food.getPrice()  + "");
            showImage(food.getImage());

            observableListItemRecipe.clear();
            observableListItemRecipe.addAll(food.getRecipe());

            setProcess(PROCESS.VIEWING);
        }
    }
    /** Metodo auxiliar de selectFood() */
    private void showImage(String pathWithProtocol)
    {
        if(new File(removeProtocolImage( pathWithProtocol )).exists())
        {
            labelPathImage.setText(pathWithProtocol);
            imageView.setImage(new Image(pathWithProtocol) );
        }
        else
        {
            labelPathImage.setText(IMAGE_DEFAULT);
            imageView.setImage(new Image(IMAGE_DEFAULT));
        }
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
            String pathImage = addProtocolImage(fileSelected.getPath().trim());
            labelPathImage.setText( pathImage );
            imageView.setImage (
                    new Image(pathImage)
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

        if(checkFields(name,cost,profit,price))
        {
            try
            {
                String pathImage = labelPathImage.getText();

                if(!pathImage.equals(IMAGE_DEFAULT))
                {
                    String copyImage = copyImageToHomeFood(pathImage , name);

                    if(copyImage != null)
                    {
                        labelPathImage.setText(addProtocolImage( copyImage ));
                        imageView.setImage( new Image(addProtocolImage( copyImage )) );
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

        if( checkFields(foodID,name,cost,profit,price) )
        {
            try
            {
                Food food = foodDAO.get( Integer.parseInt( foodID ) );
                String oldName = food.getName();

                food.setName(name);
                food.setCost  ( Double.parseDouble(cost) );
                food.setProfit( Double.parseDouble(profit) );
                food.setPrice ( Double.parseDouble(price) );

                 if(updateImage( food, labelPathImage.getText() ))
                 {
                     labelPathImage.setText(food.getImage());
                     imageView.setImage( new Image( food.getImage() ) );
                 }

                foodDAO.update(food);

                if(!observableListFood.contains(name))
                {
                    observableListFood.remove(oldName);
                    observableListFood.add   (name);
                }
                mapFood.put(name, food);

                setProcess(PROCESS.VIEWING);
            }
            catch (SQLException | NotFoundException exception)
            {
                FXTool.alertException(exception);
            }

        }
    }
    /** Metodo auxiliar de updateFood() */
    private boolean updateImage(Food food, String newImagePath)
    {
        String currentImage = food.getImage();
        //si se cambio de foto
        if (!newImagePath.equals(currentImage))
        {
            String copyNewImage = copyImageToHomeFood(removeProtocolImage(newImagePath), food.getName());
            if (copyNewImage != null) //SI SE LOGRO HACER LA COPIA
            {
                if (!currentImage.equals(IMAGE_DEFAULT)) //SI LA IMAGEN ANTERIOR NO DEFAULT

                    if (!addProtocolImage(copyNewImage).equals(currentImage)) //VERIFICA SI CAMBIARON EL NOMBRE DE LA COMIDA
                        deleteFile(removeProtocolImage(currentImage));

                food.setImage(addProtocolImage(copyNewImage));
            }
            else
            {
                return false;
            }
        }
        return true;
    }


    private void deleteFood()
    {
        String foodID = textfieldID.getText();
        String name = textfieldName.getText().trim();

        if(checkFields(foodID, name))
        {
            try
            {
                foodDAO.remove(  Integer.parseInt(foodID));

                mapFood.remove(name);
                observableListFood.remove(name);

                FXTool.alertInformation("Operación realizada", "Se ha eliminado con exito");
            }
            catch (Exception exception)
            {
                FXTool.alertException(exception);
            }
        }
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
    @FXML private void handleButtonAddIngredient(ActionEvent event)
    {
        addIngredientToFood();
    }

    @FXML private void handleButtonRemoveIngredient(ActionEvent event)
    {
        removeIngredientFromFood();
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
       int foodID = Integer.parseInt(textfieldID.getText());
       String name = textfieldName.getText();

       String description = listViewIngredients.getSelectionModel().getSelectedItem();
       if(description!=null)
       {
           int quantity = getQuantityIngredient();
           try
           {
               Ingredient ingredient = mapIngredients.get(description);
                ItemRecipe itemRecipe = new ItemRecipe(ingredient, quantity);
               foodDAO.addItemRecipe(foodID, itemRecipe);
               mapFood.get(name).addIngredient(itemRecipe);
               observableListItemRecipe.add(itemRecipe);
           }
           catch (SQLException e)
           {
               FXTool.alertException(e);
           }
       }
    }

    private void removeIngredientFromFood()
    {
        int foodID = Integer.parseInt(textfieldID.getText());
        String name = textfieldName.getText();
        ItemRecipe itemRecipe = tableViewIngredients.getSelectionModel().getSelectedItem();
        if(itemRecipe != null)
        {
            try
            {
                foodDAO.removeItemRecipe(foodID, itemRecipe);
                mapFood.get(name).removeIngredient(itemRecipe.getDescription());
                observableListItemRecipe.remove(itemRecipe);
            }
            catch (SQLException e)
            {
                FXTool.alertException(e);
            }
        }
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

            checkBoxPrice.setVisible(false);
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

            checkBoxPrice.setVisible(true);
            buttonSelectImage.setVisible(true);
            tabIngredient.setDisable(false);
            textfieldName.requestFocus();
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

            checkBoxPrice.setVisible(true);
            buttonSelectImage.setVisible(true);
            labelPathImage.setText(IMAGE_DEFAULT);
            imageView.setImage(new Image(IMAGE_DEFAULT));
            tabIngredient.setDisable(true);

            textfieldName.requestFocus();
        }
    }

}
