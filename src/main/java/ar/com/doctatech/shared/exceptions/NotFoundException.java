package ar.com.doctatech.shared.exceptions;
    /**
     *  Es una exception utilizada cuando no se encuentra el elemento buscado
     */
public class NotFoundException extends Exception {

        public NotFoundException(String message){
            super(message);
        }

        public String getMessage()
        {
            return super.getMessage();
        }
}
