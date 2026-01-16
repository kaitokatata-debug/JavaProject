package poker.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import playingcards.Card;
import playingcards.Cards;
import poker.HandRank;

/**
 * ポーカーのハンド（手札、または役判定後の5枚の手札）を表すクラス。
 * プレイヤーの持ち札としての機能と、役判定結果としての機能を併せ持ちます。
 */
public class Hand extends Cards implements Comparable<Hand> {

    private HandRank rank;

    /**
     * 空のハンドを作成します（プレイヤーの手札用）。
     */
    public Hand() {
        super(5, "ハンド");
        this.rank = null;
    }

    /**
     * 役とカードを指定してハンドを作成します（役判定結果用）。
     * @param rank 判定された役
     * @param cards 役を構成するカード
     */
    public Hand(HandRank rank, List<Card> cards) {
        super(5, "ハンド");
        if (cards.size() != 2 && cards.size() != 5) {
            throw new IllegalArgumentException("ハンドのカード枚数は2枚または5枚である必要があります。現在の枚数: " + cards.size());
        }
        this.rank = rank;
        addCards(cards);
    }

    /**
     * ハンドのカードと役判定をクリアします。
     */
    @Override
    public void clear() {
        super.clear();
        rank = null;
    }

    public HandRank getRank() {
        return rank;
    }

    @Override
    public String toString() {
        if (rank == null) return super.toString();
        return rank + " " + super.toString();
    }

    @Override
    public int compareTo(Hand other) {
        if (!this.rank.isSameRank(other.rank)) {
            return this.rank.isStrongerThan(other.rank) ? 1 : -1;
        }
        for (int i = 0; i < this.cards.size(); i++) {
            int cmp = this.cards.get(i).compareTo(other.cards.get(i));
            if (cmp != 0) return cmp; // Card.compareToは数字の比較
        }
        return 0;
    }

    /**
     * 指定された5枚のカードの役を判定し、Handインスタンスを生成します。
     * 
     * @param cards 判定する5枚のカードのリスト
     * @return 判定された役（Hand）
     */
    public static Hand evaluate(List<Card> cards) {
        // ランクの強い順にソート
        Cards.sort(cards);

        boolean flush = isFlush(cards);
        boolean straight = isStraight(cards);

        if (flush && straight) {
            if (cards.get(0).getRank() == Card.Rank.ACE && cards.get(1).getRank() == Card.Rank.KING) {
                return new Hand(HandRank.ROYAL_FLUSH, cards);
            }
            // A-2-3-4-5 のストレートフラッシュ対応（Aを最後に移動）
            if (cards.get(0).getRank() == Card.Rank.ACE && cards.get(4).getRank() == Card.Rank.TWO) {
                 List<Card> reordered = new ArrayList<>(cards);
                 reordered.add(reordered.remove(0));
                 return new Hand(HandRank.STRAIGHT_FLUSH, reordered);
            }
            return new Hand(HandRank.STRAIGHT_FLUSH, cards);
        }
        if (isFourOfAKind(cards)) return new Hand(HandRank.FOUR_OF_A_KIND, reorderByFrequency(cards));
        if (isFullHouse(cards)) return new Hand(HandRank.FULL_HOUSE, reorderByFrequency(cards));
        if (flush) return new Hand(HandRank.FLUSH, cards);
        if (straight) {
             if (cards.get(0).getRank() == Card.Rank.ACE && cards.get(4).getRank() == Card.Rank.TWO) {
                 List<Card> reordered = new ArrayList<>(cards);
                 reordered.add(reordered.remove(0));
                 return new Hand(HandRank.STRAIGHT, reordered);
            }
            return new Hand(HandRank.STRAIGHT, cards);
        }
        if (isThreeOfAKind(cards)) return new Hand(HandRank.THREE_OF_A_KIND, reorderByFrequency(cards));
        if (isTwoPair(cards)) return new Hand(HandRank.TWO_PAIR, reorderByFrequency(cards));
        if (isOnePair(cards)) return new Hand(HandRank.ONE_PAIR, reorderByFrequency(cards));

        return new Hand(HandRank.HIGH_CARD, cards);
    }

    // --- 役判定用の静的メソッド ---

    private static boolean isFlush(List<Card> cards) {
        Card.Suit s = cards.get(0).getSuit();
        for (Card c : cards) if (c.getSuit() != s) return false;
        return true;
    }

    private static boolean isStraight(List<Card> cards) {
        boolean standard = true;
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i).getRank().getValue() - cards.get(i+1).getRank().getValue() != 1) {
                standard = false; break;
            }
        }
        if (standard) return true;
        // A-5-4-3-2 のストレート判定
        return cards.get(0).getRank() == Card.Rank.ACE && cards.get(1).getRank() == Card.Rank.FIVE &&
               cards.get(4).getRank() == Card.Rank.TWO;
    }

    private static Map<Integer, Integer> getRankCounts(List<Card> cards) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Card c : cards) counts.put(c.getRank().getValue(), counts.getOrDefault(c.getRank().getValue(), 0) + 1);
        return counts;
    }

    private static boolean isFourOfAKind(List<Card> cards) { return getRankCounts(cards).containsValue(4); }
    private static boolean isFullHouse(List<Card> cards) { Map<Integer, Integer> c = getRankCounts(cards); return c.containsValue(3) && c.containsValue(2); }
    private static boolean isThreeOfAKind(List<Card> cards) { Map<Integer, Integer> c = getRankCounts(cards); return c.containsValue(3) && !c.containsValue(2); }
    private static boolean isTwoPair(List<Card> cards) { return getRankCounts(cards).values().stream().filter(v -> v == 2).count() == 2; }
    private static boolean isOnePair(List<Card> cards) { Map<Integer, Integer> c = getRankCounts(cards); return c.containsValue(2) && !c.containsValue(3); }

    /**
     * ペアなどのカードをリストの前方に移動させます（比較用）。
     */
    private static List<Card> reorderByFrequency(List<Card> cards) {
        Map<Integer, Integer> counts = getRankCounts(cards);
        List<Card> sorted = new ArrayList<>(cards);
        Collections.sort(sorted, (c1, c2) -> {
            int diff = counts.get(c2.getRank().getValue()) - counts.get(c1.getRank().getValue());
            return diff != 0 ? diff : Integer.compare(c2.getRank().getValue(), c1.getRank().getValue());
        });
        return sorted;
    }
}