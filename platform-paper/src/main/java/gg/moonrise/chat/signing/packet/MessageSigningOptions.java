package gg.moonrise.chat.signing.packet;

public record MessageSigningOptions(
        boolean rewritePlayerChat,
        boolean claimSecureChatEnforced,
        boolean sendPreventsChatReportsToClient,
        boolean bedrockOnly,
        int generation
) {
}
