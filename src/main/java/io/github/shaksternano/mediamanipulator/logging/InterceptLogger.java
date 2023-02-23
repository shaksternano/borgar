package io.github.shaksternano.mediamanipulator.logging;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;

public abstract class InterceptLogger implements Logger {

    protected final Logger delegate;

    public InterceptLogger(Logger logger) {
        delegate = logger;
    }

    protected abstract void intercept(Level level, String message, @Nullable Throwable t, Object... arguments);

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        delegate.trace(msg);
        intercept(Level.TRACE, msg, null);
    }

    @Override
    public void trace(String format, Object arg) {
        delegate.trace(format, arg);
        intercept(Level.TRACE, format, null, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        delegate.trace(format, arg1, arg2);
        intercept(Level.TRACE, format, null, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        delegate.trace(format, arguments);
        intercept(Level.TRACE, format, null, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        delegate.trace(msg, t);
        intercept(Level.TRACE, msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        delegate.trace(marker, msg);
        intercept(Level.TRACE, msg, null);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        delegate.trace(marker, format, arg);
        intercept(Level.TRACE, format, null, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        delegate.trace(marker, format, arg1, arg2);
        intercept(Level.TRACE, format, null, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        delegate.trace(marker, format, argArray);
        intercept(Level.TRACE, format, null, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        delegate.trace(marker, msg, t);
        intercept(Level.TRACE, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        delegate.debug(msg);
        intercept(Level.DEBUG, msg, null);
    }

    @Override
    public void debug(String format, Object arg) {
        delegate.debug(format, arg);
        intercept(Level.DEBUG, format, null, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        delegate.debug(format, arg1, arg2);
        intercept(Level.DEBUG, format, null, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        delegate.debug(format, arguments);
        intercept(Level.DEBUG, format, null, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        delegate.debug(msg, t);
        intercept(Level.DEBUG, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        delegate.debug(marker, msg);
        intercept(Level.DEBUG, msg, null);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        delegate.debug(marker, format, arg);
        intercept(Level.DEBUG, format, null, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        delegate.debug(marker, format, arg1, arg2);
        intercept(Level.DEBUG, format, null, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        delegate.debug(marker, format, arguments);
        intercept(Level.DEBUG, format, null, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        delegate.debug(marker, msg, t);
        intercept(Level.DEBUG, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        delegate.info(msg);
        intercept(Level.INFO, msg, null);
    }

    @Override
    public void info(String format, Object arg) {
        delegate.info(format, arg);
        intercept(Level.INFO, format, null, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        delegate.info(format, arg1, arg2);
        intercept(Level.INFO, format, null, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        delegate.info(format, arguments);
        intercept(Level.INFO, format, null, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        delegate.info(msg, t);
        intercept(Level.INFO, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        delegate.info(marker, msg);
        intercept(Level.INFO, msg, null);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        delegate.info(marker, format, arg);
        intercept(Level.INFO, format, null, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        delegate.info(marker, format, arg1, arg2);
        intercept(Level.INFO, format, null, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        delegate.info(marker, format, arguments);
        intercept(Level.INFO, format, null, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        delegate.info(marker, msg, t);
        intercept(Level.INFO, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        delegate.warn(msg);
        intercept(Level.WARN, msg, null);
    }

    @Override
    public void warn(String format, Object arg) {
        delegate.warn(format, arg);
        intercept(Level.WARN, format, null, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        delegate.warn(format, arguments);
        intercept(Level.WARN, format, null, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        delegate.warn(format, arg1, arg2);
        intercept(Level.WARN, format, null, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        delegate.warn(msg, t);
        intercept(Level.WARN, msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        delegate.warn(marker, msg);
        intercept(Level.WARN, msg, null);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        delegate.warn(marker, format, arg);
        intercept(Level.WARN, format, null, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        delegate.warn(marker, format, arg1, arg2);
        intercept(Level.WARN, format, null, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        delegate.warn(marker, format, arguments);
        intercept(Level.WARN, format, null, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        delegate.warn(marker, msg, t);
        intercept(Level.WARN, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        delegate.error(msg);
        intercept(Level.ERROR, msg, null);
    }

    @Override
    public void error(String format, Object arg) {
        delegate.error(format, arg);
        intercept(Level.ERROR, format, null, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        delegate.error(format, arg1, arg2);
        intercept(Level.ERROR, format, null, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        delegate.error(format, arguments);
        intercept(Level.ERROR, format, null, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        delegate.error(msg, t);
        intercept(Level.ERROR, msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        delegate.error(marker, msg);
        intercept(Level.ERROR, msg, null);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        delegate.error(marker, format, arg);
        intercept(Level.ERROR, format, null, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        delegate.error(marker, format, arg1, arg2);
        intercept(Level.ERROR, format, null, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        delegate.error(marker, format, arguments);
        intercept(Level.ERROR, format, null, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        delegate.error(marker, msg, t);
        intercept(Level.ERROR, msg, t);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + delegate + "]";
    }
}
