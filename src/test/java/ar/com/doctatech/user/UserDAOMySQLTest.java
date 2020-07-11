package ar.com.doctatech.user;

import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.shared.utilities.SecurityUtil;
import ar.com.doctatech.user.model.User;
import ar.com.doctatech.user.model.UserDAO;
import ar.com.doctatech.user.model.UserDAOMySQL;
import ar.com.doctatech.user.model.UserRole;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class UserDAOMySQLTest {


    @Test
    public void insertNewUser()
    {
        String name = Math.round(Math.random()*1000)
                +"-"+Math.round(Math.random()*100)
                +"-"+ Math.round(Math.random()*100);

        User user = new User(name,name + "@ros.com.ar",SecurityUtil.encrypt(name),true);
        user.addRole(UserRole.ADMIN.toString());

        UserDAO userDAO = new UserDAOMySQL();

        try {
            userDAO.save(user);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    @Test
    public void getUser()
    {
        String name = Math.round(Math.random()*1000) + Math.round(Math.random()*100) + "-" + Math.round(Math.random()*100);
        User user = new User(name,name + "@ros.com.ar",SecurityUtil.encrypt(name),true);

        UserDAO userDAO = new UserDAOMySQL();

        try {
              userDAO.save(user);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        try {
            assertEquals(user, userDAO.get(user.getUsername()));
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    @Test
    public void getAllUsersTest()
    {
        UserDAO dao = new UserDAOMySQL();
        try {
            for (User user: dao.getAll().values()) {
                System.out.print(user.getUsername()+"\nRoles: ");
                for (String s : user.getUserRoles()){
                    System.out.print(s+", ");
                }
                System.out.println("");
            }
        } catch (SQLException exception) {
            System.out.println(exception.toString());
        }
    }

}