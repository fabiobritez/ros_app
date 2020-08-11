package ar.com.doctatech.user.dialogs;

import ar.com.doctatech.customer.model.Customer;
import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.shared.exceptions.PrivilegeException;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.UserSession;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.sql.SQLException;
import java.util.Optional;

public class AdminUserDialog extends Dialog<Boolean>
{

    TextField textfieldUser;
    PasswordField passwordfield;
    Label labelUser, labelPassword, labelMessage, labelTitle;
    Button buttonCheck;
    public AdminUserDialog()
    {
        getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dialogs.css").toExternalForm());
        getDialogPane().getStyleClass().add("panel-back");


        setTitle("Autenticación");
        labelTitle = new Label("Necesitas autorización de administrador para cancelar." +
                        "\nIngresa el usuario y contraseña");
        labelUser = new Label("Usuario");
        textfieldUser = new TextField();
        textfieldUser.setPrefSize(200,28);
        labelPassword = new Label("Contraseña");
        passwordfield = new PasswordField();
        passwordfield.setPrefSize(200,28);
        labelMessage = new Label("");
        labelMessage.setPrefSize(280,35);
        labelMessage.setWrapText(true);
        buttonCheck = new Button("Verificar");
        buttonCheck.setPrefSize(280,28);

        GridPane grid = new GridPane();
        grid.add(labelUser,0,0 );
        grid.add(textfieldUser, 1,0);
        grid.add(labelPassword, 0,1);
        grid.add(passwordfield, 1,1);
        grid.setPadding(new Insets(8,8,8,8));

        VBox vBox = new VBox(labelTitle, grid, labelMessage, buttonCheck);
        vBox.setSpacing(10);
        getDialogPane().setContent(vBox);

        addListenersEvents();

        Window   window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        textfieldUser.requestFocus();
    }
    private void addListenersEvents()
    {
        passwordfield.setOnKeyPressed(event -> {
            labelMessage.setText("");
            if(event.getCode()== KeyCode.ENTER)
            {
               check();
            }
        });
        buttonCheck.setOnAction(event -> check());

    }

    private boolean check()
    {
        String user = textfieldUser.getText().trim().toUpperCase();
        String pass = passwordfield.getText().trim();

        if(user.isEmpty() || pass.isEmpty())
        {
            labelMessage.setText("Completá el campo vació");
        }else {
            try {
                setResult( UserSession.hasAdminPrivilege(user,pass) );
                close();
            } catch (NotFoundException e) {
                labelMessage.setText(e.getMessage());
            } catch (SQLException e) {
                FXTool.alertException(e);
            } catch (PrivilegeException e) {
                labelMessage.setText(e.getMessage());
            }
        }
        return false;
    }


    public Boolean showAndStop()
    {
        this.getDialogPane().requestFocus();
        textfieldUser.requestFocus();
        return showAndWait().orElse(false);
    }
}
