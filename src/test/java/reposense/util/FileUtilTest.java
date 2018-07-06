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

    private static final Path OUTPUT_DIRECTORY_ABSOLUTE = new File(FileUtilTest.class.getClassLoader()
            .getResource("reposense-report-test").getFile()).toPath().toAbsolutePath();
    private static final Path ARCHIVE_JSON_PATH = Paths.get(OUTPUT_DIRECTORY_ABSOLUTE.toString(), "archiveJSON.zip");

    @Test
    public void zipTest_success() {
        FileUtil.zipJson(OUTPUT_DIRECTORY_ABSOLUTE);
        Assert.assertTrue(Files.exists(ARCHIVE_JSON_PATH));
    }

    @Test
    public void zipTest_throwsNullPointerException() throws NullPointerException {
        Assert.assertTrue(Files.notExists(ARCHIVE_JSON_PATH));
    }

    @After
    public void after() throws IOException, NullPointerException {
        Files.deleteIfExists(ARCHIVE_JSON_PATH);
    }
}
