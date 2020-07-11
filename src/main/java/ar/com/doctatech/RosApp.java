package ar.com.doctatech;

import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.List;

import static ar.com.doctatech.shared.utilities.FXPath.*;


public class RosApp extends Application {

    @Override
    public void start(Stage stage)
    {
     
        String pathToLoad = "";

        /** VERIFICAR LA LA CONEXION, EN CASO DE SER CONECTABLE SETEA "LOGIN" PARA ABRIR
         SI NO ES CONECTABLE SETEA "DATABASE_SETTINGS" */
        try{
            if( DatabaseConnection.isConnectable() )  pathToLoad = LOGIN;
        }
        catch (IOException | SQLException exception) {
            System.out.println(exception.toString());
            pathToLoad = DATABASE_SETTINGS;
        }

        /** CARGA LA VENTANA DE pathToLoad **/
        try {
            Parent root = FXMLLoader.load(getClass().getResource(pathToLoad));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
        catch (Exception exception){
            System.out.println(exception.toString());
            exception.printStackTrace();
            FXTool.alertException(exception);
        }

    }

    public static void main(String[] args)
    {
        launch(args);
    }



    /**
     * Sun property pointing the main class and its arguments.
     * Might not be defined on non Hotspot VM implementations.
     */
    public static final String SUN_JAVA_COMMAND = "sun.java.command";

    /**
     * Restart the current Java application
     * @param runBeforeRestart some custom code to be run before restarting
     * @throws IOException
     * @see "https://dzone.com/articles/programmatically-restart-java"
     */
    public static void restartApplication(Runnable runBeforeRestart) throws IOException
    {
        try {
// java binary
            String java = System.getProperty("java.home") + "/bin/java";
// vm arguments
            List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
            StringBuffer vmArgsOneLine = new StringBuffer();
            for (String arg : vmArguments) {
// if it's the agent argument : we ignore it otherwise the
// address of the old application and the new one will be in conflict
                if (!arg.contains("-agentlib")) {
                    vmArgsOneLine.append(arg);
                    vmArgsOneLine.append(" ");
                }
            }
// init the command to execute, add the vm args
            final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);

// program main and program arguments
            String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
// program main is a jar
            if (mainCommand[0].endsWith(".jar")) {
// if it's a jar, add -jar mainJar
                cmd.append("-jar " + new File(mainCommand[0]).getPath());
            } else {
// else it's a .class, add the classpath and mainClass
                cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
            }
// finally add program arguments
            for (int i = 1; i < mainCommand.length; i++) {
                cmd.append(" ");
                cmd.append(mainCommand[i]);
            }
// execute the command in a shutdown hook, to be sure that all the
// resources have been disposed before restarting the application
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Runtime.getRuntime().exec(cmd.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
// execute some custom code before restarting
            if (runBeforeRestart!= null) {
                runBeforeRestart.run();
            }
// exit
            System.exit(0);
        } catch (Exception e) {
// something went wrong
            throw new IOException("Error while trying to restart the application", e);
        }
    }


}
