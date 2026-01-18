package poker.cards;

import playingcards.Cards;

/**
 * プレイヤーに配られるホールカード（手札）を表すクラス。
 * 最大2枚のカードを保持します。
 */
public class HoleCards extends Cards {
    public HoleCards() {
        super(2, "ホールカード");
    }

    /**
     * 2枚のカードがペア（同じランク）かどうかを判定します。
     * @return ペアの場合はtrue
     */
    public boolean isPair() {
        if (size() != 2) return false;
        return get(0).getRank() == get(1).getRank();
    }

    /**
     * 2枚のカードがスーテッド（同じスート）かどうかを判定します。
     * @return スーテッドの場合はtrue
     */
    public boolean isSuited() {
        if (size() != 2) return false;
        return get(0).getSuit() == get(1).getSuit();
    }

    /**
     * 2枚のカードがコネクター（ランクが連続している）かどうかを判定します。
     * @return コネクターの場合はtrue
     */
    public boolean isConnector() {
        if (size() != 2) return false;
        int rank1 = get(0).getRank().getValue();
        int rank2 = get(1).getRank().getValue();
        return Math.abs(rank1 - rank2) == 1;
    }
}