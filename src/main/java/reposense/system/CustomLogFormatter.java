package reposense.system;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class CustomLogFormatter extends SimpleFormatter {

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    @Override
    public synchronized String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(dateFormat.format(new Date(record.getMillis()))).append(" - ");
        builder.append("[").append(record.getLevel()).append("] - ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}