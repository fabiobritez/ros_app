package ar.com.doctatech.user.login;

import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.shared.utilities.FXPath;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.shared.utilities.GoogleMail;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import javax.mail.MessagingException;
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
    public void handleButtonUserCheck(ActionEvent event)
    {
        sendRecoveryCodeTo( txtUser.getText() );
    }

    @FXML
    public void handleButtonCodeCheck(ActionEvent event)
    {
        verifyCode( txtCodeEntered.getText() );
    }

    @FXML
    public void handleButtonBack(ActionEvent event)
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
            catch (SQLException sqlException)
            {
                FXTool.alertException(sqlException);
            }
            catch (NotFoundException e)
            {
                FXTool.textError(textStatus, e.getMessage());
            }
        }
        else
        {
            FXTool.textError(
                    textStatus,
                    "ERROR: Ingrese un usuario."
            );
        }
        return false;
    }

    private void sendRecoveryCodeTo(String username)
    {
        if(verifyUser(username))
        {
                setState(State.SENDING_EMAIL);

                Platform.runLater( ()->
                        {
                            try
                            {
                                generatedCode = generateRandomCode();
                                GoogleMail.sendEmail( email, generatedCode );
                                setState(State.VERIFYING_CODE);
                            }
                            catch (MessagingException messagingException)
                            {
                                FXTool.alertException(messagingException);
                                setState(State.STARTING);
                            }
                        }
                );
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
                catch (IOException exception)
                {
                    FXTool.alertException(exception);
                }
            }
            else
            {
                FXTool.alertException(new Exception("CODIGO INCORRECTO"));
            }
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
        catch (IOException exception)
        {
            FXTool.alertException(exception);
        }
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
