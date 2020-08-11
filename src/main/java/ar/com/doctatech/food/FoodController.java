package ar.com.doctatech.food;

import ar.com.doctatech.food.model.Food;
import ar.com.doctatech.food.model.ItemRecipe;
import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.ingredient.model.Ingredient;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;
import static ar.com.doctatech.shared.utilities.FileUtil.deleteFile;

public class FoodController
        extends FoodServices implements Initializable
{
    //region FXML REFERENCES

    @FXML private ListView<String> listViewFood, listViewIngredients;

    @FXML private TextField textfieldSearchFood, textfieldSearchIngredient,
            textfieldID, textfieldName, textfieldCost, textfieldProfit, textfieldPrice;
    @FXML private CheckBox checkBoxPrice;
    @FXML private ImageView imageView;
    @FXML private Label labelPathImage;

    @FXML private Button buttonEdit, buttonUpdate, buttonSave,
                         buttonDelete,buttonSelectImage;

    @FXML private Tab tabIngredient;
    @FXML private TableView<ItemRecipe> tableViewIngredients;
    @FXML private TableColumn<ItemRecipe, Integer> columnQuantity;
    @FXML private TableColumn<ItemRecipe, String> columnDescription, columnUnit;
    @FXML private Text textMessageItemRecipe;

    @FXML private void onActionClearFood()
    {
        textfieldSearchFood.setText("");
        textfieldSearchFood.requestFocus();
    }
    @FXML private void onActionClearIngredient()
    {
        textfieldSearchIngredient.setText("");
        textfieldSearchIngredient.requestFocus();
    }

    @FXML private void onKeyPressedTextfieldSearchFood(KeyEvent keyEvent)
    {
        if(keyEvent.getCode() == KeyCode.DOWN)
        {
            listViewFood.requestFocus();
            listViewFood.getSelectionModel().selectFirst();
        }
    }
    @FXML private void onKeyPressedTextfiedSearchIngredient(KeyEvent event)
    {
        if(event.getCode() == KeyCode.DOWN)
        {
            listViewIngredients.requestFocus();
            listViewIngredients.getSelectionModel().selectFirst();
        }
    }

    @FXML private void onKeyReleasedTextfieldProfit()
    {
        if(!textfieldProfit.getText().trim().isEmpty() && !textfieldCost.getText().trim().isEmpty())
        {
            double value = Double.parseDouble(textfieldProfit.getText());
            double cost = Double.parseDouble(textfieldCost.getText());
            double price = Double.sum(cost, cost*(value/100));
            textfieldPrice.setText( String.format(Locale.US, "%.2f", price));
        }else
        {
         if(textfieldCost.getText().isEmpty())
            textfieldPrice.setText("0");
         if(textfieldProfit.getText().isEmpty())
             textfieldPrice.setText(textfieldCost.getText());
        }
    }
    @FXML private void onKeyReleasedTextfieldPrice()
    {
        if(!textfieldPrice.getText().trim().isEmpty() && !textfieldCost.getText().trim().isEmpty())
        {
            double price = Double.parseDouble(textfieldPrice.getText());
            double cost = Double.parseDouble(textfieldCost.getText());
            double profit = (price-cost)*100/cost;
            textfieldProfit.setText(String.format(Locale.US, "%.2f",profit));
        }else
        {
            textfieldProfit.setText("0.0");
        }
    }

    @FXML private void onActionButtonSelectImage() { selectImage(); }
    @FXML private void onActionButtonNewFood()     { setProcess(PROCESS.ADDING);  }
    @FXML private void onActionButtonEditFood()    { setProcess(PROCESS.EDITING); }
    @FXML private void onActionButtonUpdateFood()  { updateFood(); }
    @FXML private void onActionButtonSaveFood()    { saveFood();   }
    @FXML private void onActionButtonDeleteFood()  { deleteFood(); }

    @FXML private void onActionButtonNewIngredient() { addNewIngredient(); }
    @FXML private void onActionButtonAddIngredient() { addIngredientToFood(); }
    @FXML private void onActionButtonRemoveIngredient() { removeIngredientFromFood(); }

    //endregion

    public void initialize(URL location, ResourceBundle resources)
    {
        Platform.runLater(()->{
            loadFoodAndIngredients();
            addListenerEvents();
        });
    }

    private HashMap<String, Food> mapFood;
    private HashMap<String, Ingredient> mapIngredients;

    private ObservableList<String> obsFood;
    private ObservableList<String> obsIngredients;
    private ObservableList<ItemRecipe> obsItemRecipe;

    private FilteredList<String> flFood;
    private FilteredList<String> flIngredients;

    private void loadFoodAndIngredients()
    {
        try
        {
            mapFood = foodDAO.getAll();
            mapIngredients = ingredientDAO.getAll();

            obsFood = FXCollections.observableArrayList( mapFood.keySet() );
            obsIngredients = FXCollections.observableArrayList(mapIngredients.keySet());

            flFood = new FilteredList<>(obsFood, s -> true);
            flIngredients = new FilteredList<>(obsIngredients, s -> true);

            listViewFood.setItems(flFood);
            listViewIngredients.setItems(flIngredients);

            //ITEMS FOR RECIPE
            columnDescription.setCellValueFactory( new PropertyValueFactory<>("description") );
            columnUnit.setCellValueFactory( new PropertyValueFactory<>("unit"));
            columnQuantity.setCellValueFactory( new PropertyValueFactory<>("quantity"));
            obsItemRecipe = FXCollections.observableArrayList();
            tableViewIngredients.setItems(obsItemRecipe);
        }
        catch (SQLException e) { FXTool.alertException(e); }
    }

    private void addListenerEvents()
    {
        FXTool.setTextFieldDouble(textfieldCost);
        FXTool.setTextFieldDouble(textfieldProfit);
        FXTool.setTextFieldDouble(textfieldPrice);

        //SEARCH
        textfieldSearchFood.textProperty().
                addListener((obs, old, value) ->
                        flFood.setPredicate(food ->
                                food.contains( value.toUpperCase().trim() )));

        textfieldSearchIngredient.textProperty().
                addListener((obs, old, value) ->
                        flIngredients.setPredicate( ingredient ->
                                ingredient.contains( value.toUpperCase().trim() )));

        //SELECTED ITEM PROPERTY
        listViewFood.getSelectionModel().selectedItemProperty()
                .addListener((observable) -> selectFood());

        listViewFood.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.UP && listViewFood
                    .getSelectionModel().isSelected(0))
                textfieldSearchFood.requestFocus(); });

        listViewIngredients.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.UP && listViewIngredients
                    .getSelectionModel().isSelected(0))
                textfieldSearchIngredient.requestFocus();
            else if(event.getCode() == KeyCode.ENTER)
                addIngredientToFood(); });

        listViewIngredients.setOnMouseClicked(event -> {
            if(event.getClickCount()==2 &&
                    event.getButton()== MouseButton.PRIMARY)
                            addIngredientToFood();
        });

        //TEXTFIELD PRICE
        checkBoxPrice.selectedProperty().addListener((obs, old, value) -> textfieldPrice.setDisable(!value));
        checkBoxPrice.setSelected(false);

        listViewFood.getSelectionModel().selectFirst();
        textfieldSearchFood.requestFocus();
    }

    //region FOOD METHODS

    /**
     * Obtiene el Food del HashMap y rellena el formulario.
     * Tambien la tabla de ingredientes.
     */
    private void selectFood()
    {
        String name = listViewFood.getSelectionModel().getSelectedItem();

        if(name !=null)
        {
            Food food = mapFood.get(name);

            textfieldID.setText    ( food.getFoodID() + "");
            textfieldName.setText  ( food.getName()   + "");
            textfieldCost.setText  ( String.format(Locale.US, "%.2f", food.getCost())   );
            textfieldProfit.setText( String.format(Locale.US, "%.2f", food.getProfit()) );
            textfieldPrice.setText ( String.format(Locale.US, "%.2f", food.getPrice())  );
            showImage(food.getImage());

            obsItemRecipe.clear();
            obsItemRecipe.addAll(food.getRecipe());

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
            labelPathImage.setText("");
            imageView.setImage(null);
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
                if(!pathImage.trim().isEmpty())
                {
                    //COPIAR IMAGEN Y SETEAR
                    String copyImage = copyImageToHomeFood(removeProtocolImage(pathImage), name);
                    if(copyImage != null)
                    {
                        labelPathImage.setText(addProtocolImage( copyImage ));
                        imageView.setImage( new Image(addProtocolImage( copyImage )) );
                    }
                }

                Food food = new Food(0, name,
                        Double.parseDouble(cost),
                        Double.parseDouble(profit),
                        Double.parseDouble(price),
                        labelPathImage.getText(),
                        true
                );
                System.out.println("image saved: "+ labelPathImage.getText());

                //TODO verificar que no haya con el mismo nombre y exist
                foodDAO.save(food);
                obsFood.add(name);
                mapFood.put(name, food);

                textfieldID.setText( food.getFoodID() + "" );
                listViewFood.requestFocus();
                listViewFood.getSelectionModel().select(food.getName());
            }
            catch (SQLException e) { FXTool.alertException(e); }
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

                if(!labelPathImage.getText().trim().isEmpty())
                    updateImage( food, labelPathImage.getText());

                foodDAO.update(food);

                if(!obsFood.contains(name))
                {
                    obsFood.remove(oldName);
                    obsFood.add   (name);
                }
                mapFood.put(name, food);

                listViewFood.requestFocus();
                listViewFood.getSelectionModel().select(name);
                setProcess(PROCESS.VIEWING);
            }
            catch (SQLException | NotFoundException e) { FXTool.alertException(e); }
        }
    }
    /** Metodo auxiliar de updateFood() */
    private void updateImage(Food food, String newImagePath)
    {
        String currentImage = food.getImage();
        //si se cambio de foto
        if (!newImagePath.equals(currentImage))
        {
            String copyNewImage = copyImageToHomeFood(removeProtocolImage(newImagePath), food.getName());
            if (copyNewImage != null) //SI SE LOGRO HACER LA COPIA
            {
                if (!addProtocolImage(copyNewImage).equals(currentImage)) //VERIFICA SI CAMBIARON EL NOMBRE DE LA COMIDA
                    deleteFile(removeProtocolImage(currentImage));

                food.setImage(addProtocolImage(copyNewImage));
                labelPathImage.setText(food.getImage());
                imageView.setImage( new Image( food.getImage() ) );
            }
        }
    }

    //TODO limpiar luego de eliminar
    private void deleteFood()
    {
        String foodID = textfieldID.getText().trim();
        String name = textfieldName.getText().trim();

        if(checkFields(foodID, name))
        {
            try
            {
                Alert alert = new Alert(
                        Alert.AlertType.WARNING,
                        "Eliminación de plato.",
                        ButtonType.YES,
                        ButtonType.CANCEL
                );
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/alerts.css").toExternalForm());

                alert.setHeaderText("¿Estas seguro que desea eliminar \na '"+name+"'?");
                alert.showAndWait();

                if(alert.getResult()==ButtonType.YES)
                {
                    foodDAO.remove(  Integer.parseInt(foodID));
                    mapFood.remove(name);
                    obsFood.remove(name);
                    deleteFile(removeProtocolImage(labelPathImage.getText() ));

                    listViewFood.requestFocus();
                    listViewFood.getSelectionModel().selectNext();
                }
            }
            catch (Exception e) { FXTool.alertException(e); }
        }
    }

    //endregion

    //region INGREDIENT METHODS

    /**
     * Crear un nuevo ingrediente
     */
    private void addNewIngredient()
    {
        try
        {
            Ingredient newIngredient = getNewIngredient();
            if(newIngredient!=null)
            {
                ingredientDAO.save(newIngredient);
                mapIngredients.put(newIngredient.getDescription(), newIngredient);
                obsIngredients.add(newIngredient.getDescription());
            }

        } catch (Exception e) { FXTool.alertException(e); }
    }

    /**
     * Añadir un ingrediente al plato de comida.
     */
    private void addIngredientToFood()
    {
       textMessageItemRecipe.setText("");

       int foodID = Integer.parseInt(textfieldID.getText());
       String foodName = textfieldName.getText();
       String description = listViewIngredients.getSelectionModel().getSelectedItem();

       if(description!=null)
       {
           Ingredient ingredient = mapIngredients.get(description);

           if(!obsItemRecipe.contains(new ItemRecipe(ingredient)))
           {
               Integer quantity = getQuantity(ingredient.getUnit());
               if (quantity != null) {
                   try
                   {
                       ItemRecipe itemRecipe = new ItemRecipe(ingredient, quantity);

                       foodDAO.addItemRecipe(foodID, itemRecipe);
                       mapFood.get(foodName).addItemRecipe(itemRecipe);
                       obsItemRecipe.add(itemRecipe);
                   }
                   catch (SQLException e) { FXTool.alertException(e); }
               }
           }
           else {
               textMessageItemRecipe.setText("Ya haz agregado este ingrediente!");
           }
       }
    }

    private void removeIngredientFromFood()
    {
        int foodID = Integer.parseInt(textfieldID.getText());
        String nameFood = textfieldName.getText();
        ItemRecipe itemRecipe = tableViewIngredients.getSelectionModel().getSelectedItem();

        if(itemRecipe != null)
        {
            try
            {
                foodDAO.removeItemRecipe(foodID, itemRecipe);
                mapFood.get(nameFood).removeItemRecipe(itemRecipe.getDescription());
                obsItemRecipe.remove(itemRecipe);
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }
    }

    //endregion

    private void setProcess(PROCESS process)
    {
        checkBoxPrice.setSelected(false);
        if(process.equals(PROCESS.VIEWING) )
        {
            textfieldName.setDisable(true);
            textfieldCost.setDisable(true);
            textfieldProfit.setDisable(true);
            textfieldPrice.setDisable(true);

            buttonDelete.setDisable (false);
            buttonEdit.setDisable   (false);
            buttonSave.setDisable   (true);
            buttonUpdate.setDisable (true);

            buttonSelectImage.setVisible(false);
            tabIngredient.setDisable(false);
        }
        else if(process.equals(PROCESS.EDITING))
        {
            textfieldName.setDisable(false);
            textfieldCost.setDisable(false);
            textfieldProfit.setDisable(false);

            buttonDelete.setDisable (true);
            buttonSave.setDisable   (true);
            buttonUpdate.setDisable (false);
            buttonEdit.setDisable   (true);

            buttonSelectImage.setVisible(true);
            tabIngredient.setDisable(false);
            textfieldName.requestFocus();
        }
        else if(process.equals(PROCESS.ADDING))
        {
            textfieldName.setDisable(false);
            textfieldCost.setDisable(false);
            textfieldProfit.setDisable(false);
            textfieldPrice.setDisable(true);

            textfieldID.setText("");
            textfieldName.setText   ("");
            textfieldCost.setText   ("0");
            textfieldProfit.setText ("0");
            textfieldPrice.setText  ("0");

            buttonDelete.setDisable (true);
            buttonSave.setDisable   (false);
            buttonUpdate.setDisable (true);
            buttonEdit.setDisable   (true);

            buttonSelectImage.setVisible(true);
            labelPathImage.setText("");
            imageView.setImage(null);
            tabIngredient.setDisable(true);

            textfieldName.requestFocus();
        }

        textMessageItemRecipe.setText("");
    }

}
