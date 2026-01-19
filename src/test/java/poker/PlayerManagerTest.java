package poker;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerManagerTest {

    private PlayerManager playerManager;
    private Player p1;
    private Player p2;
    private Player p3;

    @BeforeEach
    void setUp() {
        playerManager = new PlayerManager();
        p1 = new HumanPlayer("P1", 100);
        p2 = new HumanPlayer("P2", 100);
        p3 = new HumanPlayer("P3", 100);
        playerManager.addPlayer(p1);
        playerManager.addPlayer(p2);
        playerManager.addPlayer(p3);
    }

    @Test
    void testMoveDealerButton_AllActive() {
        // 初期状態: -1
        assertEquals(-1, playerManager.getDealerButtonPosition());

        // 1回目: 0番目のプレイヤーへ
        playerManager.moveDealerButton();
        assertEquals(0, playerManager.getDealerButtonPosition());

        // 2回目: 1番目のプレイヤーへ
        playerManager.moveDealerButton();
        assertEquals(1, playerManager.getDealerButtonPosition());

        // 3回目: 2番目のプレイヤーへ
        playerManager.moveDealerButton();
        assertEquals(2, playerManager.getDealerButtonPosition());

        // 4回目: 0番目のプレイヤーへ戻る
        playerManager.moveDealerButton();
        assertEquals(0, playerManager.getDealerButtonPosition());
    }

    @Test
    void testMoveDealerButton_SkipPlayerWithNoChips() {
        // P2のチップを0にする
        p2.bet(100);
        assertEquals(0, p2.getChips());

        // 1回目: 0番目 (P1)
        playerManager.moveDealerButton();
        assertEquals(0, playerManager.getDealerButtonPosition());

        // 2回目: 1番目 (P2) はチップがないのでスキップして 2番目 (P3) へ
        playerManager.moveDealerButton();
        assertEquals(2, playerManager.getDealerButtonPosition());
    }

    @Test
    void testNextTurn_SimpleRotation() {
        // ディーラーボタンを0番目に設定
        playerManager.moveDealerButton();
        assertEquals(0, playerManager.getDealerButtonPosition());

        // 最初のターン: ディーラー(0)の次は1
        playerManager.nextTurn();
        assertEquals(1, playerManager.getCurrentPlayerIndex());
        assertEquals(p2, playerManager.getCurrentPlayer());

        // 次のターン: 2
        playerManager.nextTurn();
        assertEquals(2, playerManager.getCurrentPlayerIndex());
        assertEquals(p3, playerManager.getCurrentPlayer());

        // 次のターン: 0
        playerManager.nextTurn();
        assertEquals(0, playerManager.getCurrentPlayerIndex());
        assertEquals(p1, playerManager.getCurrentPlayer());
    }

    @Test
    void testNextTurn_SkipFoldedPlayer() {
        playerManager.moveDealerButton(); // Dealer = 0
        
        // P2がフォールド
        p2.fold();

        // 最初のターン: ディーラー(0)の次は1だが、P2はフォールドしているのでスキップして2 (P3)
        playerManager.nextTurn();
        assertEquals(2, playerManager.getCurrentPlayerIndex());
        assertEquals(p3, playerManager.getCurrentPlayer());
    }

    @Test
    void testNextTurn_SkipAllInPlayer() {
        playerManager.moveDealerButton(); // Dealer = 0
        
        // P2がオールイン（チップ0）
        p2.bet(100);

        // 最初のターン: ディーラー(0)の次は1だが、P2はチップがないのでスキップして2 (P3)
        playerManager.nextTurn();
        assertEquals(2, playerManager.getCurrentPlayerIndex());
        assertEquals(p3, playerManager.getCurrentPlayer());
    }

    @Test
    void testGetActivePlayers() {
        assertEquals(3, playerManager.getActivePlayers().size());

        p1.fold();
        assertEquals(2, playerManager.getActivePlayers().size());
        assertFalse(playerManager.getActivePlayers().contains(p1));
        assertTrue(playerManager.getActivePlayers().contains(p2));
        assertTrue(playerManager.getActivePlayers().contains(p3));
    }
    
    @Test
    void testReset() {
        playerManager.moveDealerButton();
        assertEquals(0, playerManager.getDealerButtonPosition());
        
        playerManager.reset();
        assertEquals(-1, playerManager.getDealerButtonPosition());
    }
}