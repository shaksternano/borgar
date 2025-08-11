package com.shakster.borgar.core.logging

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level
import kotlin.test.assertEquals

class InterceptLoggerTest {

    @Test
    fun testInterceptInfo() {
        val testLogger = InterceptLogger(MockLogger)
        val hook = TestLoggerHook()
        testLogger.addHook(hook)

        val message = "Test info message"
        testLogger.info(message)

        assertEquals(1, hook.interceptedMessages.size)
        val intercepted = hook.interceptedMessages.first()
        assertEquals(Level.INFO, intercepted.level)
        assertEquals(message, intercepted.message)
        assertEquals(null, intercepted.throwable)
        assertEquals(emptyList(), intercepted.arguments)
    }

    @Test
    fun testInterceptError() {
        val testLogger = InterceptLogger(MockLogger)
        val hook = TestLoggerHook()
        testLogger.addHook(hook)

        val message = "Test error message"
        val throwable = RuntimeException("Test exception")
        testLogger.error(message, throwable)

        assertEquals(1, hook.interceptedMessages.size)
        val intercepted = hook.interceptedMessages.first()
        assertEquals(Level.ERROR, intercepted.level)
        assertEquals(message, intercepted.message)
        assertEquals(throwable, intercepted.throwable)
        assertEquals(emptyList(), intercepted.arguments)
    }

    @Test
    fun testInterceptWithArguments() {
        val testLogger = InterceptLogger(MockLogger)
        val hook = TestLoggerHook()
        testLogger.addHook(hook)

        val format = "Test message with {} and {}"
        val arg1 = "arg1"
        val arg2 = 42
        testLogger.info(format, arg1, arg2)

        assertEquals(1, hook.interceptedMessages.size)
        val intercepted = hook.interceptedMessages.first()
        assertEquals(Level.INFO, intercepted.level)
        assertEquals(format, intercepted.message)
        assertEquals(null, intercepted.throwable)
        assertEquals(listOf(arg1, arg2), intercepted.arguments)
    }

    @Test
    fun testInterceptWithMarker() {
        val testLogger = InterceptLogger(MockLogger)
        val hook = TestLoggerHook()
        testLogger.addHook(hook)
        val marker = object : Marker {

            override fun getName(): String = "TEST_MARKER"

            override fun add(reference: Marker?): Unit = throw UnsupportedOperationException()

            override fun remove(reference: Marker?): Boolean = throw UnsupportedOperationException()

            @Suppress("OVERRIDE_DEPRECATION")
            override fun hasChildren(): Boolean = throw UnsupportedOperationException()

            override fun hasReferences(): Boolean = throw UnsupportedOperationException()

            override fun iterator(): MutableIterator<Marker> = throw UnsupportedOperationException()

            override fun contains(other: Marker?): Boolean = throw UnsupportedOperationException()

            override fun contains(name: String?): Boolean = throw UnsupportedOperationException()
        }

        val message = "Test message with marker"
        testLogger.info(marker, message)

        assertEquals(1, hook.interceptedMessages.size)
        val intercepted = hook.interceptedMessages.first()
        assertEquals(Level.INFO, intercepted.level)
        assertEquals(message, intercepted.message)
        assertEquals(null, intercepted.throwable)
        assertEquals(emptyList(), intercepted.arguments)
    }
}

private class TestLoggerHook : LoggerHook {

    val interceptedMessages: MutableList<InterceptedMessage> = mutableListOf()

    override fun onLog(
        level: Level,
        message: String?,
        t: Throwable?,
        vararg arguments: Any?,
    ) {
        interceptedMessages.add(InterceptedMessage(level, message, t, arguments.toList()))
    }
}

data class InterceptedMessage(
    val level: Level,
    val message: String?,
    val throwable: Throwable?,
    val arguments: List<Any?>,
)

private object MockLogger : Logger {

    override fun getName(): String = "MockLogger"

    override fun isTraceEnabled(): Boolean = true

    override fun isTraceEnabled(marker: Marker?): Boolean = true

    override fun trace(msg: String?) {}

    override fun trace(format: String?, arg: Any?) {}

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {}

    override fun trace(format: String?, vararg arguments: Any?) {}

    override fun trace(msg: String?, t: Throwable?) {}

    override fun trace(marker: Marker?, msg: String?) {}

    override fun trace(marker: Marker?, format: String?, arg: Any?) {}

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {}

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {}

    override fun isDebugEnabled(): Boolean = true

    override fun isDebugEnabled(marker: Marker?): Boolean = true

    override fun debug(msg: String?) {}

    override fun debug(format: String?, arg: Any?) {}

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {}

    override fun debug(format: String?, vararg arguments: Any?) {}

    override fun debug(msg: String?, t: Throwable?) {}

    override fun debug(marker: Marker?, msg: String?) {}

    override fun debug(marker: Marker?, format: String?, arg: Any?) {}

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {}

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {}

    override fun isInfoEnabled(): Boolean = true

    override fun isInfoEnabled(marker: Marker?): Boolean = true

    override fun info(msg: String?) {}

    override fun info(format: String?, arg: Any?) {}

    override fun info(format: String?, arg1: Any?, arg2: Any?) {}

    override fun info(format: String?, vararg arguments: Any?) {}

    override fun info(msg: String?, t: Throwable?) {}

    override fun info(marker: Marker?, msg: String?) {}

    override fun info(marker: Marker?, format: String?, arg: Any?) {}

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {}

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {}

    override fun isWarnEnabled(): Boolean = true

    override fun isWarnEnabled(marker: Marker?): Boolean = true

    override fun warn(msg: String?) {}

    override fun warn(format: String?, arg: Any?) {}

    override fun warn(format: String?, vararg arguments: Any?) {}

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {}

    override fun warn(msg: String?, t: Throwable?) {}

    override fun warn(marker: Marker?, msg: String?) {}

    override fun warn(marker: Marker?, format: String?, arg: Any?) {}

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {}

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {}

    override fun isErrorEnabled(): Boolean = true

    override fun isErrorEnabled(marker: Marker?): Boolean = true

    override fun error(msg: String?) {}

    override fun error(format: String?, arg: Any?) {}

    override fun error(format: String?, arg1: Any?, arg2: Any?) {}

    override fun error(format: String?, vararg arguments: Any?) {}

    override fun error(msg: String?, t: Throwable?) {}

    override fun error(marker: Marker?, msg: String?) {}

    override fun error(marker: Marker?, format: String?, arg: Any?) {}

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {}

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {}

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {}
}
