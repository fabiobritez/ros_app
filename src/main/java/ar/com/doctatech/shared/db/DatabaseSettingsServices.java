package ar.com.doctatech.shared.db;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * Administra la conexión de la base de datos. Tiene metodos para administrar los 
 * atributos necesarios para la conexión a la base de datos.
 * @author: f.b.
 * @version: 1.0
 * @since 1.0
 */

public class DatabaseSettingsServices {

    private static final String PARAMETERS_FILE_PATH =  "src/main/resources/database/db.properties";

    protected static final String URL_KEY   = "url";
    protected static final String RDBMS_KEY = "rdbms";
    protected static final String HOST_KEY  = "host";
    protected static final String PORT_KEY  = "port";
    protected static final String USER_KEY  = "user";
    protected static final String DB_NAME_KEY = "database";
    protected static final String PASSWORD_KEY   = "password";
    protected static final String PROPERTIES_KEY = "properties";

    /**
     * Obtiene la URL para la conexión a la database desde el archivo de configuración
     * (indicado en la constante {@code PARAMETERS_FILE_PATH}).
     *
     * El archivo debe ser de tipo {@code .properties} y tener el parametro {@code URL_KEY}.
     *
     * @return La URL para la conexión a la base de datos del archivo. Si no existe el archivo lanza 
     * una Excepción.
     *
     * @exception IOException si ocurrió un error al leer desde la secuencia de entrada: 
     * FileInputStream(PARAMETERS_FILE_PATH)
     */
    protected static String getConnectionURL() throws IOException
    {
        try(FileInputStream configFileInput = new FileInputStream(PARAMETERS_FILE_PATH))
        {
            Properties parameters = new Properties();
            parameters.load(configFileInput);
            return parameters.getProperty(URL_KEY);
        }
    }

    /**
     * Obtiene el {@code Properties} con la configuración de la conexión a la base de datos
     * almacenada en el archivo por defecto.
     *
     * @return Configuracion de la conexion a la base de datos guardada.
     * @throws IOException si ocurrió un error al leer desde la secuencia de entrada:
     *   FileInputStream(PARAMETERS_FILE_PATH)
     */
    protected static Properties getDatabaseConnectionSettings() throws IOException
    {
        try(FileInputStream configFileInput = new FileInputStream(PARAMETERS_FILE_PATH))
        {
            Properties parameters = new Properties();
            parameters.load(configFileInput);
            return parameters;
        }
    }

    /**
     * Recibe los parametros para la conexión a la database y los escribe en un archivo 
     * de configuración {@code .properties}.
     *
     * @param rdbms Sistema gestor de base de datos relacional
     * @param host  Host o IP del servidor donde se encuentra la base de datos.
     * @param port  Puerto del servidor.
     * @param database Nombre de la base de datos a la que se conecta.
     * @param user Nombre de usuario con el que accede.
     * @param password Contraseña del usuario ingresado.
     * @param properties Propiedades y valores que se agregan a la URL. Deben iniciar con {@code &}
     *
     * @throws FileNotFoundException Si el path existe pero es un directorio en lugar de ser archivo normal.
     * Si el archivo indicado no existe ni puede ser creado o no puede ser abierto por algún otro motivo.
     * @throws IOException Si al escribir la lista de properties en el OutputStream especificado lanza una IOException
     */
    protected static void setConnectionURL(String rdbms, String host,String port,
                                        String database, String user, String password, String properties)
            throws FileNotFoundException, IOException
    {
        Properties propertiesDB = new Properties();

        propertiesDB.setProperty(RDBMS_KEY,rdbms);
        propertiesDB.setProperty(HOST_KEY,host);
        propertiesDB.setProperty(PORT_KEY,port);
        propertiesDB.setProperty(DB_NAME_KEY,database);
        propertiesDB.setProperty(USER_KEY,user);
        propertiesDB.setProperty(PASSWORD_KEY,password);
        propertiesDB.setProperty(PROPERTIES_KEY, properties);

        storeConnectionURL(propertiesDB);
    }

