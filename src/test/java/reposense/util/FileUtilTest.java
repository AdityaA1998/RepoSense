package reposense.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class FileUtilTest {

    private static final Path REPORT_DIRECTORY_ABSOLUTE = new File(FileUtilTest.class.getClassLoader()
            .getResource("reposense-report-test").getFile()).toPath().toAbsolutePath();
    private static final Path OUTPUT_DIRECTORY_ABSOLUTE = new File(FileUtilTest.class.getClassLoader()
            .getResource("output").getFile()).toPath().toAbsolutePath();
    private static final Path ARCHIVE_ZIP_PATH = Paths.get(OUTPUT_DIRECTORY_ABSOLUTE.toString(),
            FileUtil.ZIP_FILE);
    private static final Path EXPECTED_UNZIPPED_DIRECTORY_PATH = Paths.get(OUTPUT_DIRECTORY_ABSOLUTE.toString(),
            "expectedUnzip");
    private static final Path TEST_ZIP_PATH = new File(FileUtilTest.class.getClassLoader()
            .getResource("output/testZip.zip").getFile()).toPath().toAbsolutePath();
    private static final Path UNZIPPED_DIRECTORY_PATH = Paths.get(OUTPUT_DIRECTORY_ABSOLUTE.toString(),
            "UnzippedFolder");

    @Test
    public void zip_validLocation_success() throws IOException {
        FileUtil.zip(REPORT_DIRECTORY_ABSOLUTE, OUTPUT_DIRECTORY_ABSOLUTE, ".json");
        Assert.assertTrue(Files.exists(ARCHIVE_ZIP_PATH));
        Assert.assertTrue(Files.size(ARCHIVE_ZIP_PATH) > 0);
    }

    @Test
    public void zip_validFileType_success() throws IOException {
        FileUtil.zip(OUTPUT_DIRECTORY_ABSOLUTE, ".csv");
        Assert.assertTrue(Files.exists(ARCHIVE_ZIP_PATH));
        Assert.assertTrue(Files.size(ARCHIVE_ZIP_PATH) > 0);
    }

    @Test
    public void unzip_validZipFile_success() throws IOException {
        FileUtil.unzip(TEST_ZIP_PATH, UNZIPPED_DIRECTORY_PATH);
        Assert.assertTrue(Files.exists(UNZIPPED_DIRECTORY_PATH));
        Assert.assertTrue(TestUtil.compareDirectories(UNZIPPED_DIRECTORY_PATH, EXPECTED_UNZIPPED_DIRECTORY_PATH));
    }

    @Test
    public void unzip_invalidZipFile_fail() {
        Path invalidZipFile = Paths.get(OUTPUT_DIRECTORY_ABSOLUTE.toString(), "parser_test.csv");
        FileUtil.unzip(invalidZipFile, OUTPUT_DIRECTORY_ABSOLUTE);
        Assert.assertFalse(Files.exists(Paths.get(OUTPUT_DIRECTORY_ABSOLUTE.toString(), "parser_test")));
    }

    @After
    public void after() throws IOException, NullPointerException {
        Files.deleteIfExists(ARCHIVE_ZIP_PATH);
        if (Files.exists(UNZIPPED_DIRECTORY_PATH)) {
            FileUtil.deleteDirectory(UNZIPPED_DIRECTORY_PATH.toString());
        }
    }
}
