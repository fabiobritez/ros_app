package ar.com.doctatech.user.model;

import ar.com.doctatech.shared.exceptions.DuplicateKeyException;
import ar.com.doctatech.shared.exceptions.NotFoundException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

public interface UserDAO {

    void save(User user) throws SQLException;

    void update(User user) throws SQLException;

    void delete(String username) throws SQLException;

    User get(String username) throws NotFoundException, SQLException;

    HashMap<String,User> getAll() throws SQLException;
}
