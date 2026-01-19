package poker;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import playingcards.Card;
import playingcards.Card.Rank;
import playingcards.Card.Suit;

class TexasHoldemGameTest {

    private TexasHoldemGame game;
    private Player p1, p2, p3;

    @BeforeEach
    void setUp() {
        game = new TexasHoldemGame();
        // テスト実行中にコンソールへログが出力されないように、何もしないLoggerに差し替える
        game.setLogger(new Logger() {
            @Override
            public void log(String message) {
                // Do nothing
            }
        });

        // プレイヤーリストをクリアして状態をリセット
        game.getPlayers().clear();

        p1 = new HumanPlayer("Player1", 500);
        p2 = new HumanPlayer("Player2", 500);
        p3 = new HumanPlayer("Player3", 500);

        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        
        // 注意: setUpでは prepareForNewHand() を呼ばないように変更。
        // これにより、各テストメソッドで startNewRound() を呼んだときの
        // ディーラーボタンの位置などが予測しやすくなります。
        // 必要に応じて各テスト内で prepareForNewHand() を呼ぶか、startNewRound() を使用してください。
    }

    @Test
    void testShowdown_SidePot_ShortStackWinsMainPot() {
        // --- シナリオ ---
        // P1(ショートスタック)がオールインし、P2とP3がコール後さらにベットを続ける。
        // ハンドの強さ: P1 > P2 > P3
        // 期待結果: P1がメインポットを獲得、P2がサイドポットを獲得する。

        // --- 準備 ---
        // 初期チップ: P1(100), P2(500), P3(500)
        p1 = new HumanPlayer("Player1", 100);
        p2 = new HumanPlayer("Player2", 500);
        p3 = new HumanPlayer("Player3", 500);
        game.getPlayers().clear();
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        game.getTable().prepareForNewHand();
        game.getTable().getPot().addSubPot(new Pot.SubPot());

        // ベット -> メインポット:300 (P1,P2,P3), サイドポット:400 (P2,P3)
        p1.bet(100); // All-in
        p2.bet(300);
        p3.bet(300);
        game.endBettingRound();

        // ハンド設定
        // P1: Four of a Kind (A)
        p1.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        p1.addCard(Card.valueOf(Suit.HEARTS, Rank.ACE));
        // P2: Full House (K over A)
        p2.addCard(Card.valueOf(Suit.SPADES, Rank.KING));
        p2.addCard(Card.valueOf(Suit.HEARTS, Rank.KING));
        // P3: One Pair (A)
        p3.addCard(Card.valueOf(Suit.SPADES, Rank.TEN));
        p3.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));
        
        // コミュニティカード
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.DIAMONDS, Rank.ACE));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.CLUBS, Rank.ACE));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.CLUBS, Rank.TWO));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.SPADES, Rank.SEVEN));

        // --- 実行 ---
        game.executeShowdown();

        // --- 検証 ---
        // ベット後チップ: P1(0), P2(200), P3(200)
        // P1はメインポット(300)獲得 -> 最終チップ: 300
        // P2はサイドポット(400)獲得 -> 最終チップ: 200 + 400 = 600
        // P3は何も獲得せず -> 最終チップ: 200
        assertEquals(300, p1.getChips());
        assertEquals(600, p2.getChips());
        assertEquals(200, p3.getChips());
    }

    @Test
    void testShowdown_SidePot_OnePlayerWinsAll() {
        // --- シナリオ ---
        // P1(ショートスタック)がオールインし、P2とP3がコール後さらにベットを続ける。
        // ハンドの強さ: P2 > P1 > P3
        // 期待結果: P2がメインポットとサイドポットの両方を獲得する。

        // --- 準備 ---
        p1 = new HumanPlayer("Player1", 100);
        p2 = new HumanPlayer("Player2", 500);
        p3 = new HumanPlayer("Player3", 500);
        game.getPlayers().clear();
        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        game.getTable().prepareForNewHand();
        game.getTable().getPot().addSubPot(new Pot.SubPot());

        // ベット -> メインポット:300 (P1,P2,P3), サイドポット:400 (P2,P3)
        p1.bet(100); // All-in
        p2.bet(300);
        p3.bet(300);
        game.endBettingRound();

        // ハンド設定
        // P1: Full House
        p1.addCard(Card.valueOf(Suit.SPADES, Rank.KING));
        p1.addCard(Card.valueOf(Suit.HEARTS, Rank.KING));
        // P2: Four of a Kind (A)
        p2.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        p2.addCard(Card.valueOf(Suit.HEARTS, Rank.ACE));
        // P3: One Pair (A)
        p3.addCard(Card.valueOf(Suit.SPADES, Rank.TEN));
        p3.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));
        
        // コミュニティカード
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.DIAMONDS, Rank.ACE));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.CLUBS, Rank.ACE));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.CLUBS, Rank.TWO));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.SPADES, Rank.SEVEN));

        // --- 実行 ---
        game.executeShowdown();

        // --- 検証 ---
        // ベット後チップ: P1(0), P2(200), P3(200)
        // P2は全ポット(700)獲得 -> 最終チップ: 200 + 700 = 900
        assertEquals(0, p1.getChips());
        assertEquals(900, p2.getChips());
        assertEquals(200, p3.getChips());
    }

    @Test
    void testShowdown_SplitPot() {
        // --- シナリオ ---
        // P1とP2が全く同じ強さのハンドで、P3がそれより弱い。
        // 期待結果: P1とP2がポットを均等に分け合う。

        // --- 準備 ---
        // ポットの初期化
        game.getTable().getPot().addSubPot(new Pot.SubPot());

        // ベット -> ポット: 300
        p1.bet(100);
        p2.bet(100);
        p3.bet(100);
        game.endBettingRound();

        // ハンド設定
        // P1: A kicker (Two Pair K,K,Q,Q with A)
        p1.addCard(Card.valueOf(Suit.SPADES, Rank.ACE));
        p1.addCard(Card.valueOf(Suit.HEARTS, Rank.TWO));
        // P2: A kicker (Two Pair K,K,Q,Q with A)
        p2.addCard(Card.valueOf(Suit.DIAMONDS, Rank.ACE));
        p2.addCard(Card.valueOf(Suit.CLUBS, Rank.THREE));
        // P3: 9 pair (Counterfeited by board, plays board K,K,Q,Q,T)
        p3.addCard(Card.valueOf(Suit.SPADES, Rank.NINE));
        p3.addCard(Card.valueOf(Suit.HEARTS, Rank.NINE));
        
        // コミュニティカード: K, K, Q, Q, T
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.DIAMONDS, Rank.KING));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.CLUBS, Rank.KING));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.DIAMONDS, Rank.QUEEN));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.CLUBS, Rank.QUEEN));
        game.getTable().getCommunityCards().addCard(Card.valueOf(Suit.SPADES, Rank.TEN));

        // --- 実行 ---
        game.executeShowdown();

        // --- 検証 ---
        // ベット後チップ: 各400
        // ポット300をP1とP2で分け合う (150ずつ)
        // P1最終: 400 + 150 = 550
        // P2最終: 400 + 150 = 550
        // P3最終: 400
        assertEquals(550, p1.getChips());
        assertEquals(550, p2.getChips());
        assertEquals(400, p3.getChips());
    }

    @Test
    void testShowdown_Walkover() {
        // --- シナリオ ---
        // P1がベットし、他の全員がフォールドする。
        // 期待結果: P1が不戦勝でポットを獲得する。

        // --- 準備 ---
        // ポットの初期化
        game.getTable().getPot().addSubPot(new Pot.SubPot());

        p1.bet(100);
        p2.fold();
        p3.fold();
        game.endBettingRound(); // Pot: 100

        // --- 実行 ---
        game.executeShowdown();

        // --- 検証 ---
        // ベット後チップ: P1(400), P2(500), P3(500)
        // P1がポット(100)獲得 -> 最終チップ: 400 + 100 = 500
        assertEquals(500, p1.getChips());
        assertEquals(500, p2.getChips());
        assertEquals(500, p3.getChips());
    }

    @Test
    void testWalkover_DoesNotAdvanceState() {
        // --- シナリオ ---
        // プリフロップでP2がベットし、他の全員がフォールドする。
        // 期待結果: フロップは配られず、即座にP2の勝利でラウンドが終了する。

        // --- 準備 ---
        game.startNewRound(); // Dealer: P1, Start: P2
        assertEquals(State.PREFLOP, game.getState());

        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);
        Player p3 = game.getPlayers().get(2);
        
        long initialChipsP2 = p2.getChips();

        // --- 実行 ---
        p2.doBet(game, 50); // P2 bets 50
        p3.doFold(game);    // P3 folds
        p1.doFold(game);    // P1 folds

        // --- 検証 ---
        // P1のフォールドアクションにより、勝者が決まりショーダウン状態になる
        assertEquals(State.SHOWDOWN, game.getState(), "不戦勝の場合は即座にSHOWDOWNになるべき");
        assertEquals(0, game.getCommunityCards().size(), "不戦勝の場合、次のコミュニティカードは配られない");
        assertEquals(initialChipsP2, p2.getChips(), "ポットは勝者(P2)に渡される");
    }

    @Test
    void testGameProgression_PreflopToFlop() {
        // setUpで prepareForNewHand を呼ばなくなったため、
        // startNewRound で初めて呼ばれる -> Dealer = 0 (P1)
        // スタートプレイヤーは Dealer(P1) の次の P2
        game.startNewRound();
        
        assertEquals(State.PREFLOP, game.getState());
        assertEquals(0, game.getCommunityCards().size());
        
        // 1人目 (P2)
        Player currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(p2, currentPlayer);
        
        currentPlayer.doCheck(game);
        
        // 2人目 (P3)
        currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(p3, currentPlayer);
        
        currentPlayer.doCheck(game);
        
        // 3人目 (P1 - Dealer)
        currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(p1, currentPlayer);
        
        currentPlayer.doCheck(game);
        
        // 全員アクション完了 -> FLOPへ遷移しているはず
        assertEquals(State.FLOP, game.getState());
        assertEquals(3, game.getCommunityCards().size());
        
        // FLOPの最初のプレイヤーはDealer(P1)の次 (P2)
        assertEquals(p2, game.getTable().getCurrentPlayer());
    }

    @Test
    void testResetGame() {
        // ゲーム進行させてチップを変動させる
        // テスト用にハンド準備
        game.getTable().prepareForNewHand();
        game.getTable().getPot().addSubPot(new Pot.SubPot());

        p1.bet(100);
        p2.bet(200);
        p3.winChips(300);
        
        assertNotEquals(500, p1.getChips());
        assertNotEquals(500, p2.getChips());
        assertNotEquals(500, p3.getChips());
        
        // リセット実行
        game.resetGame();
        
        // 検証
        assertEquals(500, p1.getChips(), "P1のチップが初期値に戻っていること");
        assertEquals(500, p2.getChips(), "P2のチップが初期値に戻っていること");
        assertEquals(500, p3.getChips(), "P3のチップが初期値に戻っていること");
        
        // ゲーム状態の確認（新しいラウンドが始まっているか）
        assertEquals(State.PREFLOP, game.getState());
        assertEquals(0, game.getPot(), "ポットがリセットされていること");
    }

    @Test
    void testHumanVsAIProgression() {
        // 既存のプレイヤーをクリアして、HumanとAIの1対1にする
        game.getPlayers().clear();
        Player human = new HumanPlayer("Human", 1000);
        Player ai = new AIPlayer("AI", 1000);
        game.addPlayer(human);
        game.addPlayer(ai);
        
        // ゲーム開始
        // startNewRound -> Dealer = 0 (Human)
        // スタートプレイヤーは Dealer(0) の次の 1 (AI)
        game.startNewRound();
        
        assertEquals(State.PREFLOP, game.getState());
        assertEquals(ai, game.getTable().getCurrentPlayer());
        
        // AI: Check
        ((AIPlayer)ai).performTurn(game);
        
        // 次はHumanのターン
        assertEquals(human, game.getTable().getCurrentPlayer());
        
        // Human: Check
        human.doCheck(game);
        
        // 全員チェック -> FLOP
        assertEquals(State.FLOP, game.getState());
        
        // FLOP開始 (Dealer=Humanの次 -> AI)
        assertEquals(ai, game.getTable().getCurrentPlayer());
        
        // AI: Check
        ((AIPlayer)ai).performTurn(game);
        
        // Human: Bet 100
        human.doBet(game, 100);
        
        // AI: Call
        ((AIPlayer)ai).performTurn(game);
        
        // TURNへ
        assertEquals(State.TURN, game.getState());
        // Dealer(Human)の次はAI
        assertEquals(ai, game.getTable().getCurrentPlayer());
    }

    @Test
    void testGetBettingOptions() {
        // --- 準備 ---
        game.startNewRound(); // Dealer=0(P1), Start=P2
        Player currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(p2, currentPlayer);

        // 1. 初期状態 (誰もベットしていない)
        // currentHighest = 0, minRaiseTotal = 10, myBet = 0 -> minAmount = 10
        BettingOptions opts = game.getBettingOptions(currentPlayer);
        assertEquals(10, opts.getMinAmount(), "初期状態のミニマムベットは10");

        // P2が100ベット
        p2.doBet(game, 100);

        // 次はP3
        currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(p3, currentPlayer);

        // 2. 相手がベットした場合
        // currentHighest = 100, minRaiseTotal = 200, myBet = 0 -> minAmount = 200
        opts = game.getBettingOptions(currentPlayer);
        assertEquals(200, opts.getMinAmount(), "相手が100ベットした場合、ミニマムレイズ額は200");

        // P3が200レイズ (Total 200)
        p3.doRaise(game, 200);

        // 次はP1
        currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(p1, currentPlayer);

        // 3. レイズされた場合
        // currentHighest = 200, minRaiseTotal = 400, myBet = 0 -> minAmount = 400
        opts = game.getBettingOptions(currentPlayer);
        assertEquals(400, opts.getMinAmount(), "相手が200レイズした場合、次のミニマムレイズ額は400");

        // P1フォールド
        p1.doFold(game);

        // 次はP2 (既に100ベットしている)
        currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(p2, currentPlayer);

        // 4. 既にベットしている場合
        // currentHighest = 200, minRaiseTotal = 400, myBet = 100 -> minAmount = 300
        opts = game.getBettingOptions(currentPlayer);
        assertEquals(300, opts.getMinAmount(), "既にベットしている場合、差額分がミニマムレイズ額になる");
    }

    @Test
    void testGetBettingOptions_ShortStack() {
        // ショートスタックのケース
        game.getPlayers().clear();
        Player rich = new HumanPlayer("Rich", 1000);
        Player poor = new HumanPlayer("Poor", 50);
        game.addPlayer(rich);
        game.addPlayer(poor);
        
        game.startNewRound(); // Dealer=0(Rich), Start=1(Poor)
        
        // Poorのターン (Start)
        Player currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(poor, currentPlayer);
        
        // Poor checks
        poor.doCheck(game);
        
        // Rich bets 100
        rich.doBet(game, 100);
        
        // Poor's turn again
        currentPlayer = game.getTable().getCurrentPlayer();
        assertEquals(poor, currentPlayer);
        
        // currentHighest=100. minRaiseTotal=200. myBet=0. minAmount calculated=200.
        // But Poor only has 50.
        // minAmount should be capped at maxAmount (50).
        BettingOptions opts = game.getBettingOptions(poor);
        assertEquals(50, opts.getMinAmount(), "所持金が不足している場合、ミニマムレイズ額は所持金全額（オールイン）になる");
        assertEquals(50, opts.getMaxAmount());
    }
}