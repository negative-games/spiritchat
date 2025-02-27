package games.negative.spiritchat.config.serializer;

import de.exlll.configlib.Serializer;
import games.negative.alumina.message.Message;

import java.util.List;

public class MessageSerializer implements Serializer<Message, List<String>> {
    @Override
    public List<String> serialize(Message message) {
        return message.contentList();
    }

    @Override
    public Message deserialize(List<String> strings) {
        return new Message(strings.toArray(new String[0]));
    }
}
