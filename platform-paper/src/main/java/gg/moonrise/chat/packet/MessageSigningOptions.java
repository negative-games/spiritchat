package gg.moonrise.chat.packet;

public record MessageSigningOptions(
        boolean rewritePlayerChat,
        boolean claimSecureChatEnforced,
        boolean sendPreventsChatReportsToClient,
        boolean bedrockOnly,
        int generation
) {
}
