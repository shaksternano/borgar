package io.github.shaksternano.borgar.chat.command

const val COMMAND_PREFIX: String = "%"
const val ARGUMENT_PREFIX: String = "-"
const val ENTITY_ID_SEPARATOR: String = ":"

val COMMANDS: Map<String, Command> = registerCommands(
    HelpCommand,
    CaptionCommand.Caption,
    CaptionCommand.Caption2,
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
)

private fun registerCommands(vararg commands: Command): Map<String, Command> = buildMap {
    commands.forEach {
        if (containsKey(it.name)) throw IllegalArgumentException(
            "Command with name ${it.name} already exists. Existing command: ${get(it.name)}. New command: $it"
        )
        put(it.name, it)
    }
}
