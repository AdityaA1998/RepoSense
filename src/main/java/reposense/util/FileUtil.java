package reposense.util;

import java.io.File;
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
    private static final ByteBuffer buffer = ByteBuffer.allocate(1 << 11);


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

        int length;
        try (FileOutputStream fos = new FileOutputStream(outputPath + File.separator + Constants.JSON_ZIP_FILE);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            List<Path> allJsonFiles = getFilePaths(outputPath + File.separator + sourcePath, ".json");
            for (Path jsonFile : allJsonFiles) {
                try (InputStream is = Files.newInputStream(jsonFile)) {
                    zos.putNextEntry(new ZipEntry(getChild(jsonFile.toString(), sourcePath + File.separator)));
                    while ((length = is.read(buffer.array())) > 0) {
                        zos.write(buffer.array(), 0, length);
                    }
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
        }
    }

    /**
     * Unzips the contents of the {@code is} and stores in the {@code destinationFolder}
     */
    public static void unzip(InputStream is, String destinationFolder) {
        ZipEntry entry;
        try (ZipInputStream zis = new ZipInputStream(is)) {
            Files.createDirectories(Paths.get(destinationFolder));
            while ((entry = zis.getNextEntry()) != null) {
                Path path = Paths.get(destinationFolder, entry.getName());
                // create the directories of the zip directory
                if (entry.isDirectory()) {
                    Files.createDirectories(path.toAbsolutePath());
                    zis.closeEntry();
                    continue;
                }
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                try (OutputStream output = Files.newOutputStream(path)) {
                    int length;
                    while ((length = zis.read(buffer.array())) > 0) {
                        output.write(buffer.array(), 0, length);
                    }
                }
                zis.closeEntry();
            }
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
        FileUtil.unzip(is, templateLocation);
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
     * Returns a list of {@code Path} of {@code fileType} contained in the given {@code directoryName} directory.
     */
    private static List<Path> getFilePaths(String directoryName, String fileType) throws IOException {
        Path directory = Paths.get(directoryName);
        List<Path> filePaths;
        filePaths = Files.walk(directory)
                .filter(p -> p.toString().endsWith(fileType))
                .collect(Collectors.toList());
        return filePaths;
    }

    /**
     * Returns the child contained in the {@code sourcePath} of the {@code fullPath}
     */
    private static String getChild(String fullPath, String sourcePath) {
        return fullPath.substring(fullPath.indexOf(sourcePath) + sourcePath.length());
    }

    private static String attachJsPrefix(String original, String prefix) {
        return "var " + prefix + " = " + original;
    }
}
