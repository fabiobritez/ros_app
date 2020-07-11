package ar.com.doctatech.shared.exceptions;

/**
 * Esta clase esta creada para las expeciones que tienen que ver con
 * los privilegios de usuario. Cuando un usuario quiera realizar una
 * acción y no tenga los permisos adecuados se lanzará esta exception
 */

public class PrivilegeException extends Exception {

    /**
     *
     * @param message
     */
    public PrivilegeException(String message){
        super(message);
    }

    public String getMessage()
    {
        return super.getMessage();
    }

}
