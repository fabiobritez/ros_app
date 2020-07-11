package ar.com.doctatech.shared.utilities;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class GoogleMail {

    /**
     * https://www.geeksforgeeks.org/sending-email-java-ssltls-authentication/
     * https://www.wavemaker.com/learn/how-tos/implementing-forgot-password-feature-using-java-service
     */
    public static void sendEmail(String toAccount, String code)
            throws MessagingException
    {
        String remitente = "inventory.system.recovery";
        String clave = "futbol1409";

        Properties props = new Properties();

        //cambiarDatos(props);

        props.put("mail.smtp.host", "smtp.gmail.com");  //El servidor SMTP de Google
        props.put("mail.smtp.port", "587"); //El puerto SMTP seguro de Google

        props.put("mail.smtp.user", remitente);
        props.put("mail.smtp.clave", clave);    //La clave de la cuenta

        props.put("mail.smtp.auth", "true");    //Usar autenticación mediante usuario y clave
        props.put("mail.smtp.starttls.enable", "true"); //Para conectar de manera segura al servidor SMTP


        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(remitente));

        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAccount));

        message.setSubject("CODIGO DE RECUPERACIÓN: ROS APP");

        message.setText(code);


        try(Transport transport = session.getTransport("smtp"))
        {
            transport.connect("smtp.gmail.com", remitente, clave);
            transport.sendMessage(message, message.getAllRecipients());
        }
    }


    /**
     * java.version             * Java Runtime Environment version
     * java.vendor              * Java Runtime Environment vendor
     * java.vendor.url          * Java vendor URL
     * java.home                * Java installation directory
     * java.vm.specification.version         * Java Virtual Machine specification version
     * java.vm.specification.vendor          * Java Virtual Machine specification vendor
     * java.vm.specification.name            * Java Virtual Machine specification name
     * java.vm.version              * Java Virtual Machine implementation version
     * java.vm.vendor               * Java Virtual Machine implementation vendor
     * java.vm.name                 * Java Virtual Machine implementation name
     * java.specification.version           * Java Runtime Environment specification version
     * java.specification.vendor            * Java Runtime Environment specification vendor
     * java.specification.name              * Java Runtime Environment specification name
     * java.class.version           * Java class format version number
     * java.class.path              * Java class path
     * java.library.path            * List of paths to search when loading libraries
     * java.io.tmpdir               * Default temp file path
     * java.compiler                * Name of JIT compiler to use
     * java.ext.dirs                * Path of extension directory or directories Deprecated. This property, and the mechanism which implements it, may be removed in a future release.
     * os.name                  * Operating system name
     * os.arch                  * Operating system architecture
     * os.version               * Operating system version
     * file.separator           * File separator ("/" on UNIX)
     * path.separator           * Path separator (":" on UNIX)
     * line.separator           * Line separator ("\n" on UNIX)
     * user.name                * User's account name
     * user.home                * User's home directory
     * user.dir                 * User's current working directory
     */

    private static void cambiarDatos(Properties props)
    {

        String reemplazo = "userDefault";
        props.setProperty("user.home",
                props.getProperty("user.home")
                        .replaceAll(props.getProperty("user.name"),reemplazo ));
        props.setProperty("user.dir",
                props.getProperty("user.dir")
                        .replaceAll(props.getProperty("user.name"),reemplazo ));

        props.setProperty("java.io.tmpdir",
                props.getProperty("java.io.tmpdir")
                        .replaceAll(props.getProperty("user.name"),reemplazo ));

        props.setProperty("java.library.path",
                props.getProperty("java.library.path")
                        .replaceAll(props.getProperty("user.name"),reemplazo ));

        props.setProperty("java.class.path",
                props.getProperty("java.class.path")
                .replaceAll(props.getProperty("user.name"),reemplazo));


        props.setProperty("user.name",reemplazo);
    }
}
