package ar.com.doctatech.user.login;

import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.shared.utilities.FXPath;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.shared.utilities.GoogleMail;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserAuthenticationController
        extends RecoveryServices
        implements Initializable
{

    //region FXML REFERENCES
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField txtUser, txtCodeEntered;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Text textStatus, textCodeVerification,textUser;

    @FXML
    private Button buttonCodeVerification, buttonUserCheck;


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        setState(State.STARTING);
    }

    @FXML
    public void onActionButtonUserCheck()
    {
        sendRecoveryCodeTo( txtUser.getText() );
    }

    @FXML
    public void onActionButtonCodeCheck()
    {
        verifyCode( txtCodeEntered.getText() );
    }

    @FXML
    public void onActionButtonBack()
    {
        backToLogin();
    }
    //endregion

    //region GUI METHODS
    private String generatedCode, email;
    private int attemptsMade = 0;

    private boolean verifyUser(String username)
    {
        if(!username.isEmpty())
        {
            try
            {
                email = getUser(username).getEmail();
                return true;
            }
            catch (SQLException e) { FXTool.alertException(e); }
            catch (NotFoundException e) {FXTool.textError(textStatus, e.getMessage()); }
        }
        else { FXTool.textError(textStatus,"ERROR: Ingrese un usuario"); }
        return false;
    }

    private void sendRecoveryCodeTo(String username)
    {

       Task<Void> taskSend = new Task<Void>(){
           @Override
           protected Void call() throws Exception {
               GoogleMail.sendEmail( email, generatedCode );
               return null;
           }
       };
       taskSend.setOnFailed(e->
       {
           FXTool.alertException((Exception) taskSend.getException());
           setState(State.STARTING);
       });
       taskSend.setOnSucceeded(event -> setState(State.VERIFYING_CODE));

        if(verifyUser(username))
        {
            generatedCode = generateRandomCode();
            setState(State.SENDING_EMAIL);
            new Thread(taskSend).start();
        }
    }

    private void verifyCode(String codeEntered)
    {
        int attemptsAllowed = 3;
        if(attemptsMade < attemptsAllowed)
        {
            attemptsMade++;
            if(codeEntered.equals(generatedCode))
            {
                try
                {
                    ResetPasswordController STNPController = FXTool.replaceScene(anchorPane, FXPath.SET_NEW_PASSWORD);
                    STNPController.setUser(txtUser.getText());
                }
                catch (IOException e) { FXTool.alertException(e); }
            }
            else { FXTool.alertException(new Exception("CODIGO INCORRECTO")); }
        }
        else
        {
            setState(State.STARTING);
            FXTool.textError(textStatus, "Haz superado el número de intentos posibles.");
        }

    }

    private void backToLogin()
    {
        try
        {
            FXTool.replaceSceneContent(anchorPane, FXPath.LOGIN);
        }
        catch (IOException e) { FXTool.alertException(e); }
    }

    private void setState( State state )
    {
        if(state.equals(State.STARTING))
        {
            attemptsMade = 0;
            txtUser.setText("");

            textUser.setVisible(true);
            txtUser.setVisible(true);
            buttonUserCheck.setVisible(true);

            textStatus.setVisible(false);
            progressIndicator.setVisible(false);

            txtCodeEntered.setText("");
            txtCodeEntered.setVisible(false);
            textCodeVerification.setVisible(false);
            buttonCodeVerification.setVisible(false);

        }
        else if(state.equals(State.SENDING_EMAIL))
        {
            textUser.setVisible(false);
            txtUser.setVisible(false);
            buttonUserCheck.setVisible(false);

            FXTool.textInformation(textStatus, "Enviando código al email...");
            progressIndicator.toFront();
            progressIndicator.setVisible(true);
        }
        else if(state.equals(State.VERIFYING_CODE))
        {
            textStatus.setVisible(false);
            progressIndicator.setVisible(false);

            txtCodeEntered.toFront();
            txtCodeEntered.setVisible(true);
            textCodeVerification.toFront();
            textCodeVerification.setVisible(true);
            buttonCodeVerification.toFront();
            buttonCodeVerification.setVisible(true);
        }
    }

    //endregion

}
