package games.negative.chat.config.section.chat;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

@Getter
@Configuration
public class ChatItemSettings {

    @Comment({
            "Whether or not this chat item is enabled.",
            "If disabled, this chat item will not be used in chat formatting.",
            " ",
            "Default: true"
    })
    private boolean enabled = true;

    @Comment({
            "",
            "Placeholders that will be replaced with the item name in chat messages.",
            "You can add multiple placeholders if you want.",
            " ",
            "Default: [\"{i}\", \"{item}\", \"[i]\", \"[item]\"]"
    })
    private List<String> placeholders = List.of("{i}", "{item}", "[i]", "[item]");


    @Comment({
            "",
            "A list of item types that will not be displayed in chat messages.",
            "If a player is holding an item in this list, it will be ignored when formatting chat messages.",
            " ",
            "Default: [AIR]"
    })
    private List<Material> blockedItemTypes = List.of(
            Material.AIR
    );

    public boolean isItemTypeBlocked(Material material) {
        return blockedItemTypes.contains(material);
    }

    public boolean containsChatItemSyntax(String input) {
        for (String placeholder : placeholders) {
            if (input.contains(placeholder)) {
                return true;
            }
        }
        return false;
    }
}
