package poker.cards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import playingcards.Card;
import playingcards.Card.Rank;
import playingcards.Card.Suit;

class HandEvaluatorTest {

    @Test
    void testRoyalFlush() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.KING));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.QUEEN));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.JACK));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.TEN));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO)); // 関係ないカード
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE)); // 関係ないカード

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.ROYAL_FLUSH, hand.getRank());
    }

    @Test
    void testStraightFlush() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.EIGHT));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.SEVEN));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.SIX));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.KING));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.STRAIGHT_FLUSH, hand.getRank());
    }

    @Test
    void testFourOfAKind() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.ACE));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.ACE));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.ACE));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.KING));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.FOUR_OF_A_KIND, hand.getRank());
    }

    @Test
    void testFullHouse() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.KING));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.KING));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.QUEEN));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.QUEEN));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.FULL_HOUSE, hand.getRank());
    }

    @Test
    void testFlush() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.JACK));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.KING));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.QUEEN));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.FLUSH, hand.getRank());
    }

    @Test
    void testStraight() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.SIX));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.SEVEN));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.EIGHT));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.ACE));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.STRAIGHT, hand.getRank());
    }

    @Test
    void testWheelStraight() {
        // A, 2, 3, 4, 5 のストレート（通称ホイール）
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.FOUR));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.STRAIGHT, hand.getRank());
    }

    @Test
    void testKicker() {
        // プレイヤー1: Aのワンペア、キッカー K
        HoleCards hole1 = new HoleCards();
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.KING));

        // プレイヤー2: Aのワンペア、キッカー Q
        HoleCards hole2 = new HoleCards();
        hole2.addCard(Card.valueOf(Suit.HEARTS, Rank.ACE));
        hole2.addCard(Card.valueOf(Suit.HEARTS, Rank.QUEEN));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.ACE)); // 共通のA（ペア成立）
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.JACK));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));

        Hand hand1 = HandEvaluator.evaluate(hole1, comm);
        Hand hand2 = HandEvaluator.evaluate(hole2, comm);

        assertEquals(HandRank.ONE_PAIR, hand1.getRank());
        assertEquals(HandRank.ONE_PAIR, hand2.getRank());
        
        // hand1 (Kicker K) > hand2 (Kicker Q) なので compareTo は正の値を返すはず
        assertTrue(hand1.compareTo(hand2) > 0, "キッカーKの方がQより強いはずです");
    }

    @Test
    void testSplitPot() {
        // プレイヤー1: A, 2 (キッカーAが採用される)
        HoleCards hole1 = new HoleCards();
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.TWO));

        // プレイヤー2: A, 3 (キッカーAが採用される)
        HoleCards hole2 = new HoleCards();
        hole2.addCard(Card.valueOf(Suit.HEARTS, Rank.ACE));
        hole2.addCard(Card.valueOf(Suit.HEARTS, Rank.THREE));

        // コミュニティカード: K, K, Q, Q, 10
        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.KING));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.QUEEN));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.QUEEN));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.TEN));

        Hand hand1 = HandEvaluator.evaluate(hole1, comm);
        Hand hand2 = HandEvaluator.evaluate(hole2, comm);

        // 両者とも Two Pair (K, K, Q, Q) with Kicker A (10よりAが強いため採用)
        // 完全に同じ構成なので引き分け (0) になるはず
        assertEquals(0, hand1.compareTo(hand2), "完全に同じ強さのハンドなので引き分けになるはずです");
    }

    @Test
    void testThreeOfAKind() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.EIGHT));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.EIGHT));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.EIGHT));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.JACK));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.THREE_OF_A_KIND, hand.getRank());
    }

    @Test
    void testTwoPair() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.EIGHT));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.EIGHT));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.JACK));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.TWO_PAIR, hand.getRank());
    }

    @Test
    void testOnePair() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.EIGHT));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.EIGHT));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.JACK));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.ONE_PAIR, hand.getRank());
    }

    @Test
    void testHighCard() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.TWO));
        hole.addCard(Card.valueOf(Suit.HEARTS, Rank.SEVEN));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.FOUR));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.JACK));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.KING));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.ACE));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.HIGH_CARD, hand.getRank());
    }

    @Test
    void testFlushVsFlush() {
        // Player 1: A high flush
        HoleCards hole1 = new HoleCards();
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.FOUR));

        // Player 2: K high flush
        HoleCards hole2 = new HoleCards();
        hole2.addCard(Card.valueOf(Suit.SPADES, Rank.KING));
        hole2.addCard(Card.valueOf(Suit.SPADES, Rank.QUEEN));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.TEN));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.EIGHT));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));

        Hand hand1 = HandEvaluator.evaluate(hole1, comm);
        Hand hand2 = HandEvaluator.evaluate(hole2, comm);

        assertEquals(HandRank.FLUSH, hand1.getRank());
        assertEquals(HandRank.FLUSH, hand2.getRank());
        assertTrue(hand1.compareTo(hand2) > 0, "A-high flush should beat K-high flush");
    }

    @Test
    void testFullHouseVsFullHouse() {
        // Player 1: KKK 22
        HoleCards hole1 = new HoleCards();
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.KING));
        hole1.addCard(Card.valueOf(Suit.HEARTS, Rank.KING));

        // Player 2: QQQ 22
        HoleCards hole2 = new HoleCards();
        hole2.addCard(Card.valueOf(Suit.SPADES, Rank.QUEEN));
        hole2.addCard(Card.valueOf(Suit.HEARTS, Rank.QUEEN));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.QUEEN));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.FIVE));

        Hand hand1 = HandEvaluator.evaluate(hole1, comm);
        Hand hand2 = HandEvaluator.evaluate(hole2, comm);

        assertEquals(HandRank.FULL_HOUSE, hand1.getRank());
        assertEquals(HandRank.FULL_HOUSE, hand2.getRank());
        assertTrue(hand1.compareTo(hand2) > 0, "Higher three-of-a-kind in Full House should win");
    }

    @Test
    void testWheelStraightFlush() {
        HoleCards hole = new HoleCards();
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole.addCard(Card.valueOf(Suit.SPADES, Rank.TWO));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.THREE));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.FOUR));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.STRAIGHT_FLUSH, hand.getRank());
    }

    @Test
    void testWheelStraightVsHigherStraight() {
        // Hand 1: Wheel Straight (5-high)
        HoleCards hole1 = new HoleCards();
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        hole1.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        
        // Hand 2: 6-high Straight (2,3,4,5,6)
        HoleCards hole2 = new HoleCards();
        hole2.addCard(Card.valueOf(Suit.CLUBS, Rank.SIX));
        hole2.addCard(Card.valueOf(Suit.DIAMONDS, Rank.TWO));

        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.THREE));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.FOUR));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));

        Hand hand1 = HandEvaluator.evaluate(hole1, comm);
        Hand hand2 = HandEvaluator.evaluate(hole2, comm);

        assertEquals(HandRank.STRAIGHT, hand1.getRank());
        assertEquals(HandRank.STRAIGHT, hand2.getRank());

        // Hand 2 (6-high) should beat Hand 1 (5-high)
        assertTrue(hand2.compareTo(hand1) > 0, "6-high straight should beat 5-high (wheel) straight");
    }

    @Test
    void testCounterfeitedTwoPair() {
        // Player 1: Pocket 5s
        HoleCards hole1 = new HoleCards();
        hole1.addCard(Card.valueOf(Suit.SPADES, Rank.FIVE));
        hole1.addCard(Card.valueOf(Suit.HEARTS, Rank.FIVE));

        // Player 2: K, Q
        HoleCards hole2 = new HoleCards();
        hole2.addCard(Card.valueOf(Suit.SPADES, Rank.KING));
        hole2.addCard(Card.valueOf(Suit.HEARTS, Rank.QUEEN));

        // Board: 8, 8, 9, 9, A
        CommunityCards comm = new CommunityCards();
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.EIGHT));
        comm.addCard(Card.valueOf(Suit.CLUBS, Rank.EIGHT));
        comm.addCard(Card.valueOf(Suit.SPADES, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));
        comm.addCard(Card.valueOf(Suit.DIAMONDS, Rank.ACE));

        Hand hand1 = HandEvaluator.evaluate(hole1, comm);
        Hand hand2 = HandEvaluator.evaluate(hole2, comm);

        // Both play the board: 9-9, 8-8, A
        assertEquals(HandRank.TWO_PAIR, hand1.getRank());
        assertEquals(HandRank.TWO_PAIR, hand2.getRank());
        assertEquals(0, hand1.compareTo(hand2), "Pocket pair lower than board pairs should be counterfeited");
    }
}
