package reposense.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import reposense.model.RepoConfiguration;

public class TestUtil {
    private static final String MESSAGE_COMPARING_FILES = "Comparing files %s & %s\n";

    private static final String MESSAGE_LINE_CONTENT_DIFFERENT = "Content different at line number %d:\n"
            + "<< %s\n"
            + ">> %s\n";

    private static final String MESSAGE_LINES_LENGTH_DIFFERENT = "The files' lines count do not match.";

    /**
     * Returns true if the files' contents are the same.
     * Also prints out error message if the lines count are different,
     * else prints out the first line of content difference (if any).
     */
    public static boolean compareFileContents(Path expected, Path actual) throws IOException {
        return compareFileContents(expected, actual, 1);
    }

    /**
     * Returns true if the files' contents are the same.
     * Also prints out error message if the lines count are different,
     * else prints out maximum {@code maxTraceCounts} lines of content difference (if any).
     */
    public static boolean compareFileContents(Path expected, Path actual, int maxTraceCounts) throws IOException {
        int traceCounts = 0;

        System.out.println(String.format(MESSAGE_COMPARING_FILES, expected, actual));

        String[] expectedContent = new String(Files.readAllBytes(expected))
                .replace("\r", "").split("\n");
        String[] actualContent = new String(Files.readAllBytes(actual))
                .replace("\r", "").split("\n");

        for (int i = 0; i < Math.min(expectedContent.length, actualContent.length); i++) {
            if (!expectedContent[i].equals(actualContent[i])) {
                System.out.println(
                        String.format(MESSAGE_LINE_CONTENT_DIFFERENT, i + 1, expectedContent[i], actualContent[i]));
                if (++traceCounts >= maxTraceCounts) {
                    break;
                }
            }
        }
        if (expectedContent.length != actualContent.length) {
            System.out.println(MESSAGE_LINES_LENGTH_DIFFERENT);
            return false;
        } else if (traceCounts >= maxTraceCounts) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if {@code expected} directory has all files with same content as {@code actual} directory.
     */
    public static boolean compareDirectories(Path expected, Path actual) throws IOException {
        List<Path> expectedPaths = Files.walk(expected)
                .sorted()
                .collect(Collectors.toList());
        List<Path> actualPaths = Files.walk(actual)
                .sorted()
                .collect(Collectors.toList());

        if (expectedPaths.size() != actualPaths.size()) {
            return false;
        }

        for (int i = 0; i < expectedPaths.size(); i++) {
            if (!(Files.isDirectory(expectedPaths.get(i)) || Files.isDirectory(actualPaths.get(i)))
                    && !TestUtil.compareFileContents(expectedPaths.get(i), actualPaths.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates and returns a {@code Date} object with the specified {@code year}, {@code month}, {@code day}.
     */
    public static Date getDate(int year, int month, int date) {
        return new Calendar
                .Builder()
                .setDate(year, month, date)
                .setTimeOfDay(0, 0, 0)
                .build()
                .getTime();
    }

    /**
     * Compares attributes of {@code expectedRepoConfig} and {@code actualRepoConfig}.
     *
     * @throws AssertionError if any attributes fail equality check.
     */
    public static void compareRepoConfig(RepoConfiguration expectedRepoConfig, RepoConfiguration actualRepoConfig) {
        Assert.assertEquals(expectedRepoConfig.getLocation(), actualRepoConfig.getLocation());
        Assert.assertEquals(
                expectedRepoConfig.getAuthorList().hashCode(), actualRepoConfig.getAuthorList().hashCode());
        Assert.assertEquals(expectedRepoConfig.getAuthorDisplayNameMap().hashCode(),
                actualRepoConfig.getAuthorDisplayNameMap().hashCode());
        Assert.assertEquals(
                expectedRepoConfig.getAuthorAliasMap().hashCode(), actualRepoConfig.getAuthorAliasMap().hashCode());
        Assert.assertEquals(expectedRepoConfig.getIgnoreGlobList(), actualRepoConfig.getIgnoreGlobList());
        Assert.assertEquals(expectedRepoConfig.getFormats(), actualRepoConfig.getFormats());
    }

    /**
     * Returns true if the {@code expectedNumberCommits} is equal to the expected number of lines in
     * {@code gitLogResult}.
     */
    public static boolean compareNumberExpectedCommitsToGitLogLines(int expectedNumberCommits, String gitLogResult) {
        // if git log result is empty, then there are no commits
        if (gitLogResult.isEmpty()) {
            return expectedNumberCommits == 0;
        }

        // each commit has 2 lines of info, and a blank line in between each
        return expectedNumberCommits * 3 - 1 == gitLogResult.split("\n").length;
    }
}
