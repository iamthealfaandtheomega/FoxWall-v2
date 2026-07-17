package thezowi.foxwall.proxy.velocity;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.slf4j.Logger;

public final class LoggerWrapper extends java.util.logging.Logger {
    private final Logger logger;

    public LoggerWrapper(final Logger logger) {
        super("logger", null);
        this.logger = logger;
    }
    @Override
    public void log(final LogRecord record) {
    	String msg = format(record);
    	Throwable throwable = record.getThrown();

    	Level level = record.getLevel();
    	if (level == Level.SEVERE) {
    		logger.error(msg, throwable);
    	} else if (level == Level.WARNING) {
    		logger.warn(msg, throwable);
    	} else if (level == Level.INFO) {
    		logger.info(msg, throwable);
    	} else if (level == Level.FINE) {
    		logger.debug(msg, throwable);
    	} else {
    		logger.trace(msg, throwable);
    	}
    }

    protected static String format(LogRecord r) {
    	String msg = r.getMessage();
    	Object[] params = r.getParameters();
    	if (params != null && params.length > 0) {
    		try {
    			msg = MessageFormat.format(msg, params);
    		} catch (IllegalArgumentException ignored) {}
    	}
    	return msg;
    }
}