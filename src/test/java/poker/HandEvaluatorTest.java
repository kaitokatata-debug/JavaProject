package poker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import playingcards.Card;
import playingcards.Card.Rank;
import playingcards.Card.Suit;
import poker.cards.CommunityCards;
import poker.cards.Hand;
import poker.cards.HoleCards;

class HandEvaluatorTest {

    @Test
    void testRoyalFlush() {
        HoleCards hole = new HoleCards();
        hole.addCard(new Card(Suit.SPADES, Rank.ACE));
        hole.addCard(new Card(Suit.SPADES, Rank.KING));

        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.SPADES, Rank.QUEEN));
        comm.addCard(new Card(Suit.SPADES, Rank.JACK));
        comm.addCard(new Card(Suit.SPADES, Rank.TEN));
        comm.addCard(new Card(Suit.HEARTS, Rank.TWO)); // 関係ないカード
        comm.addCard(new Card(Suit.DIAMONDS, Rank.THREE)); // 関係ないカード

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.ROYAL_FLUSH, hand.getRank());
    }

    @Test
    void testStraightFlush() {
        HoleCards hole = new HoleCards();
        hole.addCard(new Card(Suit.HEARTS, Rank.NINE));
        hole.addCard(new Card(Suit.HEARTS, Rank.EIGHT));

        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.HEARTS, Rank.SEVEN));
        comm.addCard(new Card(Suit.HEARTS, Rank.SIX));
        comm.addCard(new Card(Suit.HEARTS, Rank.FIVE));
        comm.addCard(new Card(Suit.SPADES, Rank.ACE));
        comm.addCard(new Card(Suit.CLUBS, Rank.KING));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.STRAIGHT_FLUSH, hand.getRank());
    }

    @Test
    void testFourOfAKind() {
        HoleCards hole = new HoleCards();
        hole.addCard(new Card(Suit.SPADES, Rank.ACE));
        hole.addCard(new Card(Suit.HEARTS, Rank.ACE));

        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.DIAMONDS, Rank.ACE));
        comm.addCard(new Card(Suit.CLUBS, Rank.ACE));
        comm.addCard(new Card(Suit.SPADES, Rank.KING));
        comm.addCard(new Card(Suit.HEARTS, Rank.TWO));
        comm.addCard(new Card(Suit.DIAMONDS, Rank.THREE));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.FOUR_OF_A_KIND, hand.getRank());
    }

    @Test
    void testFullHouse() {
        HoleCards hole = new HoleCards();
        hole.addCard(new Card(Suit.SPADES, Rank.KING));
        hole.addCard(new Card(Suit.HEARTS, Rank.KING));

        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.DIAMONDS, Rank.KING));
        comm.addCard(new Card(Suit.CLUBS, Rank.QUEEN));
        comm.addCard(new Card(Suit.SPADES, Rank.QUEEN));
        comm.addCard(new Card(Suit.HEARTS, Rank.TWO));
        comm.addCard(new Card(Suit.DIAMONDS, Rank.THREE));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.FULL_HOUSE, hand.getRank());
    }

    @Test
    void testFlush() {
        HoleCards hole = new HoleCards();
        hole.addCard(new Card(Suit.SPADES, Rank.ACE));
        hole.addCard(new Card(Suit.SPADES, Rank.FIVE));

        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.SPADES, Rank.NINE));
        comm.addCard(new Card(Suit.SPADES, Rank.JACK));
        comm.addCard(new Card(Suit.SPADES, Rank.TWO));
        comm.addCard(new Card(Suit.HEARTS, Rank.KING));
        comm.addCard(new Card(Suit.DIAMONDS, Rank.QUEEN));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.FLUSH, hand.getRank());
    }

    @Test
    void testStraight() {
        HoleCards hole = new HoleCards();
        hole.addCard(new Card(Suit.SPADES, Rank.FIVE));
        hole.addCard(new Card(Suit.HEARTS, Rank.SIX));

        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.DIAMONDS, Rank.SEVEN));
        comm.addCard(new Card(Suit.CLUBS, Rank.EIGHT));
        comm.addCard(new Card(Suit.SPADES, Rank.NINE));
        comm.addCard(new Card(Suit.HEARTS, Rank.ACE));
        comm.addCard(new Card(Suit.DIAMONDS, Rank.KING));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.STRAIGHT, hand.getRank());
    }

    @Test
    void testWheelStraight() {
        // A, 2, 3, 4, 5 のストレート（通称ホイール）
        HoleCards hole = new HoleCards();
        hole.addCard(new Card(Suit.SPADES, Rank.ACE));
        hole.addCard(new Card(Suit.HEARTS, Rank.TWO));

        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.DIAMONDS, Rank.THREE));
        comm.addCard(new Card(Suit.CLUBS, Rank.FOUR));
        comm.addCard(new Card(Suit.SPADES, Rank.FIVE));
        comm.addCard(new Card(Suit.HEARTS, Rank.NINE));
        comm.addCard(new Card(Suit.DIAMONDS, Rank.KING));

        Hand hand = HandEvaluator.evaluate(hole, comm);
        assertEquals(HandRank.STRAIGHT, hand.getRank());
    }

    @Test
    void testKicker() {
        // プレイヤー1: Aのワンペア、キッカー K
        HoleCards hole1 = new HoleCards();
        hole1.addCard(new Card(Suit.SPADES, Rank.ACE));
        hole1.addCard(new Card(Suit.SPADES, Rank.KING));

        // プレイヤー2: Aのワンペア、キッカー Q
        HoleCards hole2 = new HoleCards();
        hole2.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hole2.addCard(new Card(Suit.HEARTS, Rank.QUEEN));

        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.CLUBS, Rank.ACE)); // 共通のA（ペア成立）
        comm.addCard(new Card(Suit.DIAMONDS, Rank.JACK));
        comm.addCard(new Card(Suit.CLUBS, Rank.NINE));
        comm.addCard(new Card(Suit.HEARTS, Rank.TWO));
        comm.addCard(new Card(Suit.DIAMONDS, Rank.THREE));

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
        hole1.addCard(new Card(Suit.SPADES, Rank.ACE));
        hole1.addCard(new Card(Suit.SPADES, Rank.TWO));

        // プレイヤー2: A, 3 (キッカーAが採用される)
        HoleCards hole2 = new HoleCards();
        hole2.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hole2.addCard(new Card(Suit.HEARTS, Rank.THREE));

        // コミュニティカード: K, K, Q, Q, 10
        CommunityCards comm = new CommunityCards();
        comm.addCard(new Card(Suit.CLUBS, Rank.KING));
        comm.addCard(new Card(Suit.DIAMONDS, Rank.KING));
        comm.addCard(new Card(Suit.CLUBS, Rank.QUEEN));
        comm.addCard(new Card(Suit.HEARTS, Rank.QUEEN));
        comm.addCard(new Card(Suit.DIAMONDS, Rank.TEN));

        Hand hand1 = HandEvaluator.evaluate(hole1, comm);
        Hand hand2 = HandEvaluator.evaluate(hole2, comm);

        // 両者とも Two Pair (K, K, Q, Q) with Kicker A (10よりAが強いため採用)
        // 完全に同じ構成なので引き分け (0) になるはず
        assertEquals(0, hand1.compareTo(hand2), "完全に同じ強さのハンドなので引き分けになるはずです");
    }
}
