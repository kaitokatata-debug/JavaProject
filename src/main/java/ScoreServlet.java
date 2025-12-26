import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ScoreServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String name = (String) req.getSession().getAttribute("username");
        if (name == null) name = "Guest";
        int score = Integer.parseInt(req.getParameter("score"));

        try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "");
             PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET score = ? WHERE name = ?")) {
            updateStmt.setInt(1, score);
            updateStmt.setString(2, name);
            int rows = updateStmt.executeUpdate();

            // 更新対象がいなかった場合（未登録ユーザーなど）は新規登録する
            if (rows == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (name, score) VALUES (?, ?)")) {
                    insertStmt.setString(1, name);
                    insertStmt.setInt(2, score);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 処理が終わったら結果画面に戻る
        resp.sendRedirect("result.jsp");
    }
}