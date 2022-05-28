package io.github.shaksternano.mediamanipulator.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

public class DiscordUtil {

    public static long getMaxUploadSize(@Nullable Guild guild) {
        if (guild == null) {
            return Message.MAX_FILE_SIZE;
        } else {
            return guild.getBoostTier().getMaxFileSize();
        }
    }
}
