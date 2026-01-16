package poker.cards;

import playingcards.Cards;
import poker.State;

/**
 * コミュニティカード（場のカード）を表すクラス。
 * 最大5枚のカードを保持します。
 */
public class CommunityCards extends Cards {
    public CommunityCards() {
        super(5, "コミュニティカード");
    }

    /**
     * 現在のカード枚数から、現在のストリート（ゲーム進行状態）を判定して返します。
     * @return PREFLOP (0枚), FLOP (3枚), TURN (4枚), RIVER (5枚)
     */
    public State getCurrentStreet() {
        int count = getCards().size();
        switch (count) {
            case 0: return State.PREFLOP;
            case 3: return State.FLOP;
            case 4: return State.TURN;
            case 5: return State.RIVER;
            default: throw new IllegalStateException("コミュニティカードの枚数が不正です: " + count);
        }
    }
}