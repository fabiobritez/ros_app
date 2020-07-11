package ar.com.doctatech.user.login;

import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.shared.utilities.FXPath;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.utilities.SecurityUtil.encrypt;

public class ResetPasswordController
        extends RecoveryServices
        implements Initializable
{

    //region FXML REFERENCES
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private PasswordField newPasswordField, repeatPasswordField;

    @FXML
    private Text textMessage,textErrors;

    @FXML
    public void handleButtonSave(ActionEvent event)
    {
        updatePassword();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    //endregion


    private String username;

    private void updatePassword()
    {
        String newPassword = newPasswordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        if(newPassword.equals(repeatPassword) && !newPassword.isEmpty())
        {
            try
            {
                updatePassword(username, encrypt(newPassword));

                FXTool.alertInformation("RESULTADO","Contraseña cambiada exitosamente");
                FXTool.replaceSceneContent(anchorPane, FXPath.LOGIN);
            }
            catch (IOException | NotFoundException | SQLException exception)
            {
                FXTool.alertException(exception);
            }
        }
        else
        {
            FXTool.textError(textErrors,"Ambos campos deben coincidir");
        }
    }

    public void setUser(String username)
    {
        this.username = username;
        textMessage.setText("Cambiar la contraseña: '"+username+"'");
    }

}
