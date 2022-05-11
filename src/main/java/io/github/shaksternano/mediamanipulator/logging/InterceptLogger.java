package io.github.shaksternano.mediamanipulator.logging;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;

public abstract class InterceptLogger implements Logger {

    private final Logger logger;

    public InterceptLogger(Logger logger) {
        this.logger = logger;
    }

    protected abstract void intercept(Level level, String message, @Nullable Throwable t, Object... arguments);

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        logger.trace(msg);
        intercept(Level.TRACE, msg, null);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
        intercept(Level.TRACE, format, null, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
        intercept(Level.TRACE, format, null, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
        intercept(Level.TRACE, format, null, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
        intercept(Level.TRACE, msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        logger.trace(marker, msg);
        intercept(Level.TRACE, msg, null);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, format, arg);
        intercept(Level.TRACE, format, null, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, format, arg1, arg2);
        intercept(Level.TRACE, format, null, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logger.trace(marker, format, argArray);
        intercept(Level.TRACE, format, null, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, msg, t);
        intercept(Level.TRACE, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
        intercept(Level.DEBUG, msg, null);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
        intercept(Level.DEBUG, format, null, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
        intercept(Level.DEBUG, format, null, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
        intercept(Level.DEBUG, format, null, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
        intercept(Level.DEBUG, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        logger.debug(marker, msg);
        intercept(Level.DEBUG, msg, null);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, format, arg);
        intercept(Level.DEBUG, format, null, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, format, arg1, arg2);
        intercept(Level.DEBUG, format, null, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(marker, format, arguments);
        intercept(Level.DEBUG, format, null, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, msg, t);
        intercept(Level.DEBUG, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
        intercept(Level.INFO, msg, null);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
        intercept(Level.INFO, format, null, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
        intercept(Level.INFO, format, null, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
        intercept(Level.INFO, format, null, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
        intercept(Level.INFO, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
        intercept(Level.INFO, msg, null);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, arg);
        intercept(Level.INFO, format, null, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
        intercept(Level.INFO, format, null, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, format, arguments);
        intercept(Level.INFO, format, null, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, t);
        intercept(Level.INFO, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
        intercept(Level.WARN, msg, null);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
        intercept(Level.WARN, format, null, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
        intercept(Level.WARN, format, null, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
        intercept(Level.WARN, format, null, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
        intercept(Level.WARN, msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
        intercept(Level.WARN, msg, null);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, arg);
        intercept(Level.WARN, format, null, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
        intercept(Level.WARN, format, null, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, format, arguments);
        intercept(Level.WARN, format, null, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, t);
        intercept(Level.WARN, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
        intercept(Level.ERROR, msg, null);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
        intercept(Level.ERROR, format, null, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
        intercept(Level.ERROR, format, null, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
        intercept(Level.ERROR, format, null, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
        intercept(Level.ERROR, msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
        intercept(Level.ERROR, msg, null);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, arg);
        intercept(Level.ERROR, format, null, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
        intercept(Level.ERROR, format, null, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, format, arguments);
        intercept(Level.ERROR, format, null, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, t);
        intercept(Level.ERROR, msg, t);
    }
}
