package poker;

import java.util.List;

import playingcards.Card;
import playingcards.Cards;
import poker.cards.AllCards;
import poker.cards.Hand;

/**
 * ポーカーの役判定を行うクラス。
 * 7枚のカードから最強の5枚を選び、役を判定します。
 */
public class HandEvaluator {

    /**
     * 手札とコミュニティカード（計7枚以上）から最強のハンドを判定します。
     * 
     * @param holeCards プレイヤーの手札
     * @param communityCards コミュニティカード
     * @return 判定された最強のハンド
     */
    public static Hand evaluate(Cards holeCards, Cards communityCards) {
        AllCards allCards = new AllCards(holeCards, communityCards);

        if (allCards.size() < 5) {
            throw new IllegalArgumentException("カードが足りません（5枚以上必要）");
        }

        // 7枚から5枚選ぶ全組み合わせを生成して評価
        List<List<Card>> combinations = allCards.getCombinations(5);
        
        Hand bestHand = null;
        for (List<Card> combo : combinations) {
            Hand hand = Hand.evaluate(combo);
            if (bestHand == null || hand.compareTo(bestHand) > 0) {
                bestHand = hand;
            }
        }
        return bestHand;
    }
}