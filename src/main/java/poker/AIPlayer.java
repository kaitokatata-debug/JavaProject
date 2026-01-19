package poker;

/**
 * 自動でプレイするAIプレイヤーを表すクラス。
 */
public class AIPlayer extends Player {

    public AIPlayer(String name, int chips) {
        super(name, chips);
    }

    @Override
    public boolean isBot() {
        return true;
    }

    /**
     * ゲーム状況に基づいてアクションを実行します。
     * @param game 現在のゲームインスタンス
     */
    public void performTurn(TexasHoldemGame game) {
        // 基本的なAIロジック:
        // チェックが可能ならチェック、相手がベットしていればコールする。
        // チップが足りなければオールインする。
        
        int amountToCall = game.getCurrentHighestBet() - this.getCurrentBet();

        if (amountToCall > 0) {
            if (amountToCall >= this.getChips()) {
                // チップが足りない、またはちょうどならオールイン
                doAllIn(game);
            } else {
                doCall(game);
            }
        } else {
            doCheck(game);
        }
    }
}