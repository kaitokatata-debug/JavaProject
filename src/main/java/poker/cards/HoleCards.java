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
}