package poker.cards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import playingcards.Card;
import playingcards.Cards;

/**
 * ポーカーの役判定を行うクラス。
 */
public class HandEvaluator {

    /**
     * ホールカードとコミュニティカードから最強の役を判定します。
     * 7枚のカードから5枚を選ぶ全ての組み合わせを評価します。
     * 
     * @param holeCards プレイヤーのホールカード
     * @param communityCards コミュニティカード
     * @return 最強の役（Hand）
     */
    public static Hand evaluate(Cards holeCards, Cards communityCards) {
        AllCards allCards = new AllCards(holeCards, communityCards);
        List<List<Card>> combinations = allCards.getCombinations(5);
        
        Hand bestHand = null;
        for (List<Card> combo : combinations) {
            Hand hand = evaluate(combo);
            if (bestHand == null || hand.compareTo(bestHand) > 0) {
                bestHand = hand;
            }
        }
        return bestHand;
    }

    /**
     * 指定された5枚のカードの役を判定し、Handインスタンスを生成します。
     * 
     * @param cards 判定する5枚のカードのリスト
     * @return 判定された役（Hand）
     */
    public static Hand evaluate(List<Card> cards) {
        // 副作用を防ぐためにリストをコピーしてソート
        List<Card> sortedCards = new ArrayList<>(cards);
        Cards.sort(sortedCards);

        boolean flush = isFlush(sortedCards);
        boolean straight = isStraight(sortedCards);

        Map<Card.Rank, Integer> rankCounts = getRankCounts(sortedCards);

        if (flush && straight) {
            if (sortedCards.get(0).getRank() == Card.Rank.ACE && sortedCards.get(1).getRank() == Card.Rank.KING) {
                return new Hand(HandRank.ROYAL_FLUSH, sortedCards);
            }
            // A-2-3-4-5 のストレートフラッシュ対応（Aを最後に移動）
            if (sortedCards.get(0).getRank() == Card.Rank.ACE && sortedCards.get(4).getRank() == Card.Rank.TWO) {
                 List<Card> reordered = new ArrayList<>(sortedCards);
                 reordered.add(reordered.remove(0));
                 return new Hand(HandRank.STRAIGHT_FLUSH, reordered);
            }
            return new Hand(HandRank.STRAIGHT_FLUSH, sortedCards);
        }
        if (isFourOfAKind(rankCounts)) return new Hand(HandRank.FOUR_OF_A_KIND, reorderByFrequency(sortedCards, rankCounts));
        if (isFullHouse(rankCounts)) return new Hand(HandRank.FULL_HOUSE, reorderByFrequency(sortedCards, rankCounts));
        if (flush) return new Hand(HandRank.FLUSH, sortedCards);
        if (straight) {
             if (sortedCards.get(0).getRank() == Card.Rank.ACE && sortedCards.get(4).getRank() == Card.Rank.TWO) {
                 List<Card> reordered = new ArrayList<>(sortedCards);
                 reordered.add(reordered.remove(0));
                 return new Hand(HandRank.STRAIGHT, reordered);
            }
            return new Hand(HandRank.STRAIGHT, sortedCards);
        }
        if (isThreeOfAKind(rankCounts)) return new Hand(HandRank.THREE_OF_A_KIND, reorderByFrequency(sortedCards, rankCounts));
        if (isTwoPair(rankCounts)) return new Hand(HandRank.TWO_PAIR, reorderByFrequency(sortedCards, rankCounts));
        if (isOnePair(rankCounts)) return new Hand(HandRank.ONE_PAIR, reorderByFrequency(sortedCards, rankCounts));

        return new Hand(HandRank.HIGH_CARD, sortedCards);
    }

    // --- 役判定用の静的メソッド ---

    /**
     * フラッシュ（同じスートが5枚）かどうかを判定します。
     * @param cards ソート済みの5枚のカード
     * @return フラッシュの場合はtrue
     */
    private static boolean isFlush(List<Card> cards) {
        Card.Suit s = cards.get(0).getSuit();
        return cards.stream().allMatch(c -> c.getSuit() == s);
    }

    /**
     * ストレート（ランクが連続する5枚）かどうかを判定します。
     * A-5-4-3-2 (Wheel) のケースも考慮します。
     * @param cards ソート済みの5枚のカード
     * @return ストレートの場合はtrue
     */
    private static boolean isStraight(List<Card> cards) {
        return isStandardStraight(cards) || isWheelStraight(cards);
    }

    private static boolean isStandardStraight(List<Card> cards) {
        return IntStream.range(0, cards.size() - 1)
                .allMatch(i -> cards.get(i).getRank().getValue() - cards.get(i + 1).getRank().getValue() == 1);
    }

    private static boolean isWheelStraight(List<Card> cards) {
        // A-5-4-3-2 のストレート判定 (ソート済み: A, 5, 4, 3, 2)
        return cards.get(0).getRank() == Card.Rank.ACE &&
               cards.get(1).getRank() == Card.Rank.FIVE &&
               cards.get(2).getRank() == Card.Rank.FOUR &&
               cards.get(3).getRank() == Card.Rank.THREE &&
               cards.get(4).getRank() == Card.Rank.TWO;
    }

    /**
     * 各ランクの出現回数をカウントします。
     * @param cards カードリスト
     * @return ランクをキー、出現回数を値とするマップ
     */
    private static Map<Card.Rank, Integer> getRankCounts(List<Card> cards) {
        return cards.stream()
                .collect(Collectors.groupingBy(
                        Card::getRank,
                        () -> new EnumMap<>(Card.Rank.class),
                        Collectors.summingInt(c -> 1)
                ));
    }

    /**
     * フォーカード（同じランクが4枚）かどうかを判定します。
     */
    private static boolean isFourOfAKind(Map<Card.Rank, Integer> counts) { return counts.containsValue(4); }

    /**
     * フルハウス（3枚組と2枚組）かどうかを判定します。
     */
    private static boolean isFullHouse(Map<Card.Rank, Integer> counts) { return counts.containsValue(3) && counts.containsValue(2); }

    /**
     * スリーカード（同じランクが3枚）かどうかを判定します。
     */
    private static boolean isThreeOfAKind(Map<Card.Rank, Integer> counts) { return counts.containsValue(3); }

    /**
     * ツーペア（2枚組が2つ）かどうかを判定します。
     */
    private static boolean isTwoPair(Map<Card.Rank, Integer> counts) { return counts.values().stream().filter(v -> v == 2).count() == 2; }

    /**
     * ワンペア（2枚組が1つ）かどうかを判定します。
     */
    private static boolean isOnePair(Map<Card.Rank, Integer> counts) { return counts.containsValue(2); }

    /**
     * ランクの出現頻度順、次いでランクの強さ順にカードを並べ替えます。
     * 例: K, K, 5, 5, A -> K, K, 5, 5, A (ペア同士の比較用)
     */
    private static List<Card> reorderByFrequency(List<Card> cards, Map<Card.Rank, Integer> counts) {
        return cards.stream()
                .sorted(Comparator.<Card, Integer>comparing(c -> counts.get(c.getRank()))
                        .thenComparing(c -> c.getRank().getValue())
                        .reversed())
                .collect(Collectors.toList());
    }
}