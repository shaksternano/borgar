package io.github.shaksternano.mediamanipulator.util;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

public class DiscordUtil {

    /**
     * The maximum file size that can be sent in a Discord message, 8MB.
     */
    private static final long DISCORD_MAXIMUM_FILE_SIZE = 8388608;

    public static long getMaxUploadSize(@Nullable Guild guild) {
        if (guild == null) {
            return DISCORD_MAXIMUM_FILE_SIZE;
        } else {
            return guild.getBoostTier().getMaxFileSize();
        }
    }
}
