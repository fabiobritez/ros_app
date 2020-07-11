package ar.com.doctatech.user;

import ar.com.doctatech.shared.exceptions.NotFoundException;
import ar.com.doctatech.shared.exceptions.PrivilegeException;
import ar.com.doctatech.shared.utilities.SecurityUtil;
import ar.com.doctatech.user.model.UserDAO;
import ar.com.doctatech.user.model.User;
import ar.com.doctatech.user.model.UserDAOMySQL;
import ar.com.doctatech.user.model.UserRole;

import java.sql.SQLException;

public class UserSession
{
    private static User userLoggedIn;//User logged in

    private UserSession() { }

    /**
     * Verifica que no haya una sesión de usuario abierta.
     * Busca en la base de datos si existe un usuario con el nombre ingresado.
     * Si encuentra el usuario verifica que esta habilitado.
     * Luego encripta la contraseña y la compara con el de la base de datos
     *
     * @param username Nombre de usuario con el que desea iniciar sesión
     * @param password Contraseña con la que desea iniciar sesión
     * @throws NotFoundException En caso de que no se encuentre un usuario con ese nombre
     * @throws PrivilegeException En caso de que la contraseña sea incorrecta
     */
    public static void login( String username, String password )
            throws NotFoundException, PrivilegeException, SQLException
    {
        if ( userLoggedIn == null )
        {
            UserDAO userDAO = new UserDAOMySQL();
            User userFound = userDAO.get( username ); //dado el caso NotFoundException

            if ( userFound.isEnabled() )
                if ( userFound.getPassword().equals( SecurityUtil.encrypt(password) ) )
                    userLoggedIn = userFound;
                else
                    throw new PrivilegeException ( "ERROR: Contraseña incorrecta" );
            else
                throw new PrivilegeException ( "ERROR: El usuario ingresado no esta habilitado" );
        }
        else
        {
            System.out.println("LOGIN: EL USUARIO YA HA INICIADO SESIÓN");
        }
    }

    /**
     * Debe verificar si la password pasada como parametro es igual
     * a la password del usuario logueado. Devuelve {@code true} en
     * caso afirmativo.
     * @param password password para verificar
     * @return {@code true} es caso de que las contraseñas sean iguales.
     * @throws PrivilegeException En caso de que la contraseña sean diferentes
     */
    public static boolean equalsPassword( String password )
            throws PrivilegeException
    {
        if( userLoggedIn.getPassword().equals( SecurityUtil.encrypt(password) ) ) return true;
        throw new PrivilegeException("La contraseña ingresada no coincide con la del usuario logueado");
    }

    public static User getUser()
    {
        return userLoggedIn;
    }

    /**
     * Verifica si la sesion de usuario tiene permiso para modificar
     * al user ingresado como parametro. Podrá modificarlo si es el
     * mismo usuario o si es el usuario administrador.
     * @param user el usuario que se desea modificar
     * @return {@code true} si la sesion de usuario lo puede modificar
     * @throws PrivilegeException si el usuario no tiene permiso para
     * modificar atributos del user
     */
    public static boolean hasPrivilegesToModify(User user)
            throws PrivilegeException
    {
        if (userLoggedIn.hasRole(UserRole.ADMIN) ||
                userLoggedIn.equals(user)) return true;
        throw new PrivilegeException("No tienes permiso para modificar este atributo del usuario");
    }

    public static void logout(){
        userLoggedIn = null;
    }
}
