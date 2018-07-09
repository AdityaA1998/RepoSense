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
    private static final Path ARCHIVE_JSON_ZIP_PATH = Paths.get(REPORT_DIRECTORY_ABSOLUTE.toString(),
            "archiveJSON.zip");
    private static final Path TEMPLATE_ZIP_PATH = new File(FileUtilTest.class.getClassLoader()
            .getResource("output/template.zip").getFile()).toPath().toAbsolutePath();
    private static final Path TEMPLATE_DIRECTORY_ABSOLUTE = Paths.get(OUTPUT_DIRECTORY_ABSOLUTE.toString(), "template");

    @Test
    public void zipTest_success() throws IOException {
        FileUtil.zipJson(REPORT_DIRECTORY_ABSOLUTE);
        Assert.assertTrue(Files.exists(ARCHIVE_JSON_ZIP_PATH));
        Assert.assertTrue(Files.size(ARCHIVE_JSON_ZIP_PATH) > 0);
    }

    @Test
    public void zipTest_failure() {
        Assert.assertTrue(Files.notExists(ARCHIVE_JSON_ZIP_PATH));
    }

    @Test
    public void unzipTest_success() throws IOException {
        FileUtil.unzip(TEMPLATE_ZIP_PATH, TEMPLATE_DIRECTORY_ABSOLUTE);
        Assert.assertTrue(Files.exists(TEMPLATE_DIRECTORY_ABSOLUTE));
        //Assert.assertTrue(Files.size(TEMPLATE_DIRECTORY_ABSOLUTE) > 0);
    }

    @Test
    public void unzipTest_failure() {
        Assert.assertFalse(Files.exists(TEMPLATE_DIRECTORY_ABSOLUTE));
    }

    @After
    public void after() throws IOException, NullPointerException {
        Files.deleteIfExists(ARCHIVE_JSON_ZIP_PATH);
        if (Files.exists(TEMPLATE_DIRECTORY_ABSOLUTE)) {
            FileUtil.deleteDirectory(TEMPLATE_DIRECTORY_ABSOLUTE.toString());
        }
    }
}
