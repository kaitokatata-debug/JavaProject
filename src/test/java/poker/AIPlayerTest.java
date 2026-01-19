package poker;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AIPlayerTest {

    private TexasHoldemGame game;
    private AIPlayer ai;

    @BeforeEach
    void setUp() {
        game = new TexasHoldemGame();
        // テスト実行時のログ出力を抑制
        game.setLogger(new Logger() {
            @Override
            public void log(String message) { }
        });

        ai = new AIPlayer("AI", 1000);
        game.addPlayer(ai);
        
        // ダミープレイヤーを追加（1人だけだとラウンドが即終了してベット額がリセットされるため）
        game.addPlayer(new HumanPlayer("Dummy", 1000));
        
        // ゲームの状態を初期化
        game.getTable().prepareForNewHand();
        game.getTable().getPot().addSubPot(new Pot.SubPot());
    }

    @Test
    void testIsBot() {
        assertTrue(ai.isBot(), "AIPlayerはisBotがtrueであるべき");
    }

    @Test
    void testPerformTurn_Check() {
        // 状況: ベット額が釣り合っている (0 vs 0)
        // 期待: チェック
        game.setCurrentHighestBet(0);
        
        ai.performTurn(game);
        
        assertEquals("Check", ai.getLastAction());
        assertEquals(0, ai.getCurrentBet());
    }

    @Test
    void testPerformTurn_Call() {
        // 状況: 相手がベットしている (HighestBet: 100)
        game.setCurrentHighestBet(100);
        
        // 期待: コール (100チップ支払う)
        ai.performTurn(game);
        
        assertEquals("Call", ai.getLastAction());
        assertEquals(100, ai.getCurrentBet());
        assertEquals(900, ai.getChips());
    }

    @Test
    void testPerformTurn_AllIn_ShortStack() {
        // 状況: 相手のベット額(2000)が自分の所持チップ(1000)より多い
        game.setCurrentHighestBet(2000);
        
        // 期待: オールイン (所持チップ1000全て出す)
        ai.performTurn(game);
        
        assertEquals("All In", ai.getLastAction());
        assertEquals(1000, ai.getCurrentBet());
        assertEquals(0, ai.getChips());
    }

    @Test
    void testPerformTurn_AllIn_ExactAmount() {
        // 状況: 相手のベット額(1000)が自分の所持チップ(1000)とちょうど同じ
        game.setCurrentHighestBet(1000);
        
        // 期待: オールイン扱い (ロジック上、チップが足りるかちょうどならAllInメソッドを呼んでいるため)
        // ※Actionクラスの実装により、全額ベットはALL_INとしてログ出力される
        ai.performTurn(game);
        
        assertEquals("All In", ai.getLastAction());
        assertEquals(1000, ai.getCurrentBet());
        assertEquals(0, ai.getChips());
    }
}