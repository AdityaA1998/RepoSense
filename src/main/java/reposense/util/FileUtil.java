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

    public static void unzip(ZipInputStream zipInput, String destinationFolder) {
        Path directory = Paths.get(destinationFolder);

        // buffer for read and write data to file
        byte[] buffer = new byte[2048];

        try {
            Files.createDirectories(directory);
            ZipEntry entry = zipInput.getNextEntry();

            while (entry != null) {
                String entryName = entry.getName();
                Path path = Paths.get(destinationFolder, entryName);
                // create the directories of the zip directory
                if (entry.isDirectory()) {
                    Path newDir = Paths.get(path.toAbsolutePath().toString());
                    Files.createDirectories(newDir);
                } else {
                    if (!Files.exists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }
                    OutputStream output = Files.newOutputStream(path);
                    int count = 0;
                    while ((count = zipInput.read(buffer)) > 0) {
                        // write 'count' bytes to the file output stream
                        output.write(buffer, 0, count);
                    }
                    output.close();
                }
                // close ZipEntry and take the next one
                zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }

            // close the last ZipEntry
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
     * Generates a list of paths of {@code fileType} contained in the given {@code directoryName} directory.
     */
    public static List<Path> listFilePath(String directoryName, String fileType) {
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
     * Zips all the JSON files contained in the {@code sourcePath} directory.
     * Creates the zip folder in the {@code outputPath}
     */
    public static void zipJson(String outputPath, String sourcePath) {
        String summaryJson = "summary.json";
        List<Path> allJsonFiles = listFilePath(outputPath + File.separator + sourcePath, ".json");

        //byte buffer for I/O
        ByteBuffer buffer = ByteBuffer.allocate(1 << 10);
        int length;
        try (FileOutputStream fos = new FileOutputStream(outputPath + File.separator + Constants.JSON_ZIP_FILE);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (int i = 0; i < allJsonFiles.size(); i++) {
                try (FileInputStream fis = new FileInputStream(allJsonFiles.get(i).toFile())) {

                    // begin writing a new ZIP entry, positions the stream to the start of the entry data
                    if (isFilePathValid(allJsonFiles.get(i), summaryJson)) {
                        zos.putNextEntry(new ZipEntry(allJsonFiles.get(i).getFileName().toString()));
                    } else {
                        zos.putNextEntry(new ZipEntry(allJsonFiles.get(i).getParent().getFileName().toString()
                                + File.separator + allJsonFiles.get(i).getFileName().toString()));
                    }

                    while ((length = fis.read(buffer.array())) > 0) {
                        zos.write(buffer.array(), 0, length);
                    }
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
        }
    }

    private static String attachJsPrefix(String original, String prefix) {
        return "var " + prefix + " = " + original;
    }

    /**
     * Checks if the given {@code path} is of {@code file}
     */
    private static boolean isFilePathValid(Path path, String file) {
        return path.getFileName().toString().equals(file);
    }

}
