package ar.com.doctatech.shared.utilities;


import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileUtil {

    public static void copyFile(File sourceFile, File destFile)
            throws IOException
    {
        if (!destFile.exists())
        {
            System.err.println(destFile.getPath());
            destFile.createNewFile();
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel())
        {
            try (FileChannel destination = new FileOutputStream(destFile).getChannel())
            {
                destination.transferFrom(source, 0, source.size());
                System.out.println("Copiying from :\n"+sourceFile.getPath()+" \nto\n"+destFile.getPath());
            }
        }
    }

    public static String createHomeFoodIfNoExists()
    {
        Path path = Paths.get(System.getProperty("user.home")+
                File.separator + "RosApp" + File.separator + "food" + File.separator + "img"
        );
         if (!Files.exists(path))
         {
            try
            {
                Files.createDirectories(path);
            }
            catch (IOException e)
            {
                FXTool.alertException(e);
            }
        }
         return path.toString();
    }

    public static String getConfigDirIfNotExists()
    {
        Path path = Paths.get(System.getProperty("user.home")+
                File.separator + "RosApp" + File.separator + ".config"
        );
        if (!Files.exists(path))
        {
            try
            {
                Files.createDirectories(path);
            }
            catch (IOException e)
            {
                FXTool.alertException(e);
            }
        }
        return path.toString();
    }

    public static Optional<String> getExtension(String filename)
    {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")));
    }

    public static void deleteFile(String pathFile)
    {
        File file = new File(pathFile);
        if(file.exists())
        {
            file.delete();
        }
    }
}
