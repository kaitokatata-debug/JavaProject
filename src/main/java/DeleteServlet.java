import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DeleteServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String name = req.getParameter("username");

        if (name != null && !name.isEmpty()) {
            try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "")) {
                // まずユーザーIDを取得
                int userId = -1;
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE name = ?")) {
                    ps.setString(1, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("id");
                        }
                    }
                }

                if (userId != -1) {
                    // ランキングテーブルから関連データを削除
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM rankings WHERE user_id = ?")) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                    // ユーザー本体を削除
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // 処理が終わったら一覧画面に戻る
        resp.sendRedirect("list");
    }
}