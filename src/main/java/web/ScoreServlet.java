package web;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class ScoreServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        String name = (String) session.getAttribute("username");
        Integer userId = (Integer) session.getAttribute("userId");
        if (name == null) name = "Guest";
        
        try {
            int score = Integer.parseInt(req.getParameter("score"));
            ScoreRepository repository = new ScoreRepository();
            // BlockBreakerのスコアとして保存
            repository.saveScore(name, score, userId, "BlockBreaker");
        } catch (NumberFormatException e) {
            // スコアが不正な場合は無視して結果画面へ
        }
        // 処理が終わったら結果画面に戻る
        resp.sendRedirect("result.jsp");
    }
}