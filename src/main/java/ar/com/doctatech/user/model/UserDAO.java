package ar.com.doctatech.user.model;

import ar.com.doctatech.shared.exceptions.DuplicateKeyException;
import ar.com.doctatech.shared.exceptions.NotFoundException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

public interface UserDAO {


    /**
     * Inserta un nuevo usuario a la tabla de usuarios.
     * @param user Usuario que se desea insertar
     */
    void save(User user) throws SQLException;

    /**
     * Actualiza el usuario (buscando en en la base de datos con el username)
     * @param user Usuario que se desea actualizar
     * @throws SQLException Un error en la consulta
     */
    void update(User user) throws SQLException;

    void remove(String username) throws SQLException;

    void removeRole(String username, String role) throws SQLException;

    void addRole(String username, String role) throws SQLException;

    User get(String username) throws NotFoundException, SQLException;

    HashMap<String,User> getAll() throws SQLException;
}
