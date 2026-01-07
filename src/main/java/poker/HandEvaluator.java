package poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ポーカーの役判定を行うクラス。
 * 7枚のカードから最強の5枚を選び、役を判定します。
 */
public class HandEvaluator {

    /**
     * 役の強さを表す列挙型（弱い順）
     */
    public enum HandRank {
        HIGH_CARD("High Card"),
        ONE_PAIR("One Pair"),
        TWO_PAIR("Two Pair"),
        THREE_OF_A_KIND("Three of a Kind"),
        STRAIGHT("Straight"),
        FLUSH("Flush"),
        FULL_HOUSE("Full House"),
        FOUR_OF_A_KIND("Four of a Kind"),
        STRAIGHT_FLUSH("Straight Flush"),
        ROYAL_FLUSH("Royal Flush");

        private final String label;
        HandRank(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    /**
     * 判定された役と、その構成カードを持つクラス。
     * 比較可能（Comparable）にして、どちらが強いか判定できるようにします。
     */
    public static class Hand implements Comparable<Hand> {
        private final HandRank handrank;
        private final List<Card> cards; // 役を構成する5枚（強さ順）

        public Hand(HandRank handrank, List<Card> cards) {
            this.handrank = handrank;
            this.cards = cards;
        }

        public HandRank getHandrank() { return handrank; }

        @Override
        public int compareTo(Hand other) {
            // まず役のランクで比較
            if (this.handrank != other.handrank) {
                return this.handrank.compareTo(other.handrank);
            }
            // 同じ役なら、カードの数字（キッカー）を強い順に比較
            for (int i = 0; i < this.cards.size(); i++) {
                int val1 = this.cards.get(i).getRank().getValue();
                int val2 = other.cards.get(i).getRank().getValue();
                int cmp = Integer.compare(val1, val2);
                if (cmp != 0) return cmp;
            }
            return 0;
        }

        @Override
        public String toString() {
            return handrank.getLabel() + " " + cards;
        }
    }

    /**
     * 手札とコミュニティカード（計7枚以上）から最強のハンドを判定します。
     */
    public static Hand evaluate(List<Card> holeCards, List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(holeCards);
        allCards.addAll(communityCards);

        if (allCards.size() < 5) {
            throw new IllegalArgumentException("カードが足りません（5枚以上必要）");
        }

        // 7枚から5枚選ぶ全組み合わせを生成して評価
        List<List<Card>> combinations = generateCombinations(allCards, 5);
        
        Hand bestHand = null;
        for (List<Card> combo : combinations) {
            Hand hand = evaluate5(combo);
            if (bestHand == null || hand.compareTo(bestHand) > 0) {
                bestHand = hand;
            }
        }
        return bestHand;
    }

    // 組み合わせ生成ヘルパー
    private static List<List<Card>> generateCombinations(List<Card> cards, int k) {
        List<List<Card>> combinations = new ArrayList<>();
        combine(cards, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private static void combine(List<Card> cards, int k, int start, List<Card> current, List<List<Card>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            combine(cards, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // 5枚のカードの役を判定するメソッド
    private static Hand evaluate5(List<Card> cards) {
        // ランクの強い順にソート
        Collections.sort(cards, (c1, c2) -> Integer.compare(c2.getRank().getValue(), c1.getRank().getValue()));

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

    // 各種判定ロジック
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

    // ペアなどのカードをリストの前方に移動させる（比較用）
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