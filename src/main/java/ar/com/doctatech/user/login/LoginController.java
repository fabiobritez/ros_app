package ar.com.doctatech.user.login;

import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.shared.exceptions.PrivilegeException;
import ar.com.doctatech.shared.utilities.FXPath;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;

/**
 * Controller de la interface grafica del Login.
 */
public class LoginController
        implements Initializable
{

    //region FXML REFERENCES
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField textfieldUsername;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Text txtErrors;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        textfieldUsername.requestFocus();
    }

    @FXML
    public void onActionButtonLogin()
    {
        login();
    }

    @FXML
    public void onKeyPressedPassword(KeyEvent event)
    {
        if(event.getCode() == KeyCode.ENTER) { login(); }
    }

    @FXML
    public void onActionButtonSettings()
    {
        openConnectionSettings();
    }

    @FXML
    public void onActionEventForgetPass()
    {
        openPasswordRecovery();
    }

    //endregion

    //region GUI METHODS

    /**
     * Metodo encargado de iniciar sesión.
     * Comprueba su los campos de usuario y password esten rellenados.
     * Maneja todas las excepciones que se pueden dar en el proceso.
     * Inicia sesión en el caso de que no haya ningun inconveniente
     * y abre la ventana del HOME.
     */
    private void login()
    {
        String user = textfieldUsername.getText();
        String password = passwordField.getText();

        if(checkFields(user, password))
        {
            try
            {
                UserSession.login(user, password);
                FXTool.replaceSceneContent(anchorPane, FXPath.HOME);
            }
            catch (NotFoundException exception)
            {
                FXTool.textError(txtErrors,exception.getMessage());
            }
            catch (PrivilegeException e)
            {
                FXTool.textError(txtErrors, e.getMessage());
            }
            catch (Exception exception)
            {
                FXTool.alertException(exception);
            }
        }
        else
        {
            FXTool.textError(txtErrors,"ERROR: Debes rellenar los campos de Usuario y Contraseña");
        }
    }

    /**
     * Metodo encargado de cambiar la Scene para recuperar la contraseña
     * y manejar posibles.
     */
    private void openPasswordRecovery()
    {
        try
        {
            FXTool.replaceSceneContent(anchorPane, FXPath.USER_VERIFICATION);
        }
        catch (Exception exception)
        {
            FXTool.alertException(exception);
        }
    }

    /**
     * Metodo encargado de abrir la ventana de configuración de las bases de datos.
     * Antes de hacerlo cierra la ventana actual.
     * Maneja los errores correspondientes.
     */
    private void openConnectionSettings()
    {
        try
        {
            txtErrors.getScene().getWindow().hide();
            FXTool.newScene(FXPath.DATABASE_SETTINGS);
        }
        catch (Exception exception)
        {
           FXTool.alertException(exception);
        }
    }

    //endregion

}
