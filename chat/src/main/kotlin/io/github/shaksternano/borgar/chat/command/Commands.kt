package io.github.shaksternano.borgar.chat.command

const val COMMAND_PREFIX: String = "%"
const val ARGUMENT_PREFIX: String = "--"
const val ENTITY_ID_SEPARATOR: String = ":"

val COMMANDS: Map<String, Command> = registerCommands(
    HelpCommand,
    CaptionCommand.Caption,
    CaptionCommand.Caption2,
    MemeCommand,
    DemotivateCommand,
    SpeechBubbleCommand,
    CutoutSpeechBubbleCommand,
    UncaptionCommand.Uncaption,
    UncaptionCommand.Uncaption2,
    AutoCropCommand,
    CropCommand,
    RotateCommand,
    FlipCommand,
    PixelateCommand,
    StretchCommand,
    ResizeCommand,
    SpinCommand,
    SpeedCommand,
    ReverseCommand,
    ReduceFpsCommand,
    TemplateCommand.SONIC_SAYS,
    TemplateCommand.SOYJAK_POINTING,
    TemplateCommand.MUTA_SOY,
    TemplateCommand.WALMART_WANTED,
    TemplateCommand.OH_MY_GOODNESS_GRACIOUS,
    TemplateCommand.THINKING_BUBBLE,
    TemplateCommand.LIVING_IN_1984,
    TemplateCommand.WHO_DID_THIS,
    LiveReactionCommand,
    SubwaySurfersCommand,
    TranscodeCommand.PNG,
    TranscodeCommand.JPG,
    TranscodeCommand.GIF,
    TranscodeCommand.MP4,
    TranscodeCommand.ICO,
    ChangeExtensionCommand.GIF,
    LoopCommand,
    EmojiImageCommand,
    StickerImageCommand,
    TenorUrlCommand,
    UrlFileCommand.HAEMA,
    UrlFileCommand.TULIN,
    UrlFileCommand.SURE_BUD,
    UserAvatarCommand,
    UserBannerCommand,
    ServerIconCommand,
    ServerBannerCommand,
    ServerSplashCommand,
    DownloadCommand,
    CatCommand.CAT,
    CatCommand.CAT_BOMB,
    PonyCommand.PONY,
    PonyCommand.PONY_BOMB,
    FavouriteCommand,
    CreateTemplateCommand,
    DeleteTemplateCommand,
    PingCommand,
    ServerCountCommand,
    ShutdownCommand,
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
