package ar.com.doctatech.user.management;

import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.model.UserDAO;
import ar.com.doctatech.user.model.UserDAOMySQL;

public class UsersServices
{
    UserDAO userDAO ;

    {
        userDAO = new UserDAOMySQL();
    }
}
