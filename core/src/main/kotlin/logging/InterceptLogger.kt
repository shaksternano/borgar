package com.shakster.borgar.core.logging

import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level

class InterceptLogger(
    val delegate: Logger,
) : Logger {

    private val hooks: MutableList<LoggerHook> = mutableListOf()

    fun addHook(hook: LoggerHook) {
        hooks += hook
    }

    private fun runHooks(level: Level, message: String?, t: Throwable?, vararg arguments: Any?) {
        hooks.forEach { hook ->
            runCatching {
                hook.onLog(level, message, t, *arguments)
            }.onFailure {
                delegate.error("Failed run hook", it)
            }
        }
    }

    override fun getName(): String {
        return delegate.name
    }

    override fun isTraceEnabled(): Boolean {
        return delegate.isTraceEnabled
    }

    override fun trace(msg: String?) {
        delegate.trace(msg)
        runHooks(Level.TRACE, msg, null)
    }

    override fun trace(format: String?, arg: Any?) {
        delegate.trace(format, arg)
        runHooks(Level.TRACE, format, null, arg)
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        delegate.trace(format, arg1, arg2)
        runHooks(Level.TRACE, format, null, arg1, arg2)
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        delegate.trace(format, *arguments)
        runHooks(Level.TRACE, format, null, *arguments)
    }

    override fun trace(msg: String?, t: Throwable?) {
        delegate.trace(msg, t)
        runHooks(Level.TRACE, msg, t)
    }

    override fun isTraceEnabled(marker: Marker?): Boolean {
        return delegate.isTraceEnabled(marker)
    }

    override fun trace(marker: Marker?, msg: String?) {
        delegate.trace(marker, msg)
        runHooks(Level.TRACE, msg, null)
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        delegate.trace(marker, format, arg)
        runHooks(Level.TRACE, format, null, arg)
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.trace(marker, format, arg1, arg2)
        runHooks(Level.TRACE, format, null, arg1, arg2)
    }

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
        delegate.trace(marker, format, *argArray)
        runHooks(Level.TRACE, format, null, *argArray)
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.trace(marker, msg, t)
        runHooks(Level.TRACE, msg, t)
    }

    override fun isDebugEnabled(): Boolean {
        return delegate.isDebugEnabled
    }

    override fun debug(msg: String?) {
        delegate.debug(msg)
        runHooks(Level.DEBUG, msg, null)
    }

    override fun debug(format: String?, arg: Any?) {
        delegate.debug(format, arg)
        runHooks(Level.DEBUG, format, null, arg)
    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        delegate.debug(format, arg1, arg2)
        runHooks(Level.DEBUG, format, null, arg1, arg2)
    }

    override fun debug(format: String?, vararg arguments: Any?) {
        delegate.debug(format, *arguments)
        runHooks(Level.DEBUG, format, null, *arguments)
    }

    override fun debug(msg: String?, t: Throwable?) {
        delegate.debug(msg, t)
        runHooks(Level.DEBUG, msg, t)
    }

    override fun isDebugEnabled(marker: Marker?): Boolean {
        return delegate.isDebugEnabled(marker)
    }

    override fun debug(marker: Marker?, msg: String?) {
        delegate.debug(marker, msg)
        runHooks(Level.DEBUG, msg, null)
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        delegate.debug(marker, format, arg)
        runHooks(Level.DEBUG, format, null, arg)
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.debug(marker, format, arg1, arg2)
        runHooks(Level.DEBUG, format, null, arg1, arg2)
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        delegate.debug(marker, format, *arguments)
        runHooks(Level.DEBUG, format, null, *arguments)
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.debug(marker, msg, t)
        runHooks(Level.DEBUG, msg, t)
    }

    override fun isInfoEnabled(): Boolean {
        return delegate.isInfoEnabled
    }

    override fun info(msg: String?) {
        delegate.info(msg)
        runHooks(Level.INFO, msg, null)
    }

    override fun info(format: String?, arg: Any?) {
        delegate.info(format, arg)
        runHooks(Level.INFO, format, null, arg)
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        delegate.info(format, arg1, arg2)
        runHooks(Level.INFO, format, null, arg1, arg2)
    }

    override fun info(format: String?, vararg arguments: Any?) {
        delegate.info(format, *arguments)
        runHooks(Level.INFO, format, null, *arguments)
    }

    override fun info(msg: String?, t: Throwable?) {
        delegate.info(msg, t)
        runHooks(Level.INFO, msg, t)
    }

    override fun isInfoEnabled(marker: Marker?): Boolean {
        return delegate.isInfoEnabled(marker)
    }

    override fun info(marker: Marker?, msg: String?) {
        delegate.info(marker, msg)
        runHooks(Level.INFO, msg, null)
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        delegate.info(marker, format, arg)
        runHooks(Level.INFO, format, null, arg)
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.info(marker, format, arg1, arg2)
        runHooks(Level.INFO, format, null, arg1, arg2)
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        delegate.info(marker, format, *arguments)
        runHooks(Level.INFO, format, null, *arguments)
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.info(marker, msg, t)
        runHooks(Level.INFO, msg, t)
    }

    override fun isWarnEnabled(): Boolean {
        return delegate.isWarnEnabled
    }

    override fun warn(msg: String?) {
        delegate.warn(msg)
        runHooks(Level.WARN, msg, null)
    }

    override fun warn(format: String?, arg: Any?) {
        delegate.warn(format, arg)
        runHooks(Level.WARN, format, null, arg)
    }

    override fun warn(format: String?, vararg arguments: Any?) {
        delegate.warn(format, *arguments)
        runHooks(Level.WARN, format, null, *arguments)
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        delegate.warn(format, arg1, arg2)
        runHooks(Level.WARN, format, null, arg1, arg2)
    }

    override fun warn(msg: String?, t: Throwable?) {
        delegate.warn(msg, t)
        runHooks(Level.WARN, msg, t)
    }

    override fun isWarnEnabled(marker: Marker?): Boolean {
        return delegate.isWarnEnabled(marker)
    }

    override fun warn(marker: Marker?, msg: String?) {
        delegate.warn(marker, msg)
        runHooks(Level.WARN, msg, null)
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        delegate.warn(marker, format, arg)
        runHooks(Level.WARN, format, null, arg)
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.warn(marker, format, arg1, arg2)
        runHooks(Level.WARN, format, null, arg1, arg2)
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        delegate.warn(marker, format, *arguments)
        runHooks(Level.WARN, format, null, *arguments)
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.warn(marker, msg, t)
        runHooks(Level.WARN, msg, t)
    }

    override fun isErrorEnabled(): Boolean {
        return delegate.isErrorEnabled
    }

    override fun error(msg: String?) {
        delegate.error(msg)
        runHooks(Level.ERROR, msg, null)
    }

    override fun error(format: String?, arg: Any?) {
        delegate.error(format, arg)
        runHooks(Level.ERROR, format, null, arg)
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        delegate.error(format, arg1, arg2)
        runHooks(Level.ERROR, format, null, arg1, arg2)
    }

    override fun error(format: String?, vararg arguments: Any?) {
        delegate.error(format, *arguments)
        runHooks(Level.ERROR, format, null, *arguments)
    }

    override fun error(msg: String?, t: Throwable?) {
        delegate.error(msg, t)
        runHooks(Level.ERROR, msg, t)
    }

    override fun isErrorEnabled(marker: Marker?): Boolean {
        return delegate.isErrorEnabled(marker)
    }

    override fun error(marker: Marker?, msg: String?) {
        delegate.error(marker, msg)
        runHooks(Level.ERROR, msg, null)
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        delegate.error(marker, format, arg)
        runHooks(Level.ERROR, format, null, arg)
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        delegate.error(marker, format, arg1, arg2)
        runHooks(Level.ERROR, format, null, arg1, arg2)
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        delegate.error(marker, format, *arguments)
        runHooks(Level.ERROR, format, null, *arguments)
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        delegate.error(marker, msg, t)
        runHooks(Level.ERROR, msg, t)
    }

    override fun toString(): String {
        return "InterceptLogger(delegate=$delegate)"
    }
}
