package ar.com.doctatech.home;

import static ar.com.doctatech.shared.utilities.FXPath.*;
import static ar.com.doctatech.user.model.UserRole.*;

import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.UserSession;
import ar.com.doctatech.user.model.UserRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class HomeController implements Initializable
{

    //region FXML REFERENCES
    @FXML
    private BorderPane borderPane;

    @FXML
    private MenuButton menuButtonUser;

    @FXML
    private void handleLogoutButton(ActionEvent event)
    {
        logout();
    }

    @FXML
    private void handleUsersButton(ActionEvent event) { selectModule(usersParent); }

    @FXML
    private void handleFoodButton(ActionEvent event)
    {
        selectModule(foodParent);
    }

    @FXML
    private void handleStockButton(ActionEvent event)
    {
        selectModule(stockParent);
    }

    //endregion


    /****
     * CAMBIAR LA FORMA DE ELIMINAR USERS
     * QUE NO SE PUEDA VER MAS EN NINGUN LADO
     * SOLO EL HISTORIAL
     * AGREGAR UN CAMPO LLAMADO exist
     */


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        loadModules();
        menuButtonUser.setText("@"+UserSession.getUser().getUsername());
    }

    private Parent orderParent, foodParent, stockParent, reportParent,usersParent, settingsParent;

    /**
     * Muestra el modulo que le indicamos como parametro
     *
     * @param module Modulo que queremos mostrar.
     */
    private void selectModule(Parent module)
    {
        borderPane.setCenter(module);
    }

    /**
     * Carga todos los modulos indicados. Dependiendo de los roles o permisos
     * que tenga el usuario que inicio sesión.
     */
    private void loadModules()
    {
        usersParent = addModule(ADMIN,   USERS);
        foodParent  = addModule(STOCKER, FOOD);
        stockParent = addModule(STOCKER, STOCK);
        //orderParent = addModule(CASHIER, ORDERS);
        //foodParent = addModule(STOCKER, FOOD );
        //report = addModule(SPECTATOR, REPORTS);
        //settingsParent = addModule(ADMIN, SETTINGS);
    }

    /**
     * Devuelve el modulo contenida en el {@code urlView} como un
     * {@code Parent}.
     * Si no tiene el rol indicado en {@code userRole} devuelve
     * un Parent con el mensaje indicando el acceso restringido.
     *
     * @param userRole Rol de usuario que necesita para acceder a este modulo.
     * @param urlView URL del modulo o archivo fxml
     * @return Parent del modulo
     */
    private Parent addModule(UserRole userRole, String urlView)
    {
        Parent parent;
        try
        {
                if (UserSession.getUser().hasRole(userRole))
                {
                    parent = FXTool.getParent(urlView);
                }
                else
                {
                    parent = FXTool.getParent(UNAUTHORIZED_MODULE);
                }
        }
        catch (IOException exception)
        {
            parent = new AnchorPane();
            FXTool.alertException(exception);
        }
        return parent;
    }

    /**
     * Cierra sesión de usuarios y vuelve al login.
     */
    private void logout()
    {
        UserSession.logout();
        borderPane.getScene().getWindow().hide();
        try
        {
            FXTool.newScene(LOGIN);
        } catch (IOException exception)
        {
            FXTool.alertException(exception);
        }
    }
}
