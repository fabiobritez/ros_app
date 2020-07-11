package ar.com.doctatech.user.login;

import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.user.model.UserDAOMySQL;
import ar.com.doctatech.user.model.UserDAO;
import ar.com.doctatech.user.model.User;

import java.sql.SQLException;
import java.util.Random;

public class RecoveryServices
{
    private final UserDAO userDAO = new UserDAOMySQL();

    protected String generateRandomCode()
    {
        char character;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder generatedCode = new StringBuilder("0");
        Random rnd = new Random();

        for (int i = 0; i < 10; i++)
        {
            if(i < 5 )
            {
                generatedCode.append(rnd.nextInt(10));
            }
            else
            {
                do
                {
                    character = (char) (rnd.nextInt(91)+65);
                }
                while ((characters.indexOf(character))<0);

                generatedCode.append(character);
            }
        }

        return generatedCode.toString();
    }

    protected void updatePassword(String username, String newPassword)
            throws SQLException, NotFoundException
    {
        User user = getUser(username);
        user.setPassword(newPassword);
        userDAO.update(user);
    }

    protected User getUser(String username)
            throws NotFoundException, SQLException
    {
        return userDAO.get(username);
    }

    protected enum State
    {
       STARTING, SENDING_EMAIL, VERIFYING_CODE
    }

}
