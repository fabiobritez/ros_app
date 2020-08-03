package ar.com.doctatech.shared.utilities;

import ar.com.doctatech.RosApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Proporciona metodos utiles para mostrar errores y mensajes en la interfaces grafica
 * de una manera más simple.
 */
public class FXTool
{

    //region SCENES MANAGER

    /**
     * Crea una nueva ventana con la vista pasada como parametro,
     * y luego la muestra.
     * @param view Vista o ruta del archivo {@code FXML}.
     * @throws IOException Excepcion en el caso de un error
     * en la carga de la vista.
     */
    public static void newScene(String view)
            throws IOException
    {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(RosApp.class.getResource(view));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    /**
     * Reemplaza el contenido que había en una Scene.
     * Obtiene la Scene a partir de un Node ubicado en la misma, que pasemos como parametro.
     * Cambia el contenido por el que hay en la vista, que pasemos como parametro.
     * @param node Nodo cualquiera de la Scene que queremos reemplazar.
     * @param view  Vista o ruta del archivo {@code FXML}.
     * @throws IOException Excepcion en el caso de un error
     * en la carga de la vista.
     */
    public static void replaceSceneContent(Node node, String view)
            throws IOException
    {
        Stage stage = (Stage)node.getScene().getWindow();
        Parent page = FXMLLoader.load(RosApp.class.getResource(view));

        Scene scene = new Scene(page);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    /**
     * Identico a {@link FXTool#replaceSceneContent(Node, String)}
     * Con la diferencia que devuelve el {@code Controller} de la nueva Scene.
     * @param node Nodo cualquiera de la Scene que queremos reemplazar.
     * @param view Vista o ruta del archivo {@code FXML}.
     * @param <T> Controller correspondiente.
     * @return Controller de la nueva Scene.
     * @throws IOException Excepcion en el caso de un error
     *      en la carga de la vista.
     */
    public static <T> T replaceScene(Node node, String view)
            throws IOException
    {
        Stage stage = (Stage)node.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(RosApp.class.getResource(view));
        Parent page = loader.load();

        Scene scene = new Scene(page);
        stage.setScene(scene);
        stage.sizeToScene();

        return loader.getController();
    }

    //endregion

    //region ERROR MANAGER

    /**
     * Muestra la Exception pasada como parametro en un {@code Alert}
     * @param exception Exception que se quiere mostrar.
     */
    public static void alertException(Exception exception)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Ha ocurrido un error:");
        alert.setContentText(exception.getMessage());
        // Create expandable Exception.
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        String exceptionText = stringWriter.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);


        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        alert.show();

       // Alert alert = new Alert( Alert.AlertType.ERROR, exception.toString(), ButtonType.OK );
    }

    /**
     * Escribe el error en el {@code Text}, lo colorea de rojo
     * y lo hace visible.
     * @param text Text en el que se quiere escribir.
     * @param err Error que ocurrió.
     */
    public static void textError(Text text, String err)
    {
        text.setText(err);
        text.setStyle("-fx-fill : red;");
        text.setVisible(true);
    }

    //endregion

    //region INFORMATION MANAGER

    /**
     * Muestra la información pasada como parametro en un {@code Alert}
     *
     * @param header Titulo o cabecera de la información
     * @param content Contenido de la información
     */
    public static void alertInformation(String header, String content)
    {
        Alert alert = new Alert( Alert.AlertType.CONFIRMATION, content, ButtonType.OK );
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    /**
     * Escribe la información en el {@code Text}, lo colorea de negro
     * y lo hace visible.
     * @param text Text en el que se quiere mostrar.
     * @param message El mensaje que desea mostrar.
     */
    public static void textInformation(Text text, String message)
    {
        text.setText(message);
        text.setStyle("-fx-fill: black;");
        text.setVisible(true);
    }

    //endregion


    public static Parent getParent(String urlNode) throws IOException
    {
        return FXMLLoader.load(RosApp.class.getResource(urlNode));
    }

    public static void setTextFieldDouble(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("^\\d*\\.?\\d*$"))
            {
                textField.setText(oldValue);
            }
        });

    }

    public static void setTextFieldInteger(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("-?[0-9]*"))
            {
                textField.setText(oldValue);
            }
        });
    }
}
