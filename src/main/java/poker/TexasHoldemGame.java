package poker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import playingcards.Card;
import poker.cards.CommunityCards;
import poker.cards.Hand;

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
     * ログを出力します。
     * @param message ログメッセージ
     */
    public void log(String message) {
        logger.log(message);
    }

    public List<String> getLogs() { return logger.getLogs(); }
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
    }

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
        table.setState(State.SHOWDOWN);
        log("--- Showdown ---");

        List<Player> activePlayers = table.getActivePlayers();
        
        // 1人しか残っていない場合（不戦勝）
        if (activePlayers.size() == 1) {
            handleWalkover(activePlayers.get(0));
            return;
        }

        // 全プレイヤーの役を判定
        Map<Player, Hand> playerHands = evaluateHands(activePlayers);

        // 各サブポットを分配
        List<Pot.SubPot> subPots = table.getPot().getSubPots();
        for (int i = 0; i < subPots.size(); i++) {
            resolvePot(subPots.get(i), playerHands, i + 1);
        }
    }

    private void handleWalkover(Player winner) {
        int totalPot = table.getPot().getTotalAmount();
        log(winner.getName() + " is the last player remaining and wins the pot of " + totalPot);
        winner.winChips(totalPot);
        table.getPot().clear();
    }

    private Map<Player, Hand> evaluateHands(List<Player> players) {
        Map<Player, Hand> playerHands = new HashMap<>();
        CommunityCards communityCards = table.getCommunityCards();
        for (Player player : players) {
            Hand hand = HandEvaluator.evaluate(player.getHoleCards(), communityCards);
            playerHands.put(player, hand);
            log(player.getName() + "'s hand: " + hand);
        }
        return playerHands;
    }

    private void resolvePot(Pot.SubPot subPot, Map<Player, Hand> playerHands, int potIndex) {
        if (subPot.getAmount() == 0) return;

        log("Evaluating Pot #" + potIndex + " (" + subPot.getAmount() + ")");

        List<Player> contenders = subPot.getEligiblePlayers().stream()
                                        .filter(playerHands::containsKey)
                                        .collect(Collectors.toList());

        if (contenders.isEmpty()) {
            log("No contenders for Pot #" + potIndex);
            return;
        }

        // 最強のハンドを見つける
        Hand bestHand = contenders.stream()
                .map(playerHands::get)
                .max(Hand::compareTo)
                .orElse(null);

        // 最強ハンドを持つプレイヤー（複数可）を抽出
        List<Player> winners = contenders.stream()
                .filter(p -> playerHands.get(p).compareTo(bestHand) == 0)
                .collect(Collectors.toList());

        distributePot(subPot, winners, bestHand, potIndex);
    }

    private void distributePot(Pot.SubPot subPot, List<Player> winners, Hand bestHand, int potIndex) {
        if (!winners.isEmpty()) {
            int prize = subPot.getAmount() / winners.size();
            int remainder = subPot.getAmount() % winners.size();

            String winnerNames = winners.stream().map(Player::getName).collect(Collectors.joining(", "));
            log("Pot #" + potIndex + " of " + subPot.getAmount() + " goes to " + winnerNames + " with " + bestHand);

            for (Player winner : winners) {
                winner.winChips(prize);
            }
            // 端数は最初の勝者に渡す
            if (remainder > 0) {
                winners.get(0).winChips(remainder);
                log(winners.get(0).getName() + " receives the remainder of " + remainder);
            }
        }
    }

    // Web表示用のGetterメソッド
    public List<Player> getPlayers() { return table.getPlayers(); }
    public List<Card> getCommunityCards() { return table.getCommunityCards().getCards(); }
    public int getPot() { return table.getPot().getTotalAmount(); }
    public int getCurrentHighestBet() { return table.getCurrentHighestBet(); }
    public Table getTable() { return table; }
    public State getState() { return table.getState(); }
    public void setState(State state) { table.setState(state); }
}