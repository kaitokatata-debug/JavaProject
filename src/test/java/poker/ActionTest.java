package poker;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionTest {

    private TexasHoldemGame game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = new TexasHoldemGame();
        // テスト実行時のログ出力を抑制
        game.setLogger(new Logger() {
            @Override
            public void log(String message) { }
        });

        player = new HumanPlayer("TestPlayer", 1000);
        game.addPlayer(player);
        
        // ラウンドが即終了しないようにダミープレイヤーを追加
        game.addPlayer(new HumanPlayer("Dummy", 1000));
        
        // ポットの初期化（executeメソッド内でendBettingRoundが呼ばれた場合のエラー回避）
        game.getTable().getPot().addSubPot(new Pot.SubPot());
    }

    @Test
    void testValidateFold() {
        Action action = new Action(player, Action.Type.FOLD);
        assertTrue(action.validate(game), "フォールドは有効であるべき");
    }

    @Test
    void testValidateFoldWhenFolded() {
        player.fold();
        Action action = new Action(player, Action.Type.FOLD);
        assertFalse(action.validate(game), "既にフォールドしている場合はアクション無効");
    }

    @Test
    void testValidateCheck() {
        // ケース1: ベット額が釣り合っている場合 (0 == 0)
        Action action = new Action(player, Action.Type.CHECK);
        assertTrue(action.validate(game), "ベット額が釣り合っていればチェックは有効");

        // ケース2: 相手がベットしている場合
        game.setCurrentHighestBet(100);
        assertFalse(action.validate(game), "相手のベットがある場合チェックは無効");
    }

    @Test
    void testValidateCall() {
        // ケース1: 相手がベットしている場合
        game.setCurrentHighestBet(100);
        Action action = new Action(player, Action.Type.CALL);
        assertTrue(action.validate(game), "相手のベットがある場合コールは有効");

        // ケース2: ベット額が釣り合っている場合 (チェックすべき状況)
        game.setCurrentHighestBet(0);
        assertFalse(action.validate(game), "ベットがない場合コールは無効（チェックすべき）");
        
        // ケース3: 既に同額をベットしている場合
        game.setCurrentHighestBet(100);
        player.bet(100);
        assertFalse(action.validate(game), "既に同額をベットしている場合コールは無効");
    }

    @Test
    void testValidateBet() {
        // ケース1: 有効なベット
        Action action = new Action(player, Action.Type.BET, 100);
        assertTrue(action.validate(game), "所持チップ内のベットは有効");

        // ケース2: 0チップのベット
        Action actionZero = new Action(player, Action.Type.BET, 0);
        assertFalse(actionZero.validate(game), "0チップのベットは無効");

        // ケース3: 所持チップを超えるベット
        Action actionOver = new Action(player, Action.Type.BET, 1001);
        assertFalse(actionOver.validate(game), "所持チップを超えるベットは無効");
    }

    @Test
    void testValidateRaise() {
        game.setCurrentHighestBet(100);

        // ケース1: 有効なレイズ (合計ベット額 > 最高ベット額)
        // プレイヤーが200出す (Total 200 > 100)
        Action action = new Action(player, Action.Type.RAISE, 200);
        assertTrue(action.validate(game), "最高ベット額の2倍以上のレイズは有効");

        // ケース2: 無効なレイズ (合計ベット額 < 最高ベット額 * 2)
        // プレイヤーが150出す (Total 150 < 200) -> ミニマムレイズ未満
        Action actionInsufficient = new Action(player, Action.Type.RAISE, 150);
        assertFalse(actionInsufficient.validate(game), "最高ベット額の2倍未満のレイズは無効");

        // ケース3: 所持チップを超えるレイズ
        Action actionOver = new Action(player, Action.Type.RAISE, 1001);
        assertFalse(actionOver.validate(game), "所持チップを超えるレイズは無効");
    }
    
    @Test
    void testValidateRaiseWithPreviousBet() {
        // プレイヤーが既に50ベットしている状況
        player.bet(50);
        game.setCurrentHighestBet(100);
        
        // ケース1: 有効なレイズ (Total 200 >= 100 * 2)
        // 追加で150出す
        Action actionValid = new Action(player, Action.Type.RAISE, 150);
        assertTrue(actionValid.validate(game), "上乗せして最高ベット額の2倍以上になるなら有効");
        
        // ケース2: 無効なレイズ (Total 110 < 200)
        // 追加で60出す
        Action actionInvalid = new Action(player, Action.Type.RAISE, 60);
        assertFalse(actionInvalid.validate(game), "上乗せしても最高ベット額の2倍未満なら無効");
    }

    @Test
    void testValidateAllIn() {
        Action action = new Action(player, Action.Type.ALL_IN);
        assertTrue(action.validate(game), "チップを持っていればオールイン有効");

        Player brokePlayer = new HumanPlayer("Broke", 0);
        Action actionBroke = new Action(brokePlayer, Action.Type.ALL_IN);
        assertFalse(actionBroke.validate(game), "チップが0ならオールイン無効");
    }

    @Test
    void testValidateRaiseAllInException() {
        game.setCurrentHighestBet(100);
        
        // チップが少ないプレイヤー (150所持)
        Player shortStack = new HumanPlayer("ShortStack", 150);
        
        // ミニマムレイズは200だが、オールイン(150)なので例外的に有効
        Action action = new Action(shortStack, Action.Type.RAISE, 150);
        assertTrue(action.validate(game), "オールインの場合はミニマムレイズ額を下回っても有効");
    }

    @Test
    void testValidateRaiseAllInLessThanCurrentBet() {
        game.setCurrentHighestBet(100);
        
        // チップが非常に少ないプレイヤー (50所持)
        Player shortStack = new HumanPlayer("ShortStack", 50);
        
        // 相手のベット(100)より少ないが、オールイン(50)なので有効
        Action action = new Action(shortStack, Action.Type.RAISE, 50);
        assertTrue(action.validate(game), "相手のベット額を下回ってもオールインなら有効");
    }

    // --- executeメソッドのテスト ---

    @Test
    void testExecuteFold() {
        Action action = new Action(player, Action.Type.FOLD);
        action.execute(game);
        
        assertTrue(player.isFolded());
        assertEquals("Fold", player.getLastAction());
    }

    @Test
    void testExecuteCheck() {
        Action action = new Action(player, Action.Type.CHECK);
        action.execute(game);
        
        assertEquals("Check", player.getLastAction());
        assertEquals(0, player.getCurrentBet());
    }

    @Test
    void testExecuteCall() {
        game.setCurrentHighestBet(100);
        Action action = new Action(player, Action.Type.CALL);
        action.execute(game);
        
        assertEquals(900, player.getChips());
        assertEquals(100, player.getCurrentBet());
        assertEquals("Call", player.getLastAction());
    }

    @Test
    void testExecuteBet() {
        Action action = new Action(player, Action.Type.BET, 100);
        action.execute(game);
        
        assertEquals(900, player.getChips());
        assertEquals(100, player.getCurrentBet());
        assertEquals(100, game.getCurrentHighestBet());
        assertEquals("Bet 100", player.getLastAction());
    }

    @Test
    void testExecuteRaise() {
        game.setCurrentHighestBet(100);
        // 200レイズ（合計300ベット）ではなく、合計200になるようにレイズする場合
        Action action = new Action(player, Action.Type.RAISE, 200);
        action.execute(game);
        
        assertEquals(800, player.getChips());
        assertEquals(200, player.getCurrentBet());
        assertEquals(200, game.getCurrentHighestBet());
        assertEquals("Raise 200", player.getLastAction());
    }

    @Test
    void testExecuteAllIn() {
        Action action = new Action(player, Action.Type.ALL_IN);
        action.execute(game);
        
        assertEquals(0, player.getChips());
        assertEquals(1000, player.getCurrentBet());
        assertEquals(1000, game.getCurrentHighestBet());
        assertEquals("All In", player.getLastAction());
    }
}