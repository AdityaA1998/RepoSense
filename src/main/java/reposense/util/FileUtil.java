package reposense.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reposense.RepoSense;
import reposense.system.LogsManager;


public class FileUtil {
    private static Logger logger = LogsManager.getLogger(FileUtil.class);

    private static final String GITHUB_API_DATE_FORMAT = "yyyy-MM-dd";

    public static void writeJsonFile(Object object, String path) {
        Gson gson = new GsonBuilder()
                .setDateFormat(GITHUB_API_DATE_FORMAT)
                .setPrettyPrinting()
                .create();
        String result = gson.toJson(object);

        try (PrintWriter out = new PrintWriter(path)) {
            out.print(result);
            out.print("\n");
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static String getRepoDirectory(String org, String repoName) {
        return Constants.REPOS_ADDRESS + File.separator + org + File.separator + repoName + File.separator;
    }

    public static void deleteDirectory(String root) throws IOException {
        Path rootPath = Paths.get(root);
        if (Files.exists(rootPath)) {
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(filePath -> filePath.toFile().delete());
        }
    }

    /**
     * Zips all the JSON files contained in the {@code sourcePath} directory.
     * Creates the zip folder in the {@code outputPath}
     */
    public static void zipJson(String outputPath, String sourcePath) {
        List<Path> allJsonFiles = getFilePath(outputPath + File.separator + sourcePath, ".json");

        ByteBuffer buffer = ByteBuffer.allocate(1 << 10);
        int length;
        try (FileOutputStream fos = new FileOutputStream(outputPath + File.separator + Constants.JSON_ZIP_FILE);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (Path jsonFile: allJsonFiles) {
                try (FileInputStream fis = new FileInputStream(jsonFile.toFile())) {
                    zos.putNextEntry(new ZipEntry(getChild(jsonFile.toString(), sourcePath + File.separator)));
                    while ((length = fis.read(buffer.array())) > 0) {
                        zos.write(buffer.array(), 0, length);
                    }
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
        }
    }

    /**
     * Unzips the contents of the {@code zipInput} and stores in the {@code destinationFolder}
     * */
    public static void unzip(ZipInputStream zipInput, String destinationFolder) {

        ByteBuffer buffer = ByteBuffer.allocate(1 << 11);
        try {
            Files.createDirectories(Paths.get(destinationFolder));
            ZipEntry entry = zipInput.getNextEntry();

            while (entry != null) {
                Path path = Paths.get(destinationFolder, entry.getName());
                // create the directories of the zip directory
                if (entry.isDirectory()) {
                    Files.createDirectories(path.toAbsolutePath());
                } else {
                    if (!Files.exists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }
                    try (OutputStream output = Files.newOutputStream(path)) {
                        int count;
                        while ((count = zipInput.read(buffer.array())) > 0) {
                            output.write(buffer.array(), 0, count);
                        }
                    }
                }
                zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }

            zipInput.closeEntry();
            zipInput.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Copies the template files to the {@code outputPath}'s {@code reportName} directory.
     */
    public static void copyTemplate(String outputPath, String reportName) {
        String templateLocation = outputPath + File.separator + reportName;
        InputStream is = RepoSense.class.getResourceAsStream(Constants.TEMPLATE_ZIP_ADDRESS);
        FileUtil.unzip(new ZipInputStream(is), templateLocation);
    }

    /**
     * Copies all the files inside {@code src} directory to {@code dest} directory.
     * Creates the {@code dest} directory if it does not exist.
     */
    public static void copyDirectoryFiles(Path src, Path dest) throws IOException {
        Files.createDirectories(dest);
        try (Stream<Path> pathStream = Files.list(src)) {
            for (Path filePath : pathStream.collect(Collectors.toList())) {
                Files.copy(filePath, dest.resolve(src.relativize(filePath)));
            }
        }
    }

    /**
     * Returns a list of paths of {@code fileType} contained in the given {@code directoryName} directory.
     */
    private static List<Path> getFilePath(String directoryName, String fileType) {
        Path directory = Paths.get(directoryName);
        List<Path> filePaths = null;
        try {
            filePaths = Files.walk(directory)
                    .filter(p -> p.toString().endsWith(fileType))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
        }
        return filePaths;
    }

    /**
     * Returns the child contained in the {@sourcePath} of the {@code fullPath}
     * */
    private static String getChild(String fullPath, String sourcePath) {
        return fullPath.substring(fullPath.indexOf(sourcePath) + sourcePath.length());
    }

    private static String attachJsPrefix(String original, String prefix) {
        return "var " + prefix + " = " + original;
    }
}
