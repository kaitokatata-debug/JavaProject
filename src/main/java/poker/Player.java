package poker;

import java.util.ArrayList;
import java.util.List;

/**
 * ポーカーのプレイヤーを表すクラス。
 * 名前、手札、所持チップを管理します。
 */
public class Player {
    private String name;
    private List<Card> holeCards = new ArrayList<>(); // 手札（ホールカード）
    private int chips;
    private boolean isFolded = false; // フォールドしたかどうか
    private int currentBet = 0; // 現在のラウンドで賭けた額

    public Player(String name, int chips) {
        this.name = name;
        this.chips = chips;
    }

    /**
     * 手札にカードを追加します。
     */
    public void addCard(Card card) {
        holeCards.add(card);
    }

    /**
     * 手札をリセットし、フォールド状態を解除します。
     * 新しいラウンドの開始時に呼び出します。
     */
    public void clearHand() {
        holeCards.clear();
        isFolded = false;
        currentBet = 0;
    }

    /**
     * チップを賭けます。
     * @param amount 賭ける額
     */
    public void bet(int amount) {
        if (amount > chips) {
            amount = chips; // 足りない場合はオールイン（全額）
        }
        chips -= amount;
        currentBet += amount;
    }

    /**
     * 現在のラウンドでの賭け金をリセットします（次のストリートへ進む際など）。
     */
    public void resetBet() {
        currentBet = 0;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void fold() {
        isFolded = true;
    }

    public boolean isFolded() {
        return isFolded;
    }

    public List<Card> getHoleCards() {
        return holeCards;
    }

    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    @Override
    public String toString() {
        return name + " (Chips: " + chips + ") Hand: " + holeCards;
    }
}