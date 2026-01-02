import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
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
        int score = Integer.parseInt(req.getParameter("score"));

        try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "")) {
            // セッション切れなどでuserIdがない場合、名前からID取得を試みる
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

            // ハイスコアの場合のみ更新する
            try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET score = ? WHERE name = ? AND score < ?")) {
                updateStmt.setInt(1, score);
                updateStmt.setString(2, name);
                updateStmt.setInt(3, score);
                int rows = updateStmt.executeUpdate();

                // 更新されなかった場合
                if (rows == 0) {
                    // ユーザーが存在するか確認
                    boolean exists = false;
                    try (PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM users WHERE name = ?")) {
                        checkStmt.setString(1, name);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) exists = true;
                        }
                    }

                    // ユーザーが存在しない場合のみ新規登録
                    if (!exists) {
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
            }

            // ランキングテーブルにも保存
            if (userId != null) {
                try (PreparedStatement rankStmt = conn.prepareStatement("INSERT INTO rankings (user_id, game_name, score) VALUES (?, ?, ?)")) {
                    rankStmt.setInt(1, userId);
                    rankStmt.setString(2, "BlockBreaker");
                    rankStmt.setInt(3, score);
                    rankStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 処理が終わったら結果画面に戻る
        resp.sendRedirect("result.jsp");
    }
}