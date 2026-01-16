package poker;

import java.util.List;

import playingcards.Card;
import poker.cards.HoleCards;

/**
 * ポーカーのプレイヤーを表すクラス。
 * 名前、手札、所持チップを管理します。
 */
public class Player {
    private String name;
    private HoleCards holeCards = new HoleCards(); // 手札（ホールカード）
    private int chips;
    private boolean isFolded = false; // フォールドしたかどうか
    private int currentBet = 0; // 現在のラウンドで賭けた額
    private int totalBetInHand = 0; // このハンド全体での賭け金合計

    /**
     * プレイヤーを生成します。
     * @param name プレイヤー名
     * @param chips 初期所持チップ数
     */
    public Player(String name, int chips) {
        this.name = name;
        this.chips = chips;
    }

    /**
     * 手札にカードを追加します。
     */
    public void addCard(Card card) {
        holeCards.addCard(card);
    }

    /**
     * 手札をリセットし、フォールド状態を解除します。
     * 新しいラウンドの開始時に呼び出します。
     */
    public void clearHand() {
        holeCards.clear();
        isFolded = false;
        currentBet = 0;
        totalBetInHand = 0;
    }

    /**
     * チップを賭けます。
     * @param amount 賭ける額
     */
    public void bet(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("ベット額は正の数である必要があります。");
        }
        if (amount > chips) {
            amount = chips; // 足りない場合はオールイン（全額）
        }
        chips -= amount;
        currentBet += amount;
        totalBetInHand += amount;
    }

    /**
     * チップを獲得します。
     * @param amount 獲得する額
     */
    public void winChips(int amount) {
        this.chips += amount;
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

    public int getTotalBetInHand() {
        return totalBetInHand;
    }

    public void fold() {
        isFolded = true;
    }

    public boolean isFolded() {
        return isFolded;
    }

    public HoleCards getHoleCards() {
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

    // --- アクション実行メソッド ---

    /**
     * ベットアクションを実行します。
     * @param game ゲームインスタンス
     * @param amount ベット額
     */
    public void doBet(TexasHoldemGame game, int amount) {
        new Action(this, Action.Type.BET, amount).execute(game);
    }

    /**
     * コール（またはチェック）アクションを実行します。
     * @param game ゲームインスタンス
     */
    public void doCall(TexasHoldemGame game) {
        new Action(this, Action.Type.CALL).execute(game);
    }

    /**
     * フォールドアクションを実行します。
     * @param game ゲームインスタンス
     */
    public void doFold(TexasHoldemGame game) {
        new Action(this, Action.Type.FOLD).execute(game);
    }

    /**
     * オールインアクションを実行します。
     * @param game ゲームインスタンス
     */
    public void doAllIn(TexasHoldemGame game) {
        new Action(this, Action.Type.ALL_IN).execute(game);
    }
}