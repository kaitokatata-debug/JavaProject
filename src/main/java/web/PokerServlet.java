package web;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import poker.AIPlayer;
import poker.Action;
import poker.HumanPlayer;
import poker.Player;
import poker.State;
import poker.TexasHoldemGame;

/**
 * ポーカーゲームのWebインターフェースを提供するサーブレット。
 * ゲームの進行状況の表示（GET）と、プレイヤーのアクション処理（POST）を行います。
 * 
 * <p>POSTリクエストで受け付ける主なアクション（actionパラメータ）:</p>
 * <ul>
 *   <li>{@code bet}: 指定された額をベットします（amountパラメータが必要）。</li>
 *   <li>{@code call}: 現在の最高ベット額に合わせてコール（またはチェック）します。</li>
 *   <li>{@code fold}: ゲームから降ります。</li>
 *   <li>{@code allin}: 全チップを賭けます。</li>
 *   <li>{@code next}: 次のラウンド（ハンド）を開始します。</li>
 *   <li>{@code reset}: ゲームをリセットして初期状態に戻します。</li>
 * </ul>
 */
public class PokerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        TexasHoldemGame game = (TexasHoldemGame) session.getAttribute("pokerGame");
        
        // ゲームがまだなければ初期化
        if (game == null) {
            game = new TexasHoldemGame();
            String username = (String) session.getAttribute("username");
            if (username == null) username = "Player";
            
            // プレイヤー作成 (Human vs CPU)
            game.addPlayer(new HumanPlayer(username, 1000));
            game.addPlayer(new AIPlayer("CPU", 1000));
            
            session.setAttribute("pokerGame", game);
            
            game.startNewRound();
            checkCpuTurn(session, game);
        }

        // CPUのターンであれば自動でアクションを実行
        Boolean isCpuTurn = (Boolean) session.getAttribute("isCpuTurn");
        if (isCpuTurn != null && isCpuTurn) {
            game.processAiTurn(game.getTable().getCurrentPlayer());
            checkCpuTurn(session, game); // アクション後の状態を再チェック
        }

        // エラーメッセージの処理（Flash Scope）
        String error = (String) session.getAttribute("error");
        if (error != null) {
            req.setAttribute("error", error);
            session.removeAttribute("error");
        }

        req.getRequestDispatcher("poker.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        TexasHoldemGame game = (TexasHoldemGame) session.getAttribute("pokerGame");

        if (game == null) {
            resp.sendRedirect("poker");
            return;
        }

        String action = req.getParameter("action");

        if ("reset".equals(action)) {
            game.resetGame();
            checkCpuTurn(session, game);
            resp.sendRedirect("poker");
            return;
        }
        
        if ("next".equals(action)) {
            handleNextRound(session, resp, game);
            return;
        }

        if ("auto_advance".equals(action)) {
            if (game.getState() != State.SHOWDOWN) {
                game.advanceState();
            }
            resp.sendRedirect("poker");
            return;
        }

        if (game.getState() != State.SHOWDOWN) {
            if (!processPlayerAction(req, session, game, action)) {
                resp.sendRedirect("poker");
                return;
            }
        }

        checkCpuTurn(session, game);
        resp.sendRedirect("poker");
    }

    private void handleNextRound(HttpSession session, HttpServletResponse resp, TexasHoldemGame game) throws IOException {
        Player human = game.getPlayers().get(0);
        Player cpu = game.getPlayers().get(1);

        // どちらかのチップが0になったらゲーム終了
        if (human.getChips() == 0 || cpu.getChips() == 0) {
            String resultMessage = (human.getChips() > 0) ? "You Win!" : "Game Over";
            session.setAttribute("gameResult", resultMessage);
            if (human.getChips() > 0) {
                saveScore(human.getName(), human.getChips(), session);
            }

            session.removeAttribute("pokerGame");
            resp.sendRedirect("result.jsp");
        } else {
            // 次のラウンドへ
            game.startNewRound();
            checkCpuTurn(session, game);
            resp.sendRedirect("poker");
        }
    }

    private boolean processPlayerAction(HttpServletRequest req, HttpSession session, TexasHoldemGame game, String action) {
        Player human = game.getPlayers().get(0);
        Action playerAction = null;

        if ("fold".equals(action)) {
            playerAction = new Action(human, Action.Type.FOLD);
        } else if ("call".equals(action)) {
            int diff = game.getCurrentHighestBet() - human.getCurrentBet();
            Action.Type type = (diff > 0) ? Action.Type.CALL : Action.Type.CHECK;
            playerAction = new Action(human, type);
        } else if ("bet".equals(action)) {
            int amount = 0;
            try {
                amount = Integer.parseInt(req.getParameter("amount"));
            } catch (NumberFormatException e) {
                // 無効な数値
            }
            
            if (amount >= human.getChips()) {
                playerAction = new Action(human, Action.Type.ALL_IN);
            } else {
                Action.Type type = (game.getCurrentHighestBet() > 0) ? Action.Type.RAISE : Action.Type.BET;
                playerAction = new Action(human, type, amount);
            }
        } else if ("allin".equals(action)) {
            playerAction = new Action(human, Action.Type.ALL_IN);
        }

        if (playerAction != null) {
            if (!playerAction.validate(game)) {
                session.setAttribute("error", "無効なアクションです（チップ不足、またはベット額が不正です）");
                return false;
            }
            playerAction.execute(game);
        }
        return true;
    }

    private void checkCpuTurn(HttpSession session, TexasHoldemGame game) {
        if (game.getState() != State.SHOWDOWN && game.getTable().getCurrentPlayer() != null && game.getTable().getCurrentPlayer().isBot()) {
            session.setAttribute("isCpuTurn", true);
        } else {
            session.removeAttribute("isCpuTurn");
        }
    }

    private void saveScore(String name, int score, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        ScoreRepository repository = new ScoreRepository();
        repository.saveScore(name, score, userId, "Poker");
    }
}
