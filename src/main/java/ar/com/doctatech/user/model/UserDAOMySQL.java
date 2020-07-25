package ar.com.doctatech.user.model;

import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.exceptions.NotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class UserDAOMySQL implements UserDAO {

    Connection connection;
    {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (IOException | SQLException exception) {
            System.out.println(exception.toString());
        }
    }

    @Override
    public void save(User user) throws SQLException
    {
        //SAVE USER
        String query = "INSERT INTO user (username, email, password, enabled) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement =
                    connection.prepareStatement(query))
        {
            preparedStatement.setString (1,user.getUsername() );
            preparedStatement.setString (2,user.getEmail()    );
            preparedStatement.setString (3,user.getPassword() );
            preparedStatement.setBoolean(4,user.isEnabled()   );

            preparedStatement.executeUpdate();
        }

        //ADD ROLES
        query = "INSERT INTO role (role, user_username) VALUES (?, ?)";
        try ( PreparedStatement preparedStatement = connection.prepareStatement(query) )
        {
            for ( String role : user.getUserRoles() )
            {
                preparedStatement.setString(1, role);
                preparedStatement.setString(2, user.getUsername());

                preparedStatement.executeUpdate();
            }
        }
    }

    @Override
    public void update(User user) throws SQLException
    {
        String query = "UPDATE user SET email = ?, password = ?, enabled = ? " +
                "WHERE username = ?";

        try( PreparedStatement preparedStatement
                     = connection.prepareStatement(query) )
        {
            preparedStatement.setString (1, user.getEmail()    );
            preparedStatement.setString (2, user.getPassword() );
            preparedStatement.setBoolean(3, user.isEnabled()   );
            preparedStatement.setString (4, user.getUsername() );

            preparedStatement.execute();
        }
    }

    @Override
    public void remove(String username) throws SQLException
    {
        String query = "UPDATE user SET exist = false WHERE username = ?";
        try( PreparedStatement preparedStatement = connection.prepareStatement(query) )
        {
            preparedStatement.setString(1, username.toUpperCase().trim() );
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void removeRole(String username, String role) throws SQLException
    {
        String query = "DELETE FROM role WHERE user_username = ? && role = ?";
        try( PreparedStatement preparedStatement = connection.prepareStatement(query) )
        {
            preparedStatement.setString(1, username.toUpperCase().trim());
            preparedStatement.setString(2, role);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void addRole(String username, String role) throws SQLException
    {
        String query = "INSERT INTO role (role,user_username) VALUES (?, ?)";
        try ( PreparedStatement preparedStatement = connection.prepareStatement(query) )
        {
                preparedStatement.setString(1, role);
                preparedStatement.setString(2, username.toUpperCase().trim() );

                preparedStatement.executeUpdate();
        }
    }

    @Override
    public HashMap<String,User> getAll() throws SQLException
    {
        String queryJoin = "SELECT * FROM user LEFT OUTER JOIN role " +
                "ON user.username = role.user_username WHERE exist = true";

        HashMap<String, User> usersFound = new HashMap<>();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(queryJoin) )
        {
            try(ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String username = resultSet.getString("username");
                    if(!usersFound.containsKey(username))
                    {
                        User user = new User(
                                username,
                                resultSet.getString("email"),
                                resultSet.getString("password"),
                                resultSet.getBoolean("enabled")
                        );
                        user.addRole( resultSet.getString("role") );
                        usersFound.put(user.getUsername(), user);
                    }
                    else
                    {
                        usersFound.get( username ).
                                addRole( resultSet.getString("role") );
                    }
                }
            }
        }
        return usersFound;
    }

    @Override
    public User get(String username) throws NotFoundException, SQLException
    {
        String query = "SELECT * FROM user LEFT OUTER JOIN role ON user.username = role.user_username " +
                "WHERE user.username = ? && exist = true";

        User userFound = new User("NULL","null","null",false);

        try ( PreparedStatement preparedStatement = connection.prepareStatement(query) )
        {
            preparedStatement.setString(1, username.toUpperCase().trim() );
            try(ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    if(userFound.getUsername().equals("NULL") )
                    {
                        userFound = new User(
                                resultSet.getString("username"),
                                resultSet.getString("email"),
                                resultSet.getString("password"),
                                resultSet.getBoolean("enabled")
                        );
                    }
                    userFound.addRole( resultSet.getString("role") );
                }
            }
        }

        if ( userFound.getUsername().equals("NULL") )
        {
            throw new NotFoundException("Usuario: '"+username+"' no encontrado.");
        }
        return userFound;
    }

}
