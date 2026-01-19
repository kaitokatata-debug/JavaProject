package poker;

import java.util.List;

import playingcards.Card;

/**
 * テキサスホールデムのゲーム進行を管理するクラス。
 * プレイヤー、デッキ、コミュニティカード（場のカード）の状態を保持します。
 */
public class TexasHoldemGame {
    private Logger logger;
    private Table table;

    public TexasHoldemGame() {
        this.logger = new Logger();
        this.table = new Table();
    }

    /**
     * ロガーを設定します。テスト時に使用します。
     * @param logger ロガー
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * ログを出力します。
     * @param message ログメッセージ
     */
    public void log(String message) {
        logger.log(message);
    }

    /**
     * 実行ログのリストを取得します。
     * @return ログメッセージのリスト
     */
    public List<String> getLogs() { return logger.getLogs(); }

    /**
     * ログをクリアします。
     */
    public void clearLogs() { logger.clear(); }

    /**
     * ゲームにプレイヤーを追加します。
     */
    public void addPlayer(Player player) {
        table.addPlayer(player);
    }

    /**
     * 新しいラウンド（ハンド）を開始します。
     * デッキをシャッフルし、各プレイヤーに2枚ずつカードを配ります（プリフロップ）。
     */
    public void startNewRound() {
        log("=== 新しいラウンドを開始します ===");
        logger.clear();
        
        table.prepareForNewHand();
        // メインポットを作成して追加しておく
        table.getPot().addSubPot(new Pot.SubPot());
        
        table.dealHoleCards();

        // 最初のプレイヤー（ディーラーの次）にターンを回す
        table.setCurrentPlayerIndex(table.getDealerButtonPosition());
        table.nextTurn();
    }

    /**
     * ゲームをリセットします。
     * 全プレイヤーのチップを初期値に戻し、ゲームの状態を初期化して新しいラウンドを開始します。
     */
    public void resetGame() {
        table.reset();
        for (Player player : table.getPlayers()) {
            player.resetChips();
        }
        startNewRound();
    }

    /**
     * 現在の最高ベット額を設定します。
     * @param amount ベット額
     */
    public void setCurrentHighestBet(int amount) {
        table.setCurrentHighestBet(amount);
    }

    /**
     * ベッティングラウンドを終了し、次のストリートの準備をします。
     */
    public void endBettingRound() {
        log("--- Betting Round Ends ---");
        table.endBettingRound();
        log("Pot Status: " + table.getPot());
    }

    /**
     * フロップ（コミュニティカード3枚）を場に出します。
     */
    public void dealFlop() {
        table.dealFlop();
        printCommunityCards("Flop");
    }

    /**
     * ターン（コミュニティカード4枚目）を場に出します。
     */
    public void dealTurn() {
        table.dealTurn();
        printCommunityCards("Turn");
    }

    /**
     * リバー（コミュニティカード5枚目）を場に出します。
     */
    public void dealRiver() {
        table.dealRiver();
        printCommunityCards("River");
    }

    private void printCommunityCards(String stage) {
        log("[" + stage + "] Community Cards: " + table.getCommunityCards());
    }

    /**
     * 現在の状態に基づいてゲームを次の段階に進めます。
     * (例: PREFLOP -> FLOP)
     */
    public void advanceState() {
        switch (table.getState()) {
            case PREFLOP:
                dealFlop();
                break;
            case FLOP:
                dealTurn();
                break;
            case TURN:
                dealRiver();
                break;
            case RIVER:
                // 次はショーダウンなので、状態を更新するだけ
                executeShowdown();
                break;
            default:
                // 何もしない
        }
    }

    /**
     * ショーダウンを実行し、サイドポットごとに勝者を判定してチップを分配します。
     * ハンドの途中で勝者が決まった場合（他の全員がフォールド）もこのメソッドで処理します。
     */
    public void executeShowdown() {
        new ShowdownManager(table, logger).execute();
    }

    /**
     * プレイヤーのアクションが完了した後に呼び出され、ゲームを進行させます。
     * @param player アクションを行ったプレイヤー
     */
    public void onPlayerAction(Player player) {
        player.setHasActed(true);

        if (isBettingRoundFinished()) {
            endBettingRound();
            
            if (table.getActivePlayers().size() <= 1) {
                executeShowdown();
            } else {
                advanceState();
            }

            // ショーダウンでなければ次のラウンドの準備
            if (getState() != State.SHOWDOWN) {
                // ディーラーボタンの位置からリセットして次のプレイヤーへ
                table.setCurrentPlayerIndex(table.getDealerButtonPosition());
                table.nextTurn();
            }
        } else {
            // ラウンド継続、次のプレイヤーへ
            table.nextTurn();
        }
    }

    /**
     * 現在のベッティングラウンドが終了したかどうかを判定します。
     * 全員がアクション済みで、かつベット額が揃っている（またはオールイン）場合に終了とみなします。
     */
    private boolean isBettingRoundFinished() {
        List<Player> activePlayers = table.getActivePlayers();
        
        // 1人しか残っていない場合は終了（不戦勝処理へ）
        if (activePlayers.size() <= 1) {
            return true;
        }

        for (Player p : activePlayers) {
            // オールインしているプレイヤーは無視（これ以上アクションできないため）
            if (p.getChips() == 0) continue;

            // まだアクションしていない、または最高ベット額に足りていないプレイヤーがいれば終了しない
            if (!p.hasActed() || p.getCurrentBet() != table.getCurrentHighestBet()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 指定されたプレイヤーがAIの場合、自動でアクションを実行します。
     * @param player 現在のターンのプレイヤー
     */
    public void processAiTurn(Player player) {
        if (player.isBot() && player instanceof AIPlayer) {
            ((AIPlayer) player).performTurn(this);
        }
    }

    /**
     * プレイヤーのベットオプションを計算して返します。
     * @param player 対象プレイヤー
     * @return ベットオプション
     */
    public BettingOptions getBettingOptions(Player player) {
        return new BettingCalculator(table).calculate(player);
    }

    // Web表示用のGetterメソッド
    /**
     * 参加しているプレイヤーのリストを取得します。
     * @return プレイヤーリスト
     */
    public List<Player> getPlayers() { return table.getPlayers(); }

    /**
     * 現在のコミュニティカードを取得します。
     * @return コミュニティカードのリスト
     */
    public List<Card> getCommunityCards() { return table.getCommunityCards().getCards(); }

    /**
     * 現在のポットの総額を取得します。
     * @return ポット総額
     */
    public int getPot() { return table.getPot().getTotalAmount(); }

    /**
     * 現在の最高ベット額を取得します。
     * @return 最高ベット額
     */
    public int getCurrentHighestBet() { return table.getCurrentHighestBet(); }

    /**
     * ディーラーボタンの位置（プレイヤーインデックス）を取得します。
     * @return ディーラーボタンの位置
     */
    public int getDealerButtonPosition() { return table.getDealerButtonPosition(); }

    /**
     * テーブル情報を取得します。
     * @return テーブルオブジェクト
     */
    public Table getTable() { return table; }

    /**
     * 現在のゲーム状態を取得します。
     * @return ゲーム状態
     */
    public State getState() { return table.getState(); }

    /**
     * ゲーム状態を設定します。
     * @param state 新しいゲーム状態
     */
    public void setState(State state) { table.setState(state); }
}