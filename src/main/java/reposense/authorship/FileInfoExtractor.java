import java.util.function.Predicate;
    private static final String MATCH_GROUP_FAIL_MESSAGE_FORMAT = "Failed to match the %s group for:\n%s";
    private static final Predicate<String> FILE_DELETED_PREDICATE = Pattern.compile(
            "^deleted file mode [\\d]{6}\n").asPredicate();
    private static final Predicate<String> NEW_EMPTY_FILE_PREDICATE = Pattern.compile(
            "^new file mode [a-zA-Z0-9\n. ]*$").asPredicate();

            // file deleted, is binary file or is new and empty file, skip it
            if (FILE_DELETED_PREDICATE.test(fileDiffResult) || fileDiffResult.contains(BINARY_FILE_SYMBOL)
                    || NEW_EMPTY_FILE_PREDICATE.test(fileDiffResult)) {
            logger.severe(String.format(MATCH_GROUP_FAIL_MESSAGE_FORMAT, "file path", fileDiffResult));
            logger.severe(String.format(MATCH_GROUP_FAIL_MESSAGE_FORMAT, "line changed", linesChanged));