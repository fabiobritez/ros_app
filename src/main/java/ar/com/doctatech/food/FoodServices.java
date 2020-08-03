package ar.com.doctatech.food;

import ar.com.doctatech.food.model.FoodDAO;
import ar.com.doctatech.food.model.FoodDAOMySQL;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.shared.utilities.FileUtil;
import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.stock.ingredient.IngredientDAO;
import ar.com.doctatech.stock.ingredient.IngredientDAOMySQL;
import ar.com.doctatech.stock.ingredient.Unit;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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

    protected Integer getQuantity(String unit)
    {
        Dialog<Integer> dialog = new Dialog<>();

        dialog.setTitle("Añade la cantidad del ingrediente");

        dialog.setHeaderText("Ingresa la cantidad:");
        dialog.setResizable(true);

        Label labelDescription = new Label("Cantidad : ");
        TextField textFieldStock = new TextField();
        ComboBox<String> comboBoxUnits = new ComboBox<>();
        comboBoxUnits.getItems().add(unit);
        comboBoxUnits.getSelectionModel().select(unit);
        //comboBoxUnits.setDisable(true);

        Text textDiv = new Text("0 " + getDivUnit(unit));

        GridPane grid = new GridPane();
        grid.add(labelDescription, 1,1);
        grid.add(textFieldStock,2,1);
        grid.add(comboBoxUnits, 3, 1);
        grid.add(textDiv,2,2);

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
            updateDiv1000(textFieldStock,textDiv,unit);
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

        return result.orElse(null);
    }

    protected void updateDiv1000(TextField textField, Text text, String unit)
    {
        if(!getDivUnit(unit).equals("unidades"))
        {
            if(!textField.getText().isEmpty())
            {
                double stockmin = Integer.parseInt(textField.getText());
                text.setText(String.format("%.2f", stockmin / 1000) +" "+ getDivUnit(unit));
            }else
            {
                text.setText("0.00" + getDivUnit(unit));
            }
        }else
        {
            text.setText(textField.getText() + " unidades");
        }
    }

    private String getDivUnit(String unit)
    {
        if(unit.equals(Unit.GRAMS.toString()))
        {
            return "kg";
        }
        else if(unit.equals(Unit.MILLILITERS.toString()))
        {
            return "litros";
        }
        return "unidades";
    }


    protected int getQuantityIngredient()
    {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Escribe la cantidad que deseas agregar");
        dialog.setHeaderText("Enter some text, or use default value.");

        TextField textField = dialog.getEditor();

        textField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if(!newValue.matches("[0-9]*"))
            {
                textField.setText(oldValue);
            }
        }
        );

        Optional<String> result = dialog.showAndWait();
        String entered = "1";

        if (result.isPresent())
        {
            if(result.get().trim().isEmpty())
                entered = "1";
            else
                entered = result.get();
        }


        return Integer.parseInt(entered);
    }

    protected File getFromImageChooser(Node node)
    {
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter
                ("Image Files", "*.jpg", "*.png", "*.jpeg");

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(imageFilter);

        return chooser.showOpenDialog(node.getScene().getWindow());
    }


    /**
     * Realiza una copia de la imagen pasada como parametro con el nombre indicado.
     * Si
     * @param imagePath Ruta de la imagen que se desea copiar
     * @param newName Nombre del archivo destino
     * @return La ruta del archivo copiado o null si no se pudo realizar.
     */
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
