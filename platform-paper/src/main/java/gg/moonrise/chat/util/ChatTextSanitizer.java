package gg.moonrise.chat.util;

import gg.moonrise.engine.message.util.MiniMessageUtil;
import lombok.experimental.UtilityClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@UtilityClass
public class ChatTextSanitizer {

    private final Pattern FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("(?i)(?<!\\\\)</?(?:message|player|item|i|mention)>");

    public String stripTagsPreservingPlaceholders(String input, List<String> preservedPlaceholders) {
        return transformPreservingPlaceholders(
                input,
                preservedPlaceholders,
                "PLACEHOLDER",
                MiniMessageUtil.INSTANCE::stripTags
        );
    }

    public String sanitizePlainUserMessage(String input, List<String> preservedPlaceholders) {
        return transformPreservingPlaceholders(
                stripTagsPreservingPlaceholders(input, preservedPlaceholders),
                preservedPlaceholders,
                "RESERVED",
                ChatTextSanitizer::escapeUserFormatPlaceholders
        );
    }

    public String escapeUserFormatPlaceholders(String input) {
        return FORMAT_PLACEHOLDER_PATTERN.matcher(input == null ? "" : input).replaceAll("\\\\$0");
    }

    private String transformPreservingPlaceholders(String input, List<String> preservedPlaceholders, String purpose, java.util.function.UnaryOperator<String> transformer) {
        Map<String, String> tokens = new LinkedHashMap<>();
        String protectedInput = input == null ? "" : input;
        int index = 0;

        for (String placeholder : safePlaceholders(preservedPlaceholders)) {
            if (placeholder == null || placeholder.isEmpty() || !protectedInput.contains(placeholder)) continue;

            String token = uniqueToken(purpose, index++, protectedInput);
            protectedInput = protectedInput.replace(placeholder, token);
            tokens.put(token, placeholder);
        }

        String transformed = transformer.apply(protectedInput);
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            transformed = transformed.replace(entry.getKey(), entry.getValue());
        }
        return transformed;
    }

    private String uniqueToken(String purpose, int index, String input) {
        int attempt = 0;
        String token = token(purpose, index, attempt);
        while (input.contains(token)) {
            token = token(purpose, index, ++attempt);
        }
        return token;
    }

    private String token(String purpose, int index, int attempt) {
        return "\uE000SPIRITCHAT_" + purpose + "_" + index + "_" + attempt + "\uE001";
    }

    private List<String> safePlaceholders(List<String> preservedPlaceholders) {
        return preservedPlaceholders == null ? List.of() : preservedPlaceholders;
    }
}
