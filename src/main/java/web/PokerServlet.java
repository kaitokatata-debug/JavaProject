package web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import poker.Action;
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
            game.addPlayer(new Player(username, 1000));
            game.addPlayer(new Player("CPU", 1000));
            
            session.setAttribute("pokerGame", game);
            
            game.startNewRound();
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
        Player human = game.getPlayers().get(0);
        Player cpu = game.getPlayers().get(1);

        if ("reset".equals(action)) {
            // ゲームリセット
            session.removeAttribute("pokerGame");
            resp.sendRedirect("poker");
            return;
        }
        
        if ("next".equals(action)) {
            // どちらかのチップが0になったらゲーム終了（強制終了）
            if (human.getChips() == 0 || cpu.getChips() == 0) {
                String resultMessage = (human.getChips() > 0) ? "You Win!" : "Game Over";
                session.setAttribute("gameResult", resultMessage);
                if (human.getChips() > 0) {
                    saveScore(human.getName(), human.getChips(), session);
                }

                session.removeAttribute("pokerGame");
                resp.sendRedirect("result.jsp");
                return;
            }

            // 次のラウンドへ（ショーダウン後）
            game.startNewRound();
            resp.sendRedirect("poker");
            return;
        }

        // 自動進行アクション（オールイン時の遅延実行用）
        if ("auto_advance".equals(action)) {
            if (game.getState() != State.SHOWDOWN) {
                game.advanceState();
            }
            resp.sendRedirect("poker");
            return;
        }

        // CPUのターン処理（JSPからの遅延リクエストで実行）
        if ("cpu_turn".equals(action)) {
            // CPUのアクション実行
            if (!human.isFolded() && game.getState() != State.SHOWDOWN) {
                cpu.doCall(game);
            }

            // ベッティングラウンド終了判定
            if (!human.isFolded()) {
                game.endBettingRound();
                // 2人以上残っている場合のみ次のストリートへ
                if (game.getTable().getActivePlayers().size() > 1) {
                    if (game.getState() != State.SHOWDOWN) {
                        game.advanceState();
                    }
                }
            }
            session.removeAttribute("isCpuTurn");
            resp.sendRedirect("poker");
            return;
        }

        // プレイヤーのアクション処理
        if (game.getState() != State.SHOWDOWN) {
            Action playerAction = null;

            if ("fold".equals(action)) {
                playerAction = new Action(human, Action.Type.FOLD);
            } else if ("call".equals(action)) {
                // コールかチェックかを判定
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
                    resp.sendRedirect("poker");
                    return;
                }
                playerAction.execute(game);

                if ("fold".equals(action)) {
                    game.executeShowdown();
                    session.removeAttribute("isCpuTurn");
                } else {
                    // プレイヤーのアクション完了後、CPUのターンフラグを立てる（即時実行しない）
                    session.setAttribute("isCpuTurn", true);
                }
            }
        }

        resp.sendRedirect("poker");
    }

    private void saveScore(String name, int score, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "")) {
            // userIdがない場合、名前から取得
            if (userId == null) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE name = ?")) {
                    ps.setString(1, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("id");
                        }
                    }
                }
            }

            // ハイスコアの場合のみusersテーブルを更新
            try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET score = ? WHERE name = ? AND score < ?")) {
                updateStmt.setInt(1, score);
                updateStmt.setString(2, name);
                updateStmt.setInt(3, score);
                int rows = updateStmt.executeUpdate();

                // 更新されず、かつユーザーが存在しない場合は新規作成
                if (rows == 0 && userId == null) {
                    try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (name, score) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, name);
                        insertStmt.setInt(2, score);
                        insertStmt.executeUpdate();
                        try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                userId = generatedKeys.getInt(1);
                            }
                        }
                    }
                }
            }

            // ランキングテーブルに保存
            if (userId != null) {
                try (PreparedStatement rankStmt = conn.prepareStatement("INSERT INTO rankings (user_id, game_name, score) VALUES (?, ?, ?)")) {
                    rankStmt.setInt(1, userId);
                    rankStmt.setString(2, "Poker");
                    rankStmt.setInt(3, score);
                    rankStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
