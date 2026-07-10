package gg.moonrise.chat.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatLogPageTest {

    @Test
    void firstPageStartsAtZeroOffset() {
        ChatLogPage page = new ChatLogPage(1, 11);

        assertEquals(0, page.offset());
        assertEquals(10, page.visiblePageSize());
    }

    @Test
    void laterPagesOffsetByVisiblePageSizeNotLookaheadLimit() {
        ChatLogPage page = new ChatLogPage(3, 11);

        assertEquals(20, page.offset());
    }

    @Test
    void clampsInvalidPageAndPageSize() {
        ChatLogPage page = new ChatLogPage(-5, 500);

        assertEquals(ChatLogPage.MIN_PAGE, page.page());
        assertEquals(ChatLogPage.LOOKAHEAD_PAGE_SIZE, page.pageSize());
    }
}
