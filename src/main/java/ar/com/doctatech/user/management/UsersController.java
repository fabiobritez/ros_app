package ar.com.doctatech.user.management;

import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.model.User;
import ar.com.doctatech.user.model.UserRole;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.PROCESS.*;
import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;
import static ar.com.doctatech.shared.utilities.SecurityUtil.encrypt;

public class UsersController
        extends UsersServices
        implements Initializable
{
    //region FXML REFERENCES
    @FXML private Button buttonEditUser, buttonUpdateUser, buttonSaveUser,
                  buttonAddRole, buttonRemoveRole, buttonDeleteUser;

    @FXML private TextField textfieldSearchEngine, textfieldUsername, textfieldEmail;

    @FXML private ListView<String> listViewRoles, listViewUsers;

    @FXML private ComboBox<UserRole> comboboxRoles;

    @FXML private CheckBox checkBoxEnabled;

    @FXML private Text textErrorsRoles;

    @FXML private void onKeyPressedSearchEngine(KeyEvent event)
    {
        if(event.getCode() == KeyCode.DOWN)
        {
            listViewUsers.getSelectionModel().selectFirst();
            listViewUsers.requestFocus();
        }
    }

    @FXML private void onActionNewUser()
    {
        setProcess(ADDING);
    }

    @FXML private void onActionSaveUser()
    {
        saveUser();
    }

    @FXML private void onActionUpdateUser()
    {
        updateUser();
    }

    @FXML private void onActionEditUser()
    {
        setProcess(EDITING);
    }

    @FXML private void onActionAddRole()
    {
        addRoleSelected();
    }

    @FXML private void onActionDeleteUser()
    {
        deleteUser();
    }

    @FXML private void onActionRemoveRole()
    {
        removeRoleSelected();
    }

    @FXML private void onActionClear()
    {
        textfieldSearchEngine.setText("");
        textfieldSearchEngine.requestFocus();
    }

    //endregion

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Platform.runLater(()->{
            loadUsers();
            addListenerEvents();
            setProcess(VIEWING);
        });
    }

    //region METHODS

    private ObservableList<String> obsUsers;
    private HashMap<String, User> mapUsers;
    private FilteredList<String> flUsers;

    /**
     * Carga los datos en las dos listas correspondientes.
     * Rellena el comboboxRoles con los valores.
     */
    private void loadUsers()
    {
        try
        {
            mapUsers = userDAO.getAll();
            obsUsers = FXCollections.observableArrayList( mapUsers.keySet() );
            flUsers = new FilteredList<>(obsUsers, s -> true);

            listViewUsers.setItems(flUsers);
            comboboxRoles.setItems(FXCollections.observableArrayList( UserRole.values() ));
        }
        catch (SQLException e) { FXTool.alertException(e); }
    }

    /**
     * Cargamos todos los listeners al property de cada componente
     */
    private void addListenerEvents()
    {
        //USERS SEARCHER
        textfieldSearchEngine.textProperty()
                .addListener( (observable, oldValue, newValue) ->
                        flUsers.setPredicate(user ->
                                user.contains(newValue.trim().toUpperCase()))
        );

        //ITEMS SELECTION
        listViewUsers.getSelectionModel().selectedItemProperty().
                addListener(event -> selectUser() );

        //UX
        listViewUsers.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.UP &&
                    listViewUsers.getSelectionModel().isSelected(0))
            { textfieldSearchEngine.requestFocus(); }
        });

        //TO START WITH FIRST
        listViewUsers.getSelectionModel().selectFirst();
        listViewUsers.requestFocus();
    }

    /**
     * Obtiene el usuario seleccionado
     * Muestra sus detalles en el formulario.
     * VIEWING
     *
     */
    private void selectUser()
    {
        String username =  listViewUsers.getSelectionModel().getSelectedItem();

        if(username!=null)
        {
            User userSelected = mapUsers.get(username);

            textfieldUsername.setText  (userSelected.getUsername());
            textfieldEmail.setText     (userSelected.getEmail()   );
            checkBoxEnabled.setSelected(userSelected.isEnabled()  );

            listViewRoles.getItems().clear();
            listViewRoles.getItems().addAll( userSelected.getUserRoles() );

            setProcess(VIEWING);
        }
    }

    /**
     * Comprueba que los campos requeridos no esten vacios.
     * Obtiene los valores del formulario y crea un nuevo usuario.
     * Agrega los roles de la lista.
     * Actualiza la lista y el mapa de usuarios.
     * VIEWING
     */
    private void saveUser()
    {
        String username = textfieldUsername.getText().trim().toUpperCase();
        String email = textfieldEmail.getText().trim();

        if( checkFields(username,email) )
        {
            try
            {
                User newUser = new User( username, email,
                        encrypt(username.toLowerCase().trim()),
                        checkBoxEnabled.isSelected()
                );

                newUser.getUserRoles().addAll( listViewRoles.getItems() );

                userDAO.save(newUser);

                mapUsers.put(newUser.getUsername(), newUser);
                obsUsers.add(newUser.getUsername());

                listViewUsers.requestFocus();
                listViewUsers.getSelectionModel().select(username);

                setProcess(VIEWING);
            }
            catch (Exception e) { FXTool.alertException(e); }
        }
    }

    /**
     * Obtiene el username del TextField y con el usuario.
     * Actualiza al usuario y sus roles.
     * Actualizamos en la base de datos.
     * VIEWING
     */
    private void updateUser()
    {
        String username = textfieldUsername.getText().trim();
        String email = textfieldEmail.getText().trim();
        if(checkFields(username, email))
        {
            User userSelected = mapUsers.get( username );
            if(userSelected!=null)
            {
                try
                {
                    userSelected.getUserRoles().clear();
                    userSelected.setEmail( email );
                    userSelected.setEnabled( checkBoxEnabled.isSelected() );
                    userSelected.getUserRoles().addAll(listViewRoles.getItems());

                    userDAO.update(userSelected);

                    setProcess(VIEWING);
                }
                catch (Exception e) { FXTool.alertException(e); }
            }
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
                    "Si elimina el usuario, eliminará todos su historial",
                    ButtonType.YES,
                    ButtonType.CANCEL
            );

            alert.setHeaderText("¿Estas seguro que desea eliminar a '"+username+"'?");
            alert.showAndWait();

            if (alert.getResult().equals(ButtonType.YES))
            {
                userDAO.delete(username);
                obsUsers.remove(username);
                mapUsers.remove(username);

                FXTool.alertInformation("Eliminado","Usuario eliminado correctamente!");

                listViewUsers.getSelectionModel().selectFirst();
                listViewUsers.requestFocus();
            }
        }
        catch (SQLException e) { FXTool.alertException(e);}
    }

    /**
     * Añadimos el role seleccionado en el comboboxRoles
     * Si no hay seleccionado, lanzamos error
     */
    private void addRoleSelected()
    {
        try
        {
           addRole( comboboxRoles.getSelectionModel().getSelectedItem().toString() );
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
     * @param role SS
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
     * Obtiene el rol seleccionado de la lista y lo remueve.
     * Limpiamos los errores.
     */
    private void removeRoleSelected()
    {
        try
        {
            String role = listViewRoles.getSelectionModel().getSelectedItem();
            listViewRoles.getItems().remove(role);
            textErrorsRoles.setText("");
        }
        catch (NullPointerException exception)
        {
            textErrorsRoles.setText("DEBES SELECCIONAR UN ROL.");
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

            buttonSaveUser.setDisable(false);
            buttonUpdateUser.setDisable(true);
            buttonEditUser.setDisable(true);
            buttonDeleteUser.setDisable(true);

            //clear components
            textfieldUsername.setText("");
            textfieldEmail.setText("");
            checkBoxEnabled.setSelected(false);
            listViewRoles.getItems().clear();

        }
        else if(process.equals(EDITING))
        {
            textfieldUsername.setEditable(false);
            textfieldEmail.setEditable(true);
            buttonAddRole.setVisible(true);
            buttonRemoveRole.setVisible(true);
            checkBoxEnabled.setDisable(false);
            comboboxRoles.setVisible(true);

            buttonSaveUser.setDisable(true);
            buttonUpdateUser.setDisable(false);
            buttonEditUser.setDisable(true);
            buttonDeleteUser.setDisable(true);
        }
        else if(process.equals(VIEWING))
        {
            textfieldUsername.setEditable(false);
            textfieldEmail.setEditable(false);
            buttonAddRole.setVisible(false);
            buttonRemoveRole.setVisible(false);
            checkBoxEnabled.setDisable(true);
            comboboxRoles.setVisible(false);

            buttonSaveUser.setDisable(true);
            buttonUpdateUser.setDisable(true);
            buttonEditUser.setDisable(false);
            buttonDeleteUser.setDisable(false);
        }
    }

    //endregion
}
