package poker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import poker.cards.CommunityCards;
import poker.cards.Hand;
import poker.cards.HandEvaluator;

/**
 * ショーダウン（勝敗判定とチップ分配）のロジックを管理するクラス。
 */
public class ShowdownManager {
    private final Table table;
    private final Logger logger;

    public ShowdownManager(Table table, Logger logger) {
        this.table = table;
        this.logger = logger;
    }

    private void log(String message) {
        logger.log(message);
    }

    /**
     * ショーダウンを実行し、サイドポットごとに勝者を判定してチップを分配します。
     * ハンドの途中で勝者が決まった場合（他の全員がフォールド）もこのメソッドで処理します。
     */
    public void execute() {
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
            player.setBestHand(hand);
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
                winner.setWinner(true);
                winner.winChips(prize);
            }
            // 端数は最初の勝者に渡す
            if (remainder > 0) {
                winners.get(0).winChips(remainder);
                log(winners.get(0).getName() + " receives the remainder of " + remainder);
            }
        }
    }
}