    /**
     * Primero obtiene el formato de la URL a partir del {@code properties} ingresado.
     * Crea un nuevo parametro con {@code URL_KEY} y almacena la URl del acceso.
     * Finalmente intenta almacenar el {@code properties} en el archivo ubicado en
     * {@code PARAMETERS_FILE_PATH}.
     *
     * @param properties Properties con los parametros para la conexión a la base de datos.
     * @throws IOException Si hay un problema al guardar crear y guardar el archivo.
     */
    private static void storeConnectionURL(Properties properties) throws IOException
    {
        String url = getURLFormat(properties);
        properties.setProperty(URL_KEY, url);

        try(OutputStream fileOutput = new FileOutputStream(PARAMETERS_FILE_PATH))
        {
            properties.store(fileOutput, "DATABASE PARAMETERS");
        }

        System.out.println("url saved: "+url+"\n");
    }

    /**
     * Construye una URL para la conexion a la database con los parametros del objeto
     * {@code Properties} recibido. Devuelve la URL preparada.
     * @param parameters {@code Properties} con los valores de los parametros de la conexión cargados.
     * @return La URL para la conexión a la base de datos ya formateada y lista para usar.
     * Por ejemplo {@code jdbc:[dbrms]://[host][:port][/[database]][?propertyName=propertyValue&...]}
     */
    protected static String getURLFormat(Properties parameters)
    {
        String rdbms      = parameters.getProperty(RDBMS_KEY);
        String host       = parameters.getProperty(HOST_KEY);
        String port       = parameters.getProperty(PORT_KEY);
        String database   = parameters.getProperty(DB_NAME_KEY);
        String user       = parameters.getProperty(USER_KEY);
        String password   = parameters.getProperty(PASSWORD_KEY);
        String properties = parameters.getProperty(PROPERTIES_KEY);

        switch (rdbms) {
            case "mysql":
            case "postgresql":
            case "mariadb":
                return String.format("jdbc:%s://%s:%s/%s?user=%s&password=%s%s",
                        rdbms, host, port, database, user, password, properties);
            case "oracle":
                return String.format("jdbc:%s:thin:%s/%s@%s:%s:%s%s",
                        rdbms, user, password, host, port, database, properties);
            default:
                return "RDBMS NOT SUPPORTED";
        }
    }

    /**
     * Busca todos los dispositivos conectados a la red de area local y almacena en
     * {@code HashMap} que tiene como {@code key = Host} y {@code value = Hostname}.
     * @return HashMap con los host y hostname de los dispositivos conectados a la red local.
     * @throws SocketException if an I/O error occurs
     */
    protected static HashMap<String, String> getDevicesConnectedOnLAN() throws SocketException
    {
        //par host, hostname
        HashMap<String, String> devices = new HashMap<>();

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements())
            {
                networkInterfaces.nextElement().getInterfaceAddresses().forEach((f) ->
                {
                    String address = f.getAddress().getHostAddress();
                    if( !address.startsWith("fe80:") && !address.startsWith("0:0:0:0:0:0:0:1")){
                        devices.put(f.getAddress().getHostAddress(),f.getAddress().getHostName());
                    }
                });
            }
        return devices;
    }

    /**
     * Busca todos los dispositivos conectados a la red de area local y almacena en
     * {@code HashMap} que tiene como {@code key = Host} y {@code value = Hostname}.
     * Este metodo es más lento que los demas, pero tiene mayor fiabilidad.
     *
     * @param subnet Prefijo de la red del hogar. Subnet
     * @return HashMap con los host y hostname de los dispositivos conectados a la red local.
     * @throws UnknownHostException  if no IP address for the host could be found,
     * or if a scope_id was specified for a global IPv6 address
     * @throws IOException if a network error occurs
     * @deprecated
     */
    protected static HashMap<String, String> getDevicesConnectedOnLan(String subnet)
            throws IOException, UnknownHostException
    {
        HashMap<String, String> devices = new HashMap<>();

        int timeout=1;
            for (int i=1;i<255;i++){
                String host=subnet + "." + i;

                if (InetAddress.getByName(host).isReachable(timeout)){
                    System.out.println(host + " Encontrado");
                    devices.put(host, InetAddress.getByName(host).getHostName());
                }else{
                    System.out.println(host + " DENEGADO");
                }
            }
        return devices;
    }

}
