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
        try
        {
            connection = DatabaseConnection.getConnection();
        }
        catch (IOException | SQLException exception) {
            System.out.println(exception.toString());
        }
    }

    @Override
    public void save(User user) throws SQLException
    {
        //SAVE USER
        String query =
                "INSERT INTO user (username, email, password, enabled) " +
                "VALUES (?, ?, ?, ?)";

        try ( PreparedStatement ps =
                      connection.prepareStatement(query) )
        {
            ps.setString (1,user.getUsername() );
            ps.setString (2,user.getEmail()    );
            ps.setString (3,user.getPassword() );
            ps.setBoolean(4,user.isEnabled()   );
            ps.executeUpdate();
        }
        updateRoles(user);
    }

    @Override
    public void update(User user) throws SQLException
    {
        String query =
                "UPDATE user SET email = ?, password = ?, enabled = ? " +
                "WHERE username = ?";

        try( PreparedStatement ps
                     = connection.prepareStatement(query) )
        {
            ps.setString (1, user.getEmail()    );
            ps.setString (2, user.getPassword() );
            ps.setBoolean(3, user.isEnabled()   );
            ps.setString (4, user.getUsername() );
            ps.execute();
        }
       updateRoles(user);
    }

    public void updateRoles(User user) throws SQLException
    {
        String query1 = "DELETE FROM role WHERE user_username='"+user.getUsername()+"'";
        try (PreparedStatement ps =
                     connection.prepareStatement(query1))
        { ps.executeUpdate(); }

        String query2 = "INSERT INTO role (role, user_username) VALUES (?,?)";

        try ( PreparedStatement ps =
                      connection.prepareStatement(query2) )
        {
            for ( String role : user.getUserRoles() )
            {
                ps.setString(1, role);
                ps.setString(2, user.getUsername());

                ps.executeUpdate();
            }
        }
    }

    @Override
    public void delete(String username) throws SQLException
    {
        String query = "UPDATE user SET exist = false WHERE username = ?";
        try( PreparedStatement ps = connection.prepareStatement(query) )
        {
            ps.setString(1, username.toUpperCase().trim() );
            ps.executeUpdate();
        }
    }


    @Override
    public HashMap<String,User> getAll() throws SQLException
    {
        String query =
        "SELECT username, email, password, enabled, role " +
        "FROM user " +
        "LEFT JOIN role r on user.username = r.user_username " +
        "WHERE user.exist=true";

        HashMap<String, User> usersFound = new HashMap<>();

        try (PreparedStatement ps =
                     connection.prepareStatement(query) )
        {
            try(ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    String username = rs.getString("username");
                    if(!usersFound.containsKey(username))
                    {
                        User user = new User(
                                username,
                                rs.getString("email"),
                                rs.getString("password"),
                                rs.getBoolean("enabled")
                        );
                        user.addRole( rs.getString("role") );
                        usersFound.put(user.getUsername(), user);
                    }
                    else
                        usersFound.get( username ).
                            addRole( rs.getString("role") );
                }
            }
        }
        return usersFound;
    }

    @Override
    public User get(String username) throws NotFoundException, SQLException
    {
        String query =
                "SELECT username, email, password, enabled, role " +
                "FROM user " +
                "LEFT JOIN role r ON user.username = r.user_username " +
                "WHERE user.username=? AND user.exist=true";

        User userFound = new User("NULL","null","null",false);
        try ( PreparedStatement ps =
                      connection.prepareStatement(query) )
        {
            ps.setString(1, username.toUpperCase().trim() );
            try( ResultSet rs = ps.executeQuery() )
            {
                while (rs.next())
                {
                    if(userFound.getUsername().equals("NULL") )
                    {
                        userFound = new User(
                            rs.getString ("username"),
                            rs.getString ("email"),
                            rs.getString ("password"),
                            rs.getBoolean("enabled")
                        );
                    }
                    userFound.addRole( rs.getString("role") );
                }
            }
        }

        if ( userFound.getUsername().equals("NULL") ){ throw new NotFoundException("Usuario: '"+username+"' no encontrado.");}

        return userFound;
    }

}
