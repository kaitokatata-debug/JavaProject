package playingcards;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import playingcards.Card.Rank;
import playingcards.Card.Suit;

class CardsTest {

    // Cardsは抽象クラスなので、テスト用の具象クラスを作成
    static class TestCards extends Cards {
        public TestCards(int maxSize) {
            super(maxSize, "テスト用カード");
        }

        // protectedメソッドのテスト用ラッパー
        public Card removeCardAtIndex(int index) {
            return super.removeCard(index);
        }
    }

    private TestCards cards;

    @BeforeEach
    void setUp() {
        cards = new TestCards(5);
    }

    @Test
    void testAddCardAndSize() {
        assertTrue(cards.isEmpty());
        assertEquals(0, cards.size());

        cards.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        assertFalse(cards.isEmpty());
        assertEquals(1, cards.size());
    }

    @Test
    void testAddCardMaxSizeExceeded() {
        for (int i = 0; i < 5; i++) {
            cards.addCard(Card.valueOf(Suit.SPADES, Rank.values()[i]));
        }
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            cards.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        });
        assertEquals("テスト用カードは5枚までです。", exception.getMessage());
    }

    @Test
    void testAddCards() {
        List<Card> newCards = new ArrayList<>();
        newCards.add(Card.valueOf(Suit.HEARTS, Rank.TWO));
        newCards.add(Card.valueOf(Suit.HEARTS, Rank.THREE));

        cards.addCards(newCards);
        assertEquals(2, cards.size());
    }

    @Test
    void testAddCardsMaxSizeExceeded() {
        List<Card> newCards = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            newCards.add(Card.valueOf(Suit.HEARTS, Rank.values()[i]));
        }

        assertThrows(IllegalStateException.class, () -> {
            cards.addCards(newCards);
        });
        // 追加失敗時はサイズが変わっていないことを確認
        assertEquals(0, cards.size());
    }

    @Test
    void testClear() {
        cards.addCard(Card.valueOf(Suit.CLUBS, Rank.KING));
        cards.clear();
        assertTrue(cards.isEmpty());
    }

    @Test
    void testContains() {
        Card target = Card.valueOf(Suit.DIAMONDS, Rank.JACK);
        cards.addCard(target);
        
        assertTrue(cards.contains(Card.valueOf(Suit.DIAMONDS, Rank.JACK))); // 同値性チェック
        assertFalse(cards.contains(Card.valueOf(Suit.DIAMONDS, Rank.QUEEN)));
    }

    @Test
    void testSort() {
        // ランク: 2, A, K -> ソート後: A, K, 2 (降順)
        Card c1 = Card.valueOf(Suit.SPADES, Rank.TWO);
        Card c2 = Card.valueOf(Suit.SPADES, Rank.ACE);
        Card c3 = Card.valueOf(Suit.SPADES, Rank.KING);

        cards.addCard(c1);
        cards.addCard(c2);
        cards.addCard(c3);

        cards.sort();
        List<Card> sorted = cards.getCards();

        assertEquals(c2, sorted.get(0)); // A
        assertEquals(c3, sorted.get(1)); // K
        assertEquals(c1, sorted.get(2)); // 2
    }

    @Test
    void testGetCombinations() {
        Card c1 = Card.valueOf(Suit.SPADES, Rank.ACE);
        Card c2 = Card.valueOf(Suit.SPADES, Rank.KING);
        Card c3 = Card.valueOf(Suit.SPADES, Rank.QUEEN);

        cards.addCard(c1);
        cards.addCard(c2);
        cards.addCard(c3);

        // 3枚から2枚選ぶ組み合わせ -> 3通り ({c1,c2}, {c1,c3}, {c2,c3})
        List<List<Card>> combinations = cards.getCombinations(2);
        assertEquals(3, combinations.size());

        // 組み合わせの内容確認
        boolean hasC1C2 = combinations.stream().anyMatch(l -> l.contains(c1) && l.contains(c2));
        assertTrue(hasC1C2);
    }
}