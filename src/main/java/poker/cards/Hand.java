package poker.cards;

import java.util.List;

import playingcards.Card;
import playingcards.Cards;

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

    /**
     * このハンドの役ランクを取得します。
     * @return 役ランク。判定前の場合はnull。
     */
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
        for (int i = 0; i < size(); i++) {
            int cmp = get(i).compareTo(other.get(i));
            if (cmp != 0) return cmp; // Card.compareToは数字の比較
        }
        return 0;
    }
}