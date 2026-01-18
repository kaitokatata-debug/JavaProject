package playingcards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeckTest {

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    @Test
    void testInitialize() {
        // 初期状態で52枚あること
        assertEquals(52, deck.size());
        assertFalse(deck.isEmpty());

        // 全てのカードがユニークであること（重複がないこと）を確認
        // Deckから全てのカードを引いてSetに入れる
        Set<Card> uniqueCards = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            uniqueCards.add(deck.draw());
        }
        assertEquals(52, uniqueCards.size());
        assertTrue(deck.isEmpty());
    }

    @Test
    void testDraw() {
        int initialSize = deck.size();
        Card card = deck.draw();

        assertNotNull(card);
        assertEquals(initialSize - 1, deck.size());
    }

    @Test
    void testDrawEmpty() {
        // 全て引く
        while (!deck.isEmpty()) {
            deck.draw();
        }

        // 空の状態で引くと例外が発生することを確認
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            deck.draw();
        });
        assertEquals("デッキが空です", exception.getMessage());
    }

    @Test
    void testReInitialize() {
        // 何枚か引く
        deck.draw();
        deck.draw();
        assertNotEquals(52, deck.size());

        // 再初期化で52枚に戻ることを確認
        deck.initialize();
        assertEquals(52, deck.size());
    }
}
