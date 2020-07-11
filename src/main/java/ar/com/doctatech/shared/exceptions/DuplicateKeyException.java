package ar.com.doctatech.shared.exceptions;

/**
 * Esta excepcion ocurre cuando se trata de crear un objeto/insertar un elemento
 * con una clave que ya existe. */
public class DuplicateKeyException extends Exception{
    public DuplicateKeyException(String message){
        super(message);
    }

    public String getMessage()
    {
        return super.getMessage();
    }
}
