package gg.moonrise.chat.storage;

public record ChatLogPage(int page, int pageSize) {

    public static final int MIN_PAGE = 1;
    public static final int MAX_PAGE = 100_000;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int LOOKAHEAD_PAGE_SIZE = DEFAULT_PAGE_SIZE + 1;

    public ChatLogPage {
        page = Math.clamp(page, MIN_PAGE, MAX_PAGE);
        pageSize = Math.clamp(pageSize, 1, LOOKAHEAD_PAGE_SIZE);
    }

    public int offset() {
        return (page - 1) * visiblePageSize();
    }

    public int visiblePageSize() {
        return Math.max(1, pageSize - 1);
    }
}
