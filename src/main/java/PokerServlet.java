import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import poker.HandEvaluator;
import poker.Player;
import poker.TexasHoldemGame;

public class PokerServlet extends HttpServlet {
    
    // ゲームの進行ステージ
    private enum Stage { PREFLOP, FLOP, TURN, RIVER, SHOWDOWN }

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
            session.setAttribute("pokerStage", Stage.PREFLOP);
            
            game.startNewRound();
        }

        req.getRequestDispatcher("poker.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        TexasHoldemGame game = (TexasHoldemGame) session.getAttribute("pokerGame");
        Stage stage = (Stage) session.getAttribute("pokerStage");

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
            // 次のラウンドへ（ショーダウン後）
            game.startNewRound();
            session.setAttribute("pokerStage", Stage.PREFLOP);
            resp.sendRedirect("poker");
            return;
        }

        // プレイヤーのアクション処理
        if (stage != Stage.SHOWDOWN) {
            if ("fold".equals(action)) {
                game.playerFold(human);
                game.log(human.getName() + " folds. CPU wins!");
                // フォールドしたら即終了扱い
                int pot = game.getPot();
                cpu.bet(-pot); // ポット獲得の簡易処理（チップを増やす）
                session.setAttribute("pokerStage", Stage.SHOWDOWN);
            } else if ("call".equals(action)) {
                game.playerCall(human);
            } else if ("bet".equals(action)) {
                int amount = Integer.parseInt(req.getParameter("amount"));
                game.playerBet(human, amount);
            }

            // CPUのターン（簡易AI: 常にコール、またはチェック）
            if (!human.isFolded() && stage != Stage.SHOWDOWN) {
                // CPUは単純にコールまたはチェックする
                game.playerCall(cpu);
            }

            // ベッティングラウンド終了判定（簡易的に、お互いアクションしたら次へ進むとする）
            if (!human.isFolded()) {
                game.endBettingRound();
                stage = advanceStage(game, stage);
                session.setAttribute("pokerStage", stage);
            }
        }

        resp.sendRedirect("poker");
    }

    private Stage advanceStage(TexasHoldemGame game, Stage current) {
        switch (current) {
            case PREFLOP:
                game.dealFlop();
                return Stage.FLOP;
            case FLOP:
                game.dealTurn();
                return Stage.TURN;
            case TURN:
                game.dealRiver();
                return Stage.RIVER;
            case RIVER:
                // ショーダウン処理
                game.log("=== Showdown ===");
                HandEvaluator.Hand h1 = HandEvaluator.evaluate(game.getPlayers().get(0).getHoleCards(), game.getCommunityCards());
                HandEvaluator.Hand h2 = HandEvaluator.evaluate(game.getPlayers().get(1).getHoleCards(), game.getCommunityCards());
                
                game.log(game.getPlayers().get(0).getName() + ": " + h1);
                game.log(game.getPlayers().get(1).getName() + ": " + h2);
                
                int result = h1.compareTo(h2);
                int pot = game.getPot();
                if (result > 0) {
                    game.log("Winner: " + game.getPlayers().get(0).getName());
                    game.getPlayers().get(0).bet(-pot); // チップ返還（簡易）
                } else if (result < 0) {
                    game.log("Winner: CPU");
                    game.getPlayers().get(1).bet(-pot);
                } else {
                    game.log("Draw");
                    game.getPlayers().get(0).bet(-pot/2);
                    game.getPlayers().get(1).bet(-pot/2);
                }
                return Stage.SHOWDOWN;
            default:
                return Stage.SHOWDOWN;
        }
    }
}
