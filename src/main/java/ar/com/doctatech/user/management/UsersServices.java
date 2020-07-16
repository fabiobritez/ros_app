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

    protected boolean checkFields(String ...fields)
    {
        for (String field : fields) {
            if (field.trim().isEmpty())
            {
                FXTool.alertInformation(
                        "RELLENA LOS CAMPOS REQUERIDOS: " ,
                        "Verifica el formulario"
                );
                return false;
            }
        }
        return true;
    }

}
