package io.github.shaksternano.mediamanipulator.util;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class MessageUtil {

    public List<Message.Attachment> getAttachments(Message message) {
        List<Message.Attachment> attachments = message.getAttachments();
        if (!attachments.isEmpty()) {
            return attachments;
        } else {
            return null;
        }
    }
}
