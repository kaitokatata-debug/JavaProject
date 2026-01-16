import java.io.IOException;
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
                session.removeAttribute("pokerGame");
                resp.sendRedirect("result.jsp");
                return;
            }

            // 次のラウンドへ（ショーダウン後）
            game.startNewRound();
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
                playerAction = new Action(human, Action.Type.BET, amount);
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
                }
            }

            // CPUのターン（簡易AI: 常にコール、またはチェック）
            if (!human.isFolded() && game.getState() != State.SHOWDOWN) {
                // CPUは単純にコールまたはチェックする
                cpu.doCall(game);
            }

            // ベッティングラウンド終了判定（簡易的に、お互いアクションしたら次へ進むとする）
            if (!human.isFolded()) {
                game.endBettingRound();

                // 2人以上残っている場合のみ次のストリートへ
                if (game.getTable().getActivePlayers().size() > 1) {
                    // オールイン判定：どちらかのチップが0になっている場合
                    boolean isAllIn = human.getChips() == 0 || cpu.getChips() == 0;

                    if (isAllIn) {
                        // オールイン状態なら、ショーダウンまで自動で進める
                        while (game.getState() != State.SHOWDOWN) {
                            game.advanceState();
                        }
                    } else {
                        // 通常進行
                        if (game.getState() == State.RIVER) {
                            game.executeShowdown();
                        } else if (game.getState() != State.SHOWDOWN) {
                            game.advanceState(); // PREFLOP -> FLOP, etc.
                        }
                    }
                }
            }
        }

        resp.sendRedirect("poker");
    }

}
