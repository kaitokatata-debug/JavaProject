package poker;

import playingcards.Card;
import poker.cards.HoleCards;
import poker.cards.Hand;

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
    private String lastAction; // 直前のアクション内容（吹き出し表示用）
    private boolean isWinner = false; // そのハンドの勝者かどうか
    private Hand bestHand; // 判定された最強の役（5枚）

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
        lastAction = null;
        isWinner = false;
        bestHand = null;
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

    /**
     * 現在のラウンドで賭けた金額を取得します。
     * @return 現在のベット額
     */
    public int getCurrentBet() {
        return currentBet;
    }

    /**
     * このハンド全体で賭けた金額の合計を取得します。
     * @return ハンド全体のベット総額
     */
    public int getTotalBetInHand() {
        return totalBetInHand;
    }

    /**
     * プレイヤーをフォールド状態にします。
     */
    public void fold() {
        isFolded = true;
    }

    /**
     * プレイヤーがフォールドしているかどうかを判定します。
     * @return フォールドしている場合はtrue
     */
    public boolean isFolded() {
        return isFolded;
    }

    /**
     * プレイヤーのホールカード（手札）を取得します。
     * @return ホールカード
     */
    public HoleCards getHoleCards() {
        return holeCards;
    }

    /**
     * プレイヤー名を取得します。
     * @return 名前
     */
    public String getName() {
        return name;
    }

    /**
     * 現在の所持チップ数を取得します。
     * @return チップ数
     */
    public int getChips() {
        return chips;
    }

    @Override
    public String toString() {
        return name + " (Chips: " + chips + ") Hand: " + holeCards;
    }

    /**
     * 直前のアクション内容を取得します。
     * @return アクションの文字列表現
     */
    public String getLastAction() {
        return lastAction;
    }

    /**
     * 直前のアクション内容を設定します。
     * @param lastAction アクションの文字列表現
     */
    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    /**
     * 直前のアクション内容を取得し、フィールドをクリアします。
     * 一度だけ表示したい場合（吹き出しなど）に使用します。
     */
    public String consumeLastAction() {
        String action = this.lastAction;
        this.lastAction = null;
        return action;
    }

    /**
     * このハンドの勝者かどうかを判定します。
     * @return 勝者の場合はtrue
     */
    public boolean isWinner() {
        return isWinner;
    }

    /**
     * このハンドの勝者かどうかを設定します。
     * @param winner 勝者の場合はtrue
     */
    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    /**
     * 判定された最強の役を取得します。
     * @return 最強の役
     */
    public Hand getBestHand() {
        return bestHand;
    }

    /**
     * 判定された最強の役を設定します。
     * @param bestHand 最強の役
     */
    public void setBestHand(Hand bestHand) {
        this.bestHand = bestHand;
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