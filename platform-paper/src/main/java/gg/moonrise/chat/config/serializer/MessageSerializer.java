package gg.moonrise.chat.config.serializer;

import de.exlll.configlib.Serializer;
import gg.moonrise.engine.message.Message;

public class MessageSerializer implements Serializer<Message, String> {

    @Override
    public String serialize(Message message) {
        if (message == null) return "";

        return message.content();
    }

    @Override
    public Message deserialize(String input) {
        return Message.of(input == null ? "" : input);
    }
}
