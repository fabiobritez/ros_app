package ar.com.doctatech.shared.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Encargada de las conexiones con la base de datos.
 * @author: f.b.
 * @version: 1.0
 * @since 1.0
 */

public class DatabaseConnection {

    private static Connection conn;

    private DatabaseConnection() { }

    /**
     * Realiza una conexion con el driver de la database con la URL de paramatro.<p>
     * Si quieres obtenerla desde un archivo, puedes ir a {@link DatabaseSettingsServices}
     *
     * @param url la url con la que se quiere realizar la conexión a la database
     * @return Si la conexión es exitosa devuelve el objeto de tipo <code>Connection</code>. En caso contrario devuelve <code>null</code>.
     *
     * @exception SQLException Si ocurrió un error de acceso a la base de datos ó si la URL es incorrecta/nula.
     *
     * @see DatabaseSettingsServices#getConnectionURL() <code> Clase con metodos para obtener la URL desde un {@code Properties}
     */

    public static Connection getConnection(String url) throws SQLException
    {
        if(conn==null) conn = DriverManager.getConnection(url);
        return conn;
    }

    /**
     * Obtiene la URL desde el archivo {@code properties} por defecto e intenta conectarse con el
     * @return Devuelve el objeto {@code Connection} de la conexión.
     * @throws IOException Si ocurrió un error de lectura en el metodo {@link DatabaseSettingsServices#getConnectionURL()}
     * @throws SQLException Si ocurrió un error de acceso a la base de datos ó si la URL es incorrecta/nula.
     */
    public static Connection getConnection() throws IOException, SQLException
    {
        return getConnection(DatabaseSettingsServices.getConnectionURL());
    }

    /**
     * Intenta establecer la conexión con la base de datos para verificar que la
     * configuración esté correcta. En caso de conectarse imprime en consola la
     * versión del servidor de base de datos
     *
     * @return true si es conectable con la configuración guardada. false si no lo es.
     * @throws SQLException error al conectarse con la url configurada
     */
    public static boolean isConnectable() throws SQLException, IOException
    {
        Connection con = getConnection();

        if (con != null) {
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
                    if (rs.next()) {
                        System.out.println("CONNECTABLE: " + rs.getString(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * NO RECOMENDADO
     * @deprecated
     */
    public static void disconnect()
    {
        conn = null;
    }
}
