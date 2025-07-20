# Borgar

Discord and Revolt bot that provides fun image editing commands, such as image captioning. View the list of commands with `/help`.

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
   <!--suppress CheckImageSize -->
   <img src="images/image_caption_example.png" alt="Image captioning" width=400/>
</div>

## Prerequisites

- Java 21 SDK or higher
- Discord or Revolt Bot Token Bot Token
   - You can get a Discord token from the [Discord Developer Portal](https://discord.com/developers/applications) or a
     Revolt token from the [Revolt Bot Menu](https://app.revolt.chat/settings/bots).

## Running the program

1. Run `./gradlew run`
   - If using IntelliJ IDEA, you can also run `app/src/main/kotlin/Main.kt`
2. Running the program for the first time should create a `config.json` file and then exit.
3. Add your Discord or Revolt bot token to the `config.json` file and run the program again.

## Exporting a JAR File

1. Run `./gradlew build`.
2. The JAR file will be located in `build/libs/`.
3. The JAR file can be run with `java -jar borgar-X.X.X.jar`.

## Available Commands

Some of the fun commands provided by Borgar:

- `%caption`: add captions to an image.
- `%rotate`: rotates an image.
- `%speed`: speeds up or slows down a GIF or video.

Run `%help` to see a full list of commands.

Commands can also be chained together. The following example captions an image and then rotates it by 90 degrees
clockwise:
```
%caption A caption %rotate 90
```
