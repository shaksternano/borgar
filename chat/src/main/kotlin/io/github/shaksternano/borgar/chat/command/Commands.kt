package io.github.shaksternano.borgar.chat.command

const val COMMAND_PREFIX: String = "%"
const val ARGUMENT_PREFIX: String = "-"
const val ENTITY_ID_SEPARATOR: String = ":"

val COMMANDS: Map<String, Command> = registerCommands(
    HelpCommand,
    CaptionCommand.Caption,
    CaptionCommand.Caption2,
    MemeCommand,
    DemotivateCommand,
    AutoCropCommand,
    CropCommand,
    FlipCommand,
    TranscodeCommand.PNG,
    TranscodeCommand.JPG,
    TranscodeCommand.GIF,
    TranscodeCommand.MP4,
    TranscodeCommand.ICO,
    ChangeExtensionCommand.GIF,
    UrlFileCommand.HAEMA,
    UrlFileCommand.TULIN,
    UrlFileCommand.SURE_BUD,
    DownloadCommand,
    CatCommand.CAT,
    CatCommand.CAT_BOMB,
    PonyCommand.PONY,
    PonyCommand.PONY_BOMB,
    PingCommand,
)

val COMMANDS_AND_ALIASES: Map<String, Command> = buildMap {
    putAll(COMMANDS)
    COMMANDS.values.forEach { command ->
        command.aliases.forEach { alias ->
            if (alias in this) throw IllegalArgumentException(
                "A command with the name or alias $alias already exists. Existing command: ${this[alias]}. New command: $command"
            )
            this[alias] = command
        }
    }
}

private fun registerCommands(vararg commands: Command): Map<String, Command> = buildMap {
    commands.forEach {
        if (it.name in this) throw IllegalArgumentException(
            "A command with the name ${it.name} already exists. Existing command: ${this[it.name]}. New command: $it"
        )
        this[it.name] = it
    }
}
