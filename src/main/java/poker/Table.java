package poker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import playingcards.Deck;
import poker.cards.CommunityCards;

/**
 * ポーカーのテーブル全体の状態を管理するクラス。
 * プレイヤー、デッキ、ポット、コミュニティカード、ディーラーボタンの位置などを集約します。
 * ゲームの進行ロジック（TexasHoldemGame）から状態管理を分離する責務を負います。
 */
public class Table {

    private final PlayerManager playerManager;
    private final Deck deck;
    private final Pot pot;
    private final CommunityCards communityCards;
    private int currentHighestBet; // 現在の最高ベット額
    private State state; // ゲームの状態

    /**
     * コンストラクタ。
     * 各コンポーネントを初期化します。
     */
    public Table() {
        this.playerManager = new PlayerManager();
        this.deck = new Deck();
        this.pot = new Pot();
        this.communityCards = new CommunityCards();
        this.currentHighestBet = 0;
        this.state = State.WAITING;
    }

    /**
     * テーブルにプレイヤーを追加します。
     * @param player 追加するプレイヤー
     */
    public void addPlayer(Player player) {
        playerManager.addPlayer(player);
    }

    /**
     * 新しいハンド（ラウンド）の準備をします。
     * ポット、コミュニティカードをクリアし、デッキをシャッフルし、
     * 全プレイヤーの状態をリセットし、ディーラーボタンを移動させます。
     */
    public void prepareForNewHand() {
        pot.clear();
        communityCards.clear();
        deck.initialize();
        playerManager.clearHands();
        playerManager.moveDealerButton();
        currentHighestBet = 0;
        this.state = State.DEALING;
    }

    /**
     * テーブルの状態をリセットします（ディーラーボタン位置など）。
     */
    public void reset() {
        playerManager.reset();
    }

    /**
     * ディーラーボタンを次のプレイヤーに移動させます。
     * チップを持っている（ゲームに参加可能な）プレイヤーを探して移動します。
     */
    public void moveDealerButton() {
        playerManager.moveDealerButton();
    }

    /**
     * 各プレイヤーにホールカード（手札）を2枚ずつ配ります。
     */
    public void dealHoleCards() {
        int startIndex = (playerManager.getDealerButtonPosition() + 1) % playerManager.getPlayers().size();
        // 1枚ずつ2周配る
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < playerManager.getPlayers().size(); j++) {
                Player currentPlayer = playerManager.getPlayers().get((startIndex + j) % playerManager.getPlayers().size());
                // チップが0より多いアクティブなプレイヤーにのみ配る
                if (currentPlayer.getChips() > 0) { 
                    currentPlayer.addCard(deck.draw());
                }
            }
        }
        this.state = State.PREFLOP;
    }

    /**
     * フロップ（コミュニティカード3枚）を場に出します。
     * 1枚バーン（捨て札）してから3枚開きます。
     */
    public void dealFlop() {
        deck.draw(); // バーンカード
        communityCards.addCard(deck.draw());
        communityCards.addCard(deck.draw());
        communityCards.addCard(deck.draw());
        this.state = State.FLOP;
    }

    /**
     * ターン（コミュニティカード4枚目）を場に出します。
     * 1枚バーンしてから1枚開きます。
     */
    public void dealTurn() {
        deck.draw(); // バーンカード
        communityCards.addCard(deck.draw());
        this.state = State.TURN;
    }

    /**
     * リバー（コミュニティカード5枚目）を場に出します。
     * 1枚バーンしてから1枚開きます。
     */
    public void dealRiver() {
        deck.draw(); // バーンカード
        communityCards.addCard(deck.draw());
        this.state = State.RIVER;
    }

    /**
     * フォールドしていない、現在ハンドに参加中のプレイヤーリストを返します。
     * @return アクティブなプレイヤーのリスト
     */
    public List<Player> getActivePlayers() {
        return playerManager.getActivePlayers();
    }

    /**
     * ベッティングラウンドを終了し、チップをポットに移動させます。
     * サイドポットの計算もここで行います。
     */
    public void endBettingRound() {
        // 1. 現在のベット額を収集（0より大きいもの）
        List<Integer> bets = playerManager.getPlayers().stream()
                .map(Player::getCurrentBet)
                .filter(bet -> bet > 0)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 2. ベット額が小さい順にポットを分配（サイドポット計算）
        int prevBet = 0;
        for (int bet : bets) {
            int delta = bet - prevBet;
            int sliceAmount = 0;
            Set<Player> eligibleForSlice = new HashSet<>();

            for (Player p : playerManager.getPlayers()) {
                if (p.getCurrentBet() >= bet) {
                    sliceAmount += delta;
                    if (!p.isFolded()) {
                        eligibleForSlice.add(p);
                    }
                }
            }

            if (sliceAmount > 0) {
                List<Pot.SubPot> subPots = pot.getSubPots();
                Pot.SubPot lastPot = subPots.get(subPots.size() - 1);

                // 最後のポットが空（初期状態）か、参加資格者が同じならマージ
                if ((lastPot.getAmount() == 0 && lastPot.getEligiblePlayers().isEmpty()) ||
                    lastPot.getEligiblePlayers().equals(eligibleForSlice)) {
                    lastPot.addAmount(sliceAmount);
                    if (lastPot.getEligiblePlayers().isEmpty()) {
                        lastPot.addPlayers(eligibleForSlice);
                    }
                } else {
                    // 参加者が異なる場合は新しいサイドポットを作成
                    Pot.SubPot newPot = new Pot.SubPot();
                    newPot.addAmount(sliceAmount);
                    newPot.addPlayers(eligibleForSlice);
                    pot.addSubPot(newPot);
                }
            }
            prevBet = bet;
        }

        for (Player p : playerManager.getPlayers()) {
            p.resetBet();
        }
        currentHighestBet = 0;
    }

    /**
     * ターンを次のプレイヤーに進めます。
     * フォールドしているプレイヤーや、チップがない（オールイン済み）プレイヤーはスキップします。
     */
    public void nextTurn() {
        playerManager.nextTurn();
    }

    // --- Getters ---

    public List<Player> getPlayers() {
        return playerManager.getPlayers();
    }

    public Deck getDeck() {
        return deck;
    }

    public Pot getPot() {
        return pot;
    }

    public CommunityCards getCommunityCards() {
        return communityCards;
    }

    public int getDealerButtonPosition() {
        return playerManager.getDealerButtonPosition();
    }

    public int getCurrentHighestBet() {
        return currentHighestBet;
    }

    public void setCurrentHighestBet(int currentHighestBet) {
        this.currentHighestBet = currentHighestBet;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getCurrentPlayerIndex() {
        return playerManager.getCurrentPlayerIndex();
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        playerManager.setCurrentPlayerIndex(currentPlayerIndex);
    }

    public Player getCurrentPlayer() {
        return playerManager.getCurrentPlayer();
    }
}