package poker.cards;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import playingcards.Card;
import playingcards.Card.Rank;
import playingcards.Card.Suit;

class HoleCardsTest {

    @Test
    void testIsPair() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.ACE));
        assertTrue(hole.isPair(), "同じランクならペアであるべき");

        HoleCards notPair = new HoleCards();
        notPair.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        notPair.addCard(Card.valueOf(Suit.HEARTS, Rank.KING));
        assertFalse(notPair.isPair(), "異なるランクならペアではない");
    }

    @Test
    void testIsSuited() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.KING));
        assertTrue(hole.isSuited(), "同じスートならスーテッドであるべき");

        HoleCards notSuited = new HoleCards();
        notSuited.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        notSuited.addCard(Card.valueOf(Suit.HEARTS, Rank.ACE));
        assertFalse(notSuited.isSuited(), "異なるスートならスーテッドではない");
    }

    @Test
    void testIsConnector() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.SIX));
        assertTrue(hole.isConnector(), "連続したランク(5, 6)ならコネクターであるべき");

        // 逆順の追加でも判定できるか
        HoleCards holeRev = new HoleCards();
        holeRev.addCard(Card.valueOf(Suit.HEARTS, Rank.SIX));
        holeRev.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));
        assertTrue(holeRev.isConnector(), "逆順(6, 5)でもコネクターであるべき");

        HoleCards notConnector = new HoleCards();
        notConnector.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));
        notConnector.addCard(Card.valueOf(Suit.HEARTS, Rank.SEVEN));
        assertFalse(notConnector.isConnector(), "連続していないランク(5, 7)はコネクターではない");
        
        // A-K コネクター
        HoleCards ak = new HoleCards();
        ak.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        ak.addCard(Card.valueOf(Suit.HEARTS, Rank.KING));
        assertTrue(ak.isConnector(), "A(14)とK(13)はコネクターであるべき");
    }

    @Test
    void testInvalidSize() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        
        // 1枚しかない場合
        assertFalse(hole.isPair());
        assertFalse(hole.isSuited());
        assertFalse(hole.isConnector());
    }
}