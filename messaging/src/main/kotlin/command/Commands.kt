package com.shakster.borgar.messaging.command

import com.shakster.borgar.core.util.Named
import com.shakster.borgar.core.util.startsWithVowel

const val ARGUMENT_PREFIX: String = "--"
const val ENTITY_ID_SEPARATOR: String = ":"

val COMMANDS: Map<String, Command> = registerCommands(
    "command",
    HelpCommand,
    CaptionCommand.Caption,
    CaptionCommand.Caption2,
    MemeCommand,
    DemotivateCommand,
    SpeechBubbleCommand,
    CutoutSpeechBubbleCommand,
    UncaptionCommand,
    AutoCropCommand,
    CropCommand,
    RotateCommand,
    FlipCommand,
    InvertColorsCommand,
    PixelateCommand,
    StretchCommand,
    ResizeCommand,
    SpinCommand,
    SpeedCommand,
    ReverseCommand,
    ReduceFpsCommand,
    TemplateCommand.SONIC_SAYS,
    TemplateCommand.SOYJAK_POINTING,
    TemplateCommand.WALMART_WANTED,
    TemplateCommand.OH_MY_GOODNESS_GRACIOUS,
    TemplateCommand.THINKING_BUBBLE,
    TemplateCommand.LIVING_IN_1984,
    TemplateCommand.WHO_DID_THIS,
    LiveReactionCommand,
    SubwaySurfersCommand,
    TranscodeCommand.PNG,
    TranscodeCommand.JPG,
    TranscodeCommand.MP4,
    TranscodeCommand.ICO,
    GifCommand,
    LoopCommand,
    EmojiImageCommand,
    StickerImageCommand,
    TenorUrlCommand,
    UrlFileCommand.TULIN,
    UserAvatarCommand,
    UserBannerCommand,
    GuildIconCommand,
    GuildBannerCommand,
    GuildSplashCommand,
    DownloadCommand,
    CatCommand.CAT,
    CatCommand.CAT_BOMB,
    DerpibooruCommand.DERPIBOORU,
    DerpibooruCommand.DERPIBOORU_BOMB,
    FavouriteCommand,
    CreateTemplateCommand,
    DeleteTemplateCommand,
    PingCommand,
    GuildCountCommand,
    UptimeCommand,
    ShutdownCommand,
    BanCommand,
    UnbanCommand,
    BanListCommand,
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

fun <T : Named> registerCommands(commandType: String, vararg commands: T): Map<String, T> = buildMap {
    commands.forEach {
        val commandName = it.name.lowercase()
        if (commandName in this) {
            var errorMessage = "A"
            if (commandType.startsWithVowel()) {
                errorMessage += "n"
            }
            errorMessage += " $commandType with the name $commandName already exists. Existing command: ${this[commandName]}. New command: $it"
            throw IllegalArgumentException(errorMessage)
        }
        this[commandName] = it
    }
}
