package ar.com.doctatech.shared.utilities;

public class ControlUtil {

    private ControlUtil(){

    }

    public static boolean checkFields(String ...fields)
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
