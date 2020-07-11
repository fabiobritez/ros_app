package ar.com.doctatech.user.management;

import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.model.User;
import ar.com.doctatech.user.model.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

 import ar.com.doctatech.shared.PROCESS;
import static ar.com.doctatech.shared.PROCESS.*;
import static ar.com.doctatech.shared.utilities.SecurityUtil.encrypt;

public class UsersController
        extends UsersServices
        implements Initializable
{

    //region FXML REFERENCES
    @FXML
    private Button buttonEditUser, buttonUpdateUser, buttonSaveUser,
                  buttonAddRole, buttonRemoveRole, buttonDeleteUser;

    @FXML
    private TextField textfieldUsername, textfieldEmail;

    @FXML
    private ListView<String> listViewRoles, listViewUsers;

    @FXML
    private ComboBox<UserRole> comboboxRoles;

    @FXML
    private CheckBox checkBoxEnabled;

    @FXML
    private Text textErrorsRoles;

    @FXML
    private void handleClickedListUser(MouseEvent event)
    {
        selectUser(listViewUsers.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void handleButtonNewUser(ActionEvent event)
    {
        setProcess(ADDING);
    }

    @FXML
    private void handleButtonSaveUser(ActionEvent event)
    {
        saveUser();
    }

    @FXML
    private void handleButtonUpdateUser(ActionEvent event)
    {
        updateUser();
    }

    @FXML
    private void handleButtonEditUser(ActionEvent event)
    {
        setProcess(EDITING);
    }

    @FXML
    private void handleButtonAddRole(ActionEvent event)
    {
        addRoleSelected();
    }

    @FXML
    private void handleButtonDeleteUser(ActionEvent event)
    {
        deleteUser();
    }

    @FXML
    private void handleButtonRemoveRole(ActionEvent event)
    {
        removeRoleSelected();
    }

    //endregion

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        loadLists();

        listViewUsers.getSelectionModel().selectedItemProperty().
                addListener(event ->
                {
                    selectUser( listViewUsers.getSelectionModel().getSelectedItem() );
                }
        );
        listViewUsers.getSelectionModel().selectFirst();

        setProcess(VIEWING);
    }

    //region GUI METHODS

    /**
     * TENER EN CUENTA AL ACTUALIZAR, GUARDAR Y ELIMINAR
     */
    private ObservableList<String> observableListUsers;
    private HashMap<String, User> mapUsers;

    /**
     * Carga los datos en las dos listas correspondientes.
     * Rellena el listViewUsers con la lista de usuarios.
     * Rellena el comboboxRoles con los valores.
     */
    private void loadLists()
    {
        try
        {
            mapUsers = userDAO.getAll();
            observableListUsers = FXCollections.observableArrayList( mapUsers.keySet() );

            comboboxRoles.setItems(FXCollections.observableArrayList( UserRole.values() ));
            listViewUsers.setItems(observableListUsers);
        }
        catch (SQLException sqlException) {
            FXTool.alertException(sqlException);
        }
    }

    /**
     * Obtiene el usuario con el username seleccionado.
     * Muestra sus detalles en el formulario.
     * Cambia la el Procces:VIEWING
     *
     * @param username Nombre de usuario que se desea mostrar.
     */
    private void selectUser(String username)
    {
        User userSelected = mapUsers.get(username);

        textfieldUsername.setText(userSelected.getUsername());
        textfieldEmail.setText(userSelected.getEmail());
        checkBoxEnabled.setSelected(userSelected.isEnabled());
        listViewRoles.setItems(
                FXCollections.observableArrayList(userSelected.getUserRoles())
        );
        setProcess(VIEWING);
    }

    /**
     * Comprueba que los campos requeridos no esten vacios.
     * Obtiene los valores del formulario y crea un nuevo usuario.
     * Agrega los roles de la lista.
     * Actualiza la lista y el mapa de usuarios.
     * Cambia el PROCESS:VIEWING
     */
    private void saveUser()
    {
        if(!textfieldUsername.getText().trim().isEmpty() ||
                !textfieldEmail.getText().trim().isEmpty())
        {
            try
            {
                String username = textfieldUsername.getText().trim();
                User newUser = new User(
                        username,
                        textfieldEmail.getText().trim(),
                        encrypt(username),
                        checkBoxEnabled.isSelected()
                );
                newUser.getUserRoles().addAll( listViewRoles.getItems() );

                userDAO.save(newUser);

                mapUsers.put(username, newUser);
                observableListUsers.add(username);

                setProcess(VIEWING);
            }
            catch (Exception e)
            {
                FXTool.alertException(e);
            }
        }
        else
        {
            FXTool.alertInformation(
                    "Campos requeridos",
                    "El usuario debe tener nombre de usuario y un correo"
            );
        }
    }

    /**
     * Obtiene el username del TextField y con el usuario.
     * Actualiza al usuario y sus roles.
     * Actualizamos en la base de datos.
     * Cambiamos PROCESS:VIEWING
     */
    private void updateUser()
    {
        String username = textfieldUsername.getText().trim();
        try
        {
            User userSelected = mapUsers.get( username );

            userSelected.setEmail( textfieldEmail.getText().trim() );
            userSelected.setEnabled( checkBoxEnabled.isSelected() );

            //SI UN ROL DE LA LISTA ANTIGUA NO SE ENCUENTRA EN
            //LA NUEVA LISTA, LO REMOVEMOS DE LA BASE DE DATOS
            List<String> listTmp = new ArrayList<>(userSelected.getUserRoles());
            for (String role : listTmp)
            {
                if(!listViewRoles.getItems().contains(role))
                {
                    userDAO.removeRole(username, role);
                    userSelected.removeRole(role);
                }
            }

            //SI UN ROL DE LA LISTA NUEVA NO SE ENCUENTRA EN
            //LA LISTA VEJA, LO AGREGAMOS A LA BASE DE DATOS
            listTmp.clear();
            listTmp.addAll( listViewRoles.getItems() );
            for (String role : listTmp)
            {
                if(!userSelected.getUserRoles().contains(role))
                {
                    userDAO.addRole(username,role);
                    userSelected.addRole(role);
                }
            }

            userDAO.update(userSelected);

            setProcess(VIEWING);
        }
        catch (Exception exception)
        {
            FXTool.alertException(exception);
        }
    }

    /**
     * Lanzar advertencia sobre consecuencias de borrar un usuario.
     * Si acepta, eliminar usuario de la base de datos, del mapa y
     * de la lista de usuarios.
     * Cambiar PROCESS:VIEWING
     * Seleccionar el primer usuario de la lista.
     */
    private void deleteUser()
    {
        try
        {
            String username = textfieldUsername.getText().trim();

            Alert alert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Si elimina el usuario, eliminará todos sus registros, historial, etc",
                    ButtonType.YES,
                    ButtonType.CANCEL
            );
            alert.setHeaderText("¿Estas seguro que desea eliminar a '"+username+"'?");
            alert.showAndWait();

            if (alert.getResult().equals(ButtonType.YES))
            {
                userDAO.remove(username);
                observableListUsers.remove(username);
                mapUsers.remove(username);

                FXTool.alertInformation("Eliminado","Usuario eliminado correctamente!");

                listViewUsers.getSelectionModel().selectFirst();
            }
        }
        catch (SQLException e) {
            FXTool.alertException(e);
        }
    }

    /**
     * Añadimos el role seleccionado en el comboboxRoles
     * Si no lanzamos error
     */
    private void addRoleSelected()
    {
        try
        {
           addRole(
                   comboboxRoles.getSelectionModel().getSelectedItem().toString()
           );
        }
        catch (NullPointerException exception)
        {
            textErrorsRoles.setText("DEBES SELECCIONAR UN ROL");
        }
    }

    /**
     * Verificamos que el rol que añadiremos no se
     * encuentra en la lista.
     * Si el rol es administrador: agregamos todos los roles.
     * Sino agregamos solo el rol ingresado.
     * Limpiamos los mensajes de error anteriores.
     * @param role
     */
    private void addRole(String role)
    {
        if(!listViewRoles.getItems().contains(role))
        {
            if(role.equals(UserRole.ADMIN.toString()))
            {
                addRole(UserRole.CASHIER.toString());
                addRole(UserRole.SPECTATOR.toString());
                addRole(UserRole.STOCKER.toString());
            }
            listViewRoles.getItems().add(role);
            textErrorsRoles.setText("");
        }
    }

    /**
     * Obtiene el rol seleccionado de la lista..
     * Si el rol no es el POR DEFECTO, lo removemos.
     * Limpiamos los errores.
     */
    private void removeRoleSelected()
    {
        try
        {
            String role = listViewRoles.getSelectionModel().getSelectedItem();
            if(!role.equals(UserRole.DEFAULT.toString()))
                listViewRoles.getItems().remove(role);
            textErrorsRoles.setText("");
        }
        catch (NullPointerException exception)
        {
            textErrorsRoles.setText("DEBES SELECCIONAR UN ROL!");
        }
    }

    /**
     * Actualiza todos los componentes del formulario
     * segun en que proceso se encuentre.
     * @param process Proceso en el que se encuentra.
     */
    private void setProcess(PROCESS process)
    {
        if(process.equals(ADDING))
        {
            textfieldUsername.setEditable(true);
            textfieldUsername.requestFocus();
            textfieldEmail.setEditable(true);

            buttonAddRole.setVisible(true);
            buttonRemoveRole.setVisible(true);
            comboboxRoles.setVisible(true);

            checkBoxEnabled.setDisable(false);

            buttonSaveUser.setVisible(true);
            buttonUpdateUser.setVisible(false);
            buttonEditUser.setVisible(false);
            buttonDeleteUser.setVisible(false);

            //clear components
            textfieldUsername.setText("");
            textfieldEmail.setText("");
            checkBoxEnabled.setSelected(false);
            listViewRoles.getItems().clear();
            listViewRoles.getItems().add(UserRole.DEFAULT.toString());
        }
        else if(process.equals(EDITING))
        {
            textfieldUsername.setEditable(false);
            textfieldEmail.setEditable(true);
            buttonAddRole.setVisible(true);
            buttonRemoveRole.setVisible(true);
            checkBoxEnabled.setDisable(false);
            comboboxRoles.setVisible(true);

            buttonSaveUser.setVisible(false);
            buttonUpdateUser.setVisible(true);
            buttonEditUser.setVisible(false);
            buttonDeleteUser.setVisible(false);
        }
        else if(process.equals(VIEWING))
        {
            textfieldUsername.setEditable(false);
            textfieldEmail.setEditable(false);
            buttonAddRole.setVisible(false);
            buttonRemoveRole.setVisible(false);
            checkBoxEnabled.setDisable(true);
            comboboxRoles.setVisible(false);

            buttonSaveUser.setVisible(false);
            buttonUpdateUser.setVisible(false);
            buttonEditUser.setVisible(true);
            buttonDeleteUser.setVisible(true);
        }
    }

    //endregion
}
