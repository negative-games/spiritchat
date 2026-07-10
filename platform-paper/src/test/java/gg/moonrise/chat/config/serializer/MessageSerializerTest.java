package gg.moonrise.chat.config.serializer;

import gg.moonrise.engine.message.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageSerializerTest {

    @Test
    void serializesMessageContent() {
        MessageSerializer serializer = new MessageSerializer();

        assertEquals("<green>Hello", serializer.serialize(Message.of("<green>Hello")));
    }

    @Test
    void deserializesMessageContent() {
        MessageSerializer serializer = new MessageSerializer();

        assertEquals("<red>Stop", serializer.deserialize("<red>Stop").content());
    }

    @Test
    void toleratesNullMessageValues() {
        MessageSerializer serializer = new MessageSerializer();

        assertEquals("", serializer.serialize(null));
        assertEquals("", serializer.deserialize(null).content());
    }
}
