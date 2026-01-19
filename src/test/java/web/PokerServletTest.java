package web;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import poker.AIPlayer;
import poker.HumanPlayer;
import poker.Player;
import poker.TexasHoldemGame;

class PokerServletTest {

    private PokerServlet servlet;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        servlet = new PokerServlet();
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(req.getSession()).thenReturn(session);
        when(req.getRequestDispatcher(any(String.class))).thenReturn(dispatcher);
    }

    @Test
    void testDoGet_InitializesGame() throws Exception {
        // セッションにゲームが存在しない場合
        when(session.getAttribute("pokerGame")).thenReturn(null);
        when(session.getAttribute("username")).thenReturn("TestUser");

        servlet.doGet(req, resp);

        // ゲームが初期化され、セッションに保存されることを検証
        verify(session).setAttribute(eq("pokerGame"), any(TexasHoldemGame.class));
        // JSPへフォワードされることを検証
        verify(req).getRequestDispatcher("poker.jsp");
        verify(dispatcher).forward(req, resp);
    }

    @Test
    void testDoPost_FoldAction() throws Exception {
        // ゲーム状態のセットアップ
        TexasHoldemGame game = new TexasHoldemGame();
        Player human = new HumanPlayer("Human", 1000);
        Player cpu = new AIPlayer("CPU", 1000);
        game.addPlayer(human);
        game.addPlayer(cpu);
        game.startNewRound();

        when(session.getAttribute("pokerGame")).thenReturn(game);
        when(req.getParameter("action")).thenReturn("fold");

        servlet.doPost(req, resp);

        // プレイヤーがフォールドしたか検証
        assertTrue(human.isFolded(), "Player should be folded after fold action");
        // リダイレクト検証
        verify(resp).sendRedirect("poker");
    }

    @Test
    void testDoPost_BetAction() throws Exception {
        TexasHoldemGame game = new TexasHoldemGame();
        Player human = new HumanPlayer("Human", 1000);
        Player cpu = new AIPlayer("CPU", 1000);
        game.addPlayer(human);
        game.addPlayer(cpu);
        game.startNewRound();

        int initialChips = human.getChips();

        when(session.getAttribute("pokerGame")).thenReturn(game);
        when(req.getParameter("action")).thenReturn("bet");
        when(req.getParameter("amount")).thenReturn("100");

        servlet.doPost(req, resp);

        // チップが減っていることを検証（ベットが成立）
        assertTrue(human.getChips() < initialChips, "Chips should decrease after betting");
        verify(resp).sendRedirect("poker");
    }

    @Test
    void testDoPost_ResetAction() throws Exception {
        TexasHoldemGame game = new TexasHoldemGame();
        game.addPlayer(new HumanPlayer("Human", 1000));
        game.addPlayer(new AIPlayer("CPU", 1000));
        
        when(session.getAttribute("pokerGame")).thenReturn(game);
        when(req.getParameter("action")).thenReturn("reset");

        servlet.doPost(req, resp);

        // リダイレクト検証
        verify(resp).sendRedirect("poker");
        // ゲームの状態がリセットされているか（例：ポットが0）
        assertEquals(0, game.getTable().getPot().getTotalAmount(), "Pot should be empty after reset");
    }
}