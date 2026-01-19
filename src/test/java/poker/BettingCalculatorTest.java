package poker;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BettingCalculatorTest {

    private Table table;
    private BettingCalculator calculator;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp() {
        table = new Table();
        // ポットの初期化（メインポットを追加）
        table.getPot().addSubPot(new Pot.SubPot());
        
        p1 = new HumanPlayer("P1", 1000);
        p2 = new HumanPlayer("P2", 1000);
        
        table.addPlayer(p1);
        table.addPlayer(p2);
        
        calculator = new BettingCalculator(table);
    }

    @Test
    void testCalculate_InitialState() {
        // 状況: 誰もベットしていない
        table.setCurrentHighestBet(0);
        
        BettingOptions opts = calculator.calculate(p1);
        
        assertEquals(10, opts.getMinAmount(), "初期状態のミニマムベットは10");
        assertEquals(1000, opts.getMaxAmount());
        assertEquals(0, opts.getCallAmount());
    }

    @Test
    void testCalculate_FacingBet() {
        // 状況: P2が100ベット
        p2.bet(100);
        table.setCurrentHighestBet(100);
        
        BettingOptions opts = calculator.calculate(p1);
        
        // ミニマムレイズ額: 現在の最高ベット(100) * 2 = 200
        // プレイヤーのベットは0なので、追加で支払う額は200
        assertEquals(200, opts.getMinAmount());
        assertEquals(100, opts.getCallAmount());
        
        // ポット額計算
        // 現在のポット総額(100) + コール額(100) + レイズ分(100) = 300
        assertEquals(300, opts.getPotAmount());
    }

    @Test
    void testCalculate_FacingRaise() {
        // 状況: P2が200ベット（レイズ）
        p2.bet(200);
        table.setCurrentHighestBet(200);
        
        BettingOptions opts = calculator.calculate(p1);
        
        // ミニマムレイズ額: 200 * 2 = 400
        assertEquals(400, opts.getMinAmount());
        assertEquals(200, opts.getCallAmount());
    }

    @Test
    void testCalculate_ReRaise() {
        // 状況: P1が100ベット、P2が200にレイズ
        p1.bet(100);
        p2.bet(200);
        table.setCurrentHighestBet(200);
        
        // P1の手番
        BettingOptions opts = calculator.calculate(p1);
        
        // ミニマムレイズ額: 200 * 2 = 400
        // P1は既に100出しているので、追加で必要な額は 400 - 100 = 300
        assertEquals(300, opts.getMinAmount());
        assertEquals(100, opts.getCallAmount()); // 200 - 100
    }

    @Test
    void testCalculate_ShortStack() {
        // 状況: チップが少ないプレイヤー
        Table tableShort = new Table();
        tableShort.getPot().addSubPot(new Pot.SubPot());
        Player shortStack = new HumanPlayer("Short", 50);
        Player rich = new HumanPlayer("Rich", 1000);
        tableShort.addPlayer(shortStack);
        tableShort.addPlayer(rich);
        
        BettingCalculator calcShort = new BettingCalculator(tableShort);
        
        // Richが100ベット
        rich.bet(100);
        tableShort.setCurrentHighestBet(100);
        
        BettingOptions opts = calcShort.calculate(shortStack);
        
        // 本来のミニマムレイズは200だが、所持金が50しかないので50（オールイン）になる
        assertEquals(50, opts.getMinAmount());
        assertEquals(50, opts.getMaxAmount());
    }
}