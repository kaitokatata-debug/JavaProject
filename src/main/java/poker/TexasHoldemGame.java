package poker;

import java.util.ArrayList;
import java.util.List;

import poker.Card;
import poker.Deck;
import poker.HandEvaluator;
import poker.Player;

/**
 * テキサスホールデムのゲーム進行を管理するクラス。
 * プレイヤー、デッキ、コミュニティカード（場のカード）の状態を保持します。
 */
public class TexasHoldemGame {
    private Deck deck;
    private List<Player> players;
    private List<Card> communityCards; // 場に出る共通カード
    private int pot; // ポット（賭け金の総額）
    private int currentHighestBet; // 現在の最高ベット額
    private List<String> logs = new ArrayList<>(); // ゲームログ

    public TexasHoldemGame() {
        deck = new Deck();
        players = new ArrayList<>();
        communityCards = new ArrayList<>();
    }

    // ログ出力用メソッド
    public void log(String message) {
        logs.add(message);
        System.out.println(message); // コンソールにも出す
    }

    public List<String> getLogs() { return logs; }
    public void clearLogs() { logs.clear(); }

    /**
     * ゲームにプレイヤーを追加します。
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * 新しいラウンド（ハンド）を開始します。
     * デッキをシャッフルし、各プレイヤーに2枚ずつカードを配ります（プリフロップ）。
     */
    public void startNewRound() {
        log("=== 新しいラウンドを開始します ===");
        logs.clear();
        pot = 0;
        currentHighestBet = 0;
        deck.initialize();
        communityCards.clear();
        for (Player p : players) {
            p.clearHand();
            // プリフロップ: 各プレイヤーに2枚配る
            p.addCard(deck.draw());
            p.addCard(deck.draw());
            // log(p.toString()); // 手札はログに出すとバレるので隠す
        }
    }

    // --- ベッティングアクション ---

    public void playerBet(Player player, int amount) {
        if (player.isFolded()) return;
        player.bet(amount);
        pot += amount;
        if (player.getCurrentBet() > currentHighestBet) {
            currentHighestBet = player.getCurrentBet();
        }
        log(player.getName() + " bets " + amount + " (Total: " + player.getCurrentBet() + ")");
    }

    public void playerCall(Player player) {
        if (player.isFolded()) return;
        int amountToCall = currentHighestBet - player.getCurrentBet();
        if (amountToCall > 0) {
            player.bet(amountToCall);
            pot += amountToCall;
            log(player.getName() + " calls " + amountToCall);
        } else {
            log(player.getName() + " checks");
        }
    }

    public void playerFold(Player player) {
        player.fold();
        log(player.getName() + " folds");
    }

    /**
     * ベッティングラウンドを終了し、次のストリートの準備をします。
     */
    public void endBettingRound() {
        log("--- Betting Round Ends. Pot: " + pot + " ---");
        currentHighestBet = 0;
        for (Player p : players) {
            p.resetBet();
        }
    }

    // フロップ: コミュニティカードを3枚開く
    public void dealFlop() {
        deck.draw(); // バーンカード（不正防止で1枚捨てる）
        communityCards.add(deck.draw());
        communityCards.add(deck.draw());
        communityCards.add(deck.draw());
        printCommunityCards("Flop");
    }

    // ターン: 4枚目を開く
    public void dealTurn() {
        deck.draw(); // バーン
        communityCards.add(deck.draw());
        printCommunityCards("Turn");
    }

    // リバー: 5枚目を開く
    public void dealRiver() {
        deck.draw(); // バーン
        communityCards.add(deck.draw());
        printCommunityCards("River");
    }

    private void printCommunityCards(String stage) {
        log("[" + stage + "] Community Cards: " + communityCards);
    }

    // 動作確認用のメインメソッド
    public static void main(String[] args) {
        TexasHoldemGame game = new TexasHoldemGame();
        
        // プレイヤー参加
        Player alice = new Player("Alice", 1000);
        Player bob = new Player("Bob", 1000);
        game.addPlayer(alice);
        game.addPlayer(bob);

        // ゲーム進行
        game.startNewRound();
        
        // プリフロップのベット進行例
        game.playerBet(alice, 50); // Aliceが50ベット
        game.playerCall(bob);      // Bobがコール
        game.endBettingRound();

        game.dealFlop();
        // フロップでのベット進行例
        game.playerCall(alice);    // Aliceチェック
        game.playerBet(bob, 100);  // Bobが100ベット
        game.playerCall(alice);    // Aliceコール
        game.endBettingRound();

        game.dealTurn();
        game.endBettingRound(); // チェックで進行
        game.dealRiver();
        game.endBettingRound(); // チェックで進行
        
        

        System.out.println("=== ラウンド終了: 役判定へ ===");

        HandEvaluator.Hand bestHand = null;
        List<Player> winners = new ArrayList<>();

        // 各プレイヤーの役を判定して表示
        for (Player p : game.players) {
            if (p.isFolded()) continue; // フォールドした人は判定しない

            HandEvaluator.Hand hand = HandEvaluator.evaluate(p.getHoleCards(), game.communityCards);
            System.out.println(p.getName() + " の役: " + hand);

            if (bestHand == null || hand.compareTo(bestHand) > 0) {
                bestHand = hand;
                winners.clear();
                winners.add(p);
            } else if (hand.compareTo(bestHand) == 0) {
                winners.add(p);
            }
        }

        System.out.println("\n=== 勝者 ===");
        for (Player winner : winners) {
            System.out.println("Winner: " + winner.getName() + " (" + bestHand + ") wins Pot: " + game.pot);
        }
    }

    // Web表示用のGetterメソッド
    public List<Player> getPlayers() { return players; }
    public List<Card> getCommunityCards() { return communityCards; }
    public int getPot() { return pot; }
    public int getCurrentHighestBet() { return currentHighestBet; }
}