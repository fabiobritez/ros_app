package ar.com.doctatech.shared.db;

import ar.com.doctatech.RosApp;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

public class DatabaseSettingsController
        extends DatabaseSettingsServices
        implements Initializable {

    @FXML
    private ComboBox<String> comboDBMS, comboHost;
    @FXML
    private TextField txtPort, txtDatabase, txtUser,txtProperties;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextArea txtAreaLog;

    private ObservableList<String> dbmsList, hostnametList;
    private HashMap<String, String> devices;

    /**
     * 1. Cargar componentes escenciales, combos,listas etc.
     * 2. Rellenar o tratar de rellenar los nodos del formulario.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        loadCombos();
        loadSettings();
    }

    @FXML
    void close(ActionEvent event) {
        txtUser.getScene().getWindow().hide();
    }

    /**
     * 1. Verifica que los campos requeridos tengan valores establecidos distintos de null o "".
     * 2. Guarda la configuración del formulario en el archivo de configuración.
     * 3. Prueba si la conexión se puede realizar.
     * 4. Reinicia el programa en caso de lograr la conexión.
     *
     * Si no se cumple con alguno de los primeros 3 puntos,
     * lanza el mensaje en caso de un error.
     */
    @FXML
    private void connect(ActionEvent event)
    {
        //1
        if(requiredFieldsAreFilled()){
           try {
               //2
               setConnectionURL(
                       comboDBMS.getSelectionModel().getSelectedItem(),
                       comboHost.getSelectionModel().getSelectedItem(),
                       txtPort.getText(),
                       txtDatabase.getText(),
                       txtUser.getText(),
                       txtPassword.getText(),
                       txtProperties.getText()
               );

               //3
               if(DatabaseConnection.isConnectable()){
                   addLog("CONEXION A BASE DE DATOS EXITOSA.");

                   //4
                   addLog( "REINICIANDO LA APLICACIÓN..." );
                   txtUser.getScene().getWindow().hide();
                   DatabaseConnection.disconnect();

                   Platform.runLater( () -> new RosApp().start( new Stage() ) );
               }
           }
           //4
           catch (IOException exception)
           {
               addLog(exception.toString());
           }
           catch (SQLException exception)
           {
               addLog( exception.getSQLState() +"\n"+
                       exception.getLocalizedMessage() );
           }
       }
        else
        {
           addLog("RELLENAR CAMPOS REQUERIDOS: \n" +
                   "-Gestor de base de datos \n" +
                   "-Host \n" +
                   "-Puerto \n" +
                   "-Nombre de base de datos \n" +
                   "-Usuario");
        }
    }

    //region AUXILIARY METHODS
    /**
     * 1. Inicializar todas las listas/colecciones que se van a utilizar en el formulario.
     * 2. Cargar o rellenar las listas/colecciones con los correspondientes datos.
     * 3. Establecer los valores en los elementos del formulario.
     */
    private void loadCombos()
    {
        //1
        dbmsList = FXCollections.observableArrayList();
        hostnametList = FXCollections.observableArrayList();
        devices = new HashMap<String, String>();

        //2
        dbmsList.add("mysql");
        dbmsList.add("postgresql");
        dbmsList.add("mariadb");
        dbmsList.add("oracle");

        try {
            devices = DatabaseSettingsServices.getDevicesConnectedOnLAN();
            hostnametList.setAll(devices.values());
        } catch (SocketException e) {
            addLog(e.toString());
        }

        //3
        comboDBMS.setItems(dbmsList);
        comboHost.setItems(hostnametList);
    }

    /**
     * Trata de acceder al archivo de configuración y cargar los datos.
     * Si no es posible muestra el error en txtAreaLog
     */
    private void loadSettings(){

        try {
                Properties settings = getDatabaseConnectionSettings();

                comboDBMS.getSelectionModel().select( settings.getProperty(RDBMS_KEY) );
                comboHost.getSelectionModel().select( settings.getProperty(HOST_KEY) );
                txtPort.setText( settings.getProperty(PORT_KEY) );
                txtDatabase.setText( settings.getProperty(DB_NAME_KEY) );
                txtUser.setText( settings.getProperty(USER_KEY) );
                txtPassword.setText( settings.getProperty(PASSWORD_KEY) );
                txtProperties.setText( settings.getProperty(PROPERTIES_KEY) );
                DatabaseConnection.isConnectable();

        }
        catch (IOException | SQLException exception)
        {
            addLog(exception.toString());
        }

    }

    /**
     * Metodo auxiliar que agrega los errores al TextArea y los separa con un formato.
     * @param message Error o mensaje que desea escribir
     */
    private void addLog(String message)
    {
        txtAreaLog.appendText("\n"+ message +
                "\n_________________________________________________________\n");
    }

    /**
     * Verifica que los campos requeridos para la conexion tengan valores distintos de {@code null} o ""
     * - Gestor de base de datos
     * - Host
     * - Puerto
     * - Nombre de base de datos
     * - Usuario
     *
     * @return true si los campos requeridos tienen valores distintos de null o ""
     */
    private boolean requiredFieldsAreFilled(){
        return ( (comboDBMS.getSelectionModel().getSelectedItem() != null) &&
                (comboHost.getSelectionModel().getSelectedItem() != null) &&
                !txtPort.getText().isEmpty() &&
                !txtDatabase.getText().isEmpty() &&
                !txtUser.getText().isEmpty() );
    }

    //endregion

}
