package poker;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TableTest {

    private Table table;
    private Player p1;
    private Player p2;
    private Player p3;

    @BeforeEach
    void setUp() {
        table = new Table();
        // ゲーム開始時と同様に、最初の空のポットを追加しておく
        table.getPot().addSubPot(new Pot.SubPot());

        p1 = new HumanPlayer("Player1", 1000);
        p2 = new HumanPlayer("Player2", 1000);
        p3 = new HumanPlayer("Player3", 1000);

        table.addPlayer(p1);
        table.addPlayer(p2);
        table.addPlayer(p3);
    }

    @Test
    void testEndBettingRound_Simple() {
        // 全員が同じ額をベット（サイドポットなし）
        p1.bet(100);
        p2.bet(100);
        p3.bet(100);
        table.setCurrentHighestBet(100);

        table.endBettingRound();

        List<Pot.SubPot> subPots = table.getPot().getSubPots();
        assertEquals(1, subPots.size(), "サイドポットは発生しないはず");
        
        Pot.SubPot mainPot = subPots.get(0);
        assertEquals(300, mainPot.getAmount());
        assertEquals(3, mainPot.getEligiblePlayers().size());
        assertTrue(mainPot.getEligiblePlayers().contains(p1));
        assertTrue(mainPot.getEligiblePlayers().contains(p2));
        assertTrue(mainPot.getEligiblePlayers().contains(p3));

        // 状態のリセット確認
        assertEquals(0, table.getCurrentHighestBet());
        assertEquals(0, p1.getCurrentBet());
    }

    @Test
    void testEndBettingRound_OneAllIn() {
        // P1がショートスタックでオールイン
        p1.bet(100); // All-in
        p2.bet(300);
        p3.bet(300);

        table.endBettingRound();

        List<Pot.SubPot> subPots = table.getPot().getSubPots();
        assertEquals(2, subPots.size(), "サイドポットが発生するはず");

        // メインポット: 全員参加 (100 * 3 = 300)
        Pot.SubPot mainPot = subPots.get(0);
        assertEquals(300, mainPot.getAmount());
        assertTrue(mainPot.getEligiblePlayers().contains(p1));
        assertTrue(mainPot.getEligiblePlayers().contains(p2));
        assertTrue(mainPot.getEligiblePlayers().contains(p3));

        // サイドポット: P2とP3のみ ((300-100) * 2 = 400)
        Pot.SubPot sidePot = subPots.get(1);
        assertEquals(400, sidePot.getAmount());
        assertFalse(sidePot.getEligiblePlayers().contains(p1));
        assertTrue(sidePot.getEligiblePlayers().contains(p2));
        assertTrue(sidePot.getEligiblePlayers().contains(p3));
    }

    @Test
    void testEndBettingRound_MultipleAllIns() {
        Player p4 = new HumanPlayer("Player4", 1000);
        table.addPlayer(p4);

        // 異なる額でのオールインが複数発生
        p1.bet(50);  // All-in
        p2.bet(100); // All-in
        p3.bet(300);
        p4.bet(300);

        table.endBettingRound();

        List<Pot.SubPot> subPots = table.getPot().getSubPots();
        assertEquals(3, subPots.size());

        // Pot 1: 50 * 4 = 200 (全員)
        Pot.SubPot pot1 = subPots.get(0);
        assertEquals(200, pot1.getAmount());
        assertEquals(4, pot1.getEligiblePlayers().size());

        // Pot 2: (100 - 50) * 3 = 150 (P2, P3, P4)
        Pot.SubPot pot2 = subPots.get(1);
        assertEquals(150, pot2.getAmount());
        assertEquals(3, pot2.getEligiblePlayers().size());
        assertFalse(pot2.getEligiblePlayers().contains(p1));

        // Pot 3: (300 - 100) * 2 = 400 (P3, P4)
        Pot.SubPot pot3 = subPots.get(2);
        assertEquals(400, pot3.getAmount());
        assertEquals(2, pot3.getEligiblePlayers().size());
        assertFalse(pot3.getEligiblePlayers().contains(p1));
        assertFalse(pot3.getEligiblePlayers().contains(p2));
    }

    @Test
    void testEndBettingRound_WithFold() {
        Player p4 = new HumanPlayer("Player4", 1000);
        table.addPlayer(p4);

        // P1: 100 bet -> Fold
        // P2: 50 bet (All-in)
        // P3: 200 bet
        // P4: 200 bet
        
        p1.bet(100);
        p1.fold();
        p2.bet(50);
        p3.bet(200);
        p4.bet(200);
        
        table.endBettingRound();
        
        List<Pot.SubPot> subPots = table.getPot().getSubPots();
        assertEquals(2, subPots.size());
        
        // Pot 1 (Main): P2の50に合わせて計算。P1のチップも含まれるが、P1は権利なし。
        // 50 * 4 = 200. Eligible: P2, P3, P4
        assertEquals(200, subPots.get(0).getAmount());
        assertFalse(subPots.get(0).getEligiblePlayers().contains(p1));
        assertTrue(subPots.get(0).getEligiblePlayers().contains(p2));
        
        // Pot 2 (Side): 残りのチップ。P1の残り50もここに含まれる（デッドマネー）。
        // P1(50) + P3(150) + P4(150) = 350. Eligible: P3, P4
        assertEquals(350, subPots.get(1).getAmount());
        assertFalse(subPots.get(1).getEligiblePlayers().contains(p1));
        assertFalse(subPots.get(1).getEligiblePlayers().contains(p2));
        assertTrue(subPots.get(1).getEligiblePlayers().contains(p3));
        
        assertEquals(550, table.getPot().getTotalAmount());
    }

    @Test
    void testEndBettingRound_ShortStackAllIn() {
        // チップが少ないプレイヤーを追加
        Player pShort = new HumanPlayer("ShortStack", 50);
        table.addPlayer(pShort);

        // P1が100ベット
        p1.bet(100);
        table.setCurrentHighestBet(100);
        
        // ShortStackは50しか持っておらず、オールイン（相手のベット額より少ない）
        pShort.bet(50); 
        
        // P3は100コール
        p3.bet(100);
        
        // P2はフォールド（計算簡略化）
        p2.fold();
        
        table.endBettingRound();
        
        List<Pot.SubPot> subPots = table.getPot().getSubPots();
        assertEquals(2, subPots.size(), "サイドポットが発生するはず");
        
        // メインポット: 50 * 3 = 150 (P1, P3, ShortStack)
        Pot.SubPot mainPot = subPots.get(0);
        assertEquals(150, mainPot.getAmount());
        assertTrue(mainPot.getEligiblePlayers().contains(pShort));
        assertTrue(mainPot.getEligiblePlayers().contains(p1));
        assertTrue(mainPot.getEligiblePlayers().contains(p3));
        
        // サイドポット: (100 - 50) * 2 = 100 (P1, P3のみ)
        Pot.SubPot sidePot = subPots.get(1);
        assertEquals(100, sidePot.getAmount());
        assertFalse(sidePot.getEligiblePlayers().contains(pShort));
        assertTrue(sidePot.getEligiblePlayers().contains(p1));
        assertTrue(sidePot.getEligiblePlayers().contains(p3));
    }
}