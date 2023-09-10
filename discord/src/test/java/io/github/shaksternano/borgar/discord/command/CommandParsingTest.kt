package io.github.shaksternano.borgar.discord.command

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandParsingTest {

    @Test
    fun canParseCommandString() {
        val command = "${COMMAND_PREFIX}caption"
        val commandStrings = parseCommandStrings(command)
        assertEquals(1, commandStrings.size)
        assertEquals(command, commandStrings[0])
    }

    @Test
    fun canParseCommandStringWithArguments() {
        val command = "${COMMAND_PREFIX}caption Test"
        val commandStrings = parseCommandStrings(command)
        assertEquals(1, commandStrings.size)
        assertEquals(command, commandStrings[0])
    }

    @Test
    fun canParseCommandStringWithArgumentsAndCommandPrefix() {
        val command = "${COMMAND_PREFIX}caption Test$COMMAND_PREFIX"
        val commandStrings = parseCommandStrings(command)
        assertEquals(1, commandStrings.size)
        assertEquals(command, commandStrings[0])
    }

    @Test
    fun canParseMultipleCommandStrings() {
        val command1 = "${COMMAND_PREFIX}caption"
        val command2 = "${COMMAND_PREFIX}spin"
        val command3 = "${COMMAND_PREFIX}gif"
        val chained = "$command1 $command2 $command3"
        val commandStrings = parseCommandStrings(chained)
        assertEquals(3, commandStrings.size)
        assertEquals(command1, commandStrings[0])
        assertEquals(command2, commandStrings[1])
        assertEquals(command3, commandStrings[2])
    }

    @Test
    fun canParseMultipleCommandStringsWithArguments() {
        val command1 = "${COMMAND_PREFIX}caption Test"
        val command2 = "${COMMAND_PREFIX}spin 2"
        val command3 = "${COMMAND_PREFIX}gif"
        val chained = "$command1 $command2 $command3"
        val commandStrings = parseCommandStrings(chained)
        assertEquals(3, commandStrings.size)
        assertEquals(command1, commandStrings[0])
        assertEquals(command2, commandStrings[1])
        assertEquals(command3, commandStrings[2])
    }

    @Test
    fun canParseMultipleCommandStringsWithLargeSpaces() {
        val command1 = "${COMMAND_PREFIX}caption Test   Test2"
        val command2 = "${COMMAND_PREFIX}spin 2"
        val command3 = "${COMMAND_PREFIX}gif"
        val chained = "$command1   $command2   $command3"
        val commandStrings = parseCommandStrings(chained)
        assertEquals(3, commandStrings.size)
        assertEquals(command1, commandStrings[0])
        assertEquals(command2, commandStrings[1])
        assertEquals(command3, commandStrings[2])
    }

    @Test
    fun canParseCommandStringWithEntityId() {
        val command = "${COMMAND_PREFIX}caption:964550080000565379"
        val commandStrings = parseCommandStrings(command)
        assertEquals(1, commandStrings.size)
        assertEquals(command, commandStrings[0])
    }

    @Test
    fun onlyPrefixIsNotValid() {
        val command = COMMAND_PREFIX
        val commandStrings = parseCommandStrings(command)
        assertEquals(0, commandStrings.size)
    }

    @Test
    fun noPrefixStartIsNotValid() {
        val command = "a${COMMAND_PREFIX}caption"
        val commandStrings = parseCommandStrings(command)
        assertEquals(0, commandStrings.size)
    }

    @Test
    fun canParseCommandWithNamedArguments() {
        val commandName = "caption"
        val arguments = "Test"
        val argumentName1 = "named"
        val argumentValue1 = "Test2"
        val argumentName2 = "named2"
        val argumentValue2 = "Test3"
        val command =
            "${COMMAND_PREFIX}$commandName $arguments $NAMED_ARGUMENT_PREFIX$argumentName1 $argumentValue1 $NAMED_ARGUMENT_PREFIX$argumentName2 $argumentValue2"
        val rawCommands = parseRawCommands(command)
        assertEquals(1, rawCommands.size)
        val rawCommand = rawCommands[0]
        assertEquals(commandName, rawCommand.command)
        assertEquals(arguments, rawCommand.arguments)
        assertEquals(2, rawCommand.namedArguments.size)
        assertEquals(argumentValue1, rawCommand.namedArguments[argumentName1])
        assertEquals(argumentValue2, rawCommand.namedArguments[argumentName2])
    }

    @Test
    fun canParseMultipleCommandsWithNamedArguments() {
        val command1Name = "caption"
        val arguments1 = "Test"
        val argument1Name1 = "named"
        val argument1Value1 = "Test2"
        val argument1Name2 = "named2"
        val argument1Value2 = "Test3"
        val command1 =
            "${COMMAND_PREFIX}$command1Name $arguments1 $NAMED_ARGUMENT_PREFIX$argument1Name1 $argument1Value1 $NAMED_ARGUMENT_PREFIX$argument1Name2 $argument1Value2"

        val command2Name = "spin"
        val arguments2 = "2"
        val command2 = "${COMMAND_PREFIX}$command2Name $arguments2"

        val command3Name = "gif"
        val argument3Name = "named"
        val argument3Value = "Test"
        val command3 = "${COMMAND_PREFIX}$command3Name $NAMED_ARGUMENT_PREFIX$argument3Name $argument3Value"

        val chained = "$command1 $command2 $command3"
        val rawCommands = parseRawCommands(chained)
        assertEquals(3, rawCommands.size)

        val rawCommand1 = rawCommands[0]
        assertEquals(command1Name, rawCommand1.command)
        assertEquals(arguments1, rawCommand1.arguments)
        assertEquals(2, rawCommand1.namedArguments.size)
        assertEquals(argument1Value1, rawCommand1.namedArguments[argument1Name1])
        assertEquals(argument1Value2, rawCommand1.namedArguments[argument1Name2])

        val rawCommand2 = rawCommands[1]
        assertEquals(command2Name, rawCommand2.command)
        assertEquals(arguments2, rawCommand2.arguments)
        assertEquals(0, rawCommand2.namedArguments.size)

        val rawCommand3 = rawCommands[2]
        assertEquals(command3Name, rawCommand3.command)
        assertEquals("", rawCommand3.arguments)
        assertEquals(1, rawCommand3.namedArguments.size)
        assertEquals(argument3Value, rawCommand3.namedArguments[argument3Name])
    }
}
