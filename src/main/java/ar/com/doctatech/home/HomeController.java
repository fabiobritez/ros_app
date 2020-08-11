package ar.com.doctatech.home;

import static ar.com.doctatech.shared.utilities.FXPath.*;
import static ar.com.doctatech.user.model.UserRole.*;

import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.UserSession;
import ar.com.doctatech.user.model.UserRole;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.SepiaTone;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;


public class HomeController implements Initializable
{

    //region FXML REFERENCES
    @FXML
    private BorderPane borderPane;

    @FXML private ToggleButton buttonFood, buttonOrders, buttonIngredients,
            buttonCustomers, buttonReports,buttonUsers,buttonSettings;
    private ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private MenuButton menuButtonUser;

    @FXML private void onActionButtonOrders() {
     orderParent = addModule(CASHIER, ORDERS);
     selectModule(orderParent);
     toggleGroup.selectToggle(buttonOrders);
    }
    @FXML private void onActionButtonFood()  {
        foodParent  = addModule(STOCKER, FOOD);
        selectModule(foodParent);
        toggleGroup.selectToggle(buttonFood);
    }
    @FXML private void onActionButtonIngredients() {
        stockParent = addModule(STOCKER, STOCK);
        selectModule(stockParent);
        toggleGroup.selectToggle(buttonIngredients);
    }
    @FXML private void onActionButtonCustomers() {
        customerParent = addModule(ADMIN, CUSTOMERS);
        selectModule(customerParent);
        toggleGroup.selectToggle(buttonCustomers);
    }
    @FXML private void onActionButtonReports()
    {
        reportParent = addModule(SPECTATOR, REPORTS);
        selectModule(reportParent);
        toggleGroup.selectToggle(buttonReports);
    }
    @FXML private void onActionButtonUsers() {
        usersParent = addModule(ADMIN, USERS);
        selectModule(usersParent);
        toggleGroup.selectToggle(buttonUsers);
    }


    @FXML private void onActionButtonLogout()
    {
        logout();
    }



    //endregion
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        toggleGroup.getToggles().addAll(
                buttonFood, buttonOrders, buttonIngredients,
                buttonCustomers, buttonReports,buttonUsers,buttonSettings
        );

        menuButtonUser.setText("@"+UserSession.getUser().getUsername());
        orderParent = addModule(CASHIER, ORDERS);
        selectModule(orderParent);
        toggleGroup.selectToggle(buttonOrders);
    }

    private Parent orderParent,customerParent, foodParent, stockParent, reportParent,usersParent, settingsParent;

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
