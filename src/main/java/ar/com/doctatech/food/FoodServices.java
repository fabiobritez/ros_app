package ar.com.doctatech.food;

import ar.com.doctatech.food.model.FoodDAO;
import ar.com.doctatech.food.model.FoodDAOMySQL;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.shared.utilities.FileUtil;
import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.stock.ingredient.IngredientDAO;
import ar.com.doctatech.stock.ingredient.IngredientDAOMySQL;
import ar.com.doctatech.stock.ingredient.Unit;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static ar.com.doctatech.shared.utilities.FileUtil.createHomeFoodIfNoExists;
import static ar.com.doctatech.shared.utilities.FileUtil.getExtension;
import static javafx.collections.FXCollections.observableArrayList;

public class FoodServices {
    IngredientDAO ingredientDAO;
    FoodDAO foodDAO ;

    {
        ingredientDAO = new IngredientDAOMySQL();
        foodDAO = new FoodDAOMySQL();
    }
    protected Ingredient getNewIngredient()
    {
        Dialog<Ingredient> dialog = new Dialog<>();

        dialog.setTitle("Ingresa un nuevo ingrediente");

        dialog.setHeaderText("Escribe los datos requeridos  \n" +
                "Luego presiona guardar o cancelar");

        dialog.setResizable(true);

        Label labelDescription = new Label("Descripcion: ");
        Label labelStock = new Label("Stock actual: ");
        Label labelStockMin = new Label("Stock minimo: ");
        Label labelUnit = new Label("Unidad medida: ");

        TextField textFieldDescription = new TextField();
        TextField textFieldStock = new TextField("0");
        TextField textFieldStockMin = new TextField("0");
        ComboBox<Unit> comboBoxUnits = new ComboBox<>();
        comboBoxUnits.setItems(
                observableArrayList( Unit.values() )
        );
        comboBoxUnits.getSelectionModel().select(Unit.QUANTITY);

        GridPane grid = new GridPane();
        grid.add(labelDescription, 1,1);
        grid.add(textFieldDescription,2,1);

        grid.add(labelUnit,1,2);
        grid.add(comboBoxUnits,2,2);

        grid.add(labelStock,1,3);
        grid.add(textFieldStock,2,3);

        grid.add(labelStockMin,1,4);
        grid.add(textFieldStockMin,2,4);



        dialog.getDialogPane().setContent(grid);


        ButtonType buttonTypeGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeGuardar);

        Node saveButton = dialog.getDialogPane().lookupButton(buttonTypeGuardar);
        saveButton.setDisable(true);

        textFieldDescription.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(
                newValue.trim().isEmpty()
        ));

        textFieldStock.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[0-9]*")){
                textFieldStock.setText(oldValue);
                saveButton.setDisable(oldValue.trim().isEmpty());
            }else
            {
                saveButton.setDisable(newValue.trim().isEmpty());
            }
        });

        textFieldStockMin.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[0-9]*"))
            {
                textFieldStockMin.setText(oldValue);
                saveButton.setDisable(oldValue.trim().isEmpty());
            }else
                {
                saveButton.setDisable(newValue.trim().isEmpty());
            }
        });


        dialog.setResultConverter(buttonType ->
        {
            if(buttonType == buttonTypeGuardar)
            {
                 return new Ingredient(
                                textFieldDescription.getText(),
                                Integer.parseInt(textFieldStock.getText().trim()),
                                Integer.parseInt(textFieldStockMin.getText().trim()),
                                comboBoxUnits.getSelectionModel().getSelectedItem().toString()
                        );
            }
            return null;
        });

        Optional<Ingredient> result = dialog.showAndWait();

        return result.orElse(null);
    }

    protected File getFromImageChooser(Node node)
    {
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter
                ("Image Files", "*.jpg", "*.png", "*.jpeg");

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(imageFilter);

        return chooser.showOpenDialog(node.getScene().getWindow());
    }
    protected String copyImageToHomeFood(String imagePath, String newName)
    {
        File fileOriginal = new File(imagePath);

        String newImagePath = createHomeFoodIfNoExists() +
                        File.separator + newName +
             getExtension(fileOriginal.getName()).get();

        File fileDestination = new File(newImagePath);

        try
        {
            FileUtil.copyFile(fileOriginal, fileDestination);
            return newImagePath;
        }
        catch (IOException exception)
        {
            FXTool.alertException(exception);
        }
        return null;
    }

    public String addProtocolImage(String imageWithoutProtocol)
    {
        return "file:"+imageWithoutProtocol;
    }

    public String removeProtocolImage(String imageWithProtocol)
    {
        return imageWithProtocol.substring(5);
    }

}
