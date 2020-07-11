package ar.com.doctatech.shared.utilities;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class SecurityUtil {
    public static final String ALGORITHM = "SHA-256";

    /**
     * Encripta el texto ingresado con el algoritmo especificado
     * @param text El texto que se quiere encriptar
     * @return El texto encriptado
     */
    public static String encrypt(String text)
    {
        try{
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.reset();
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        }catch(NoSuchAlgorithmException ex){
            System.out.println("ERROR ENCRIPTANDO TEXTO:\n"+ex.getMessage());
        }
        return null;
    }

    //auxiliary method
    private static String bytesToHex(byte[] hash)
    {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
