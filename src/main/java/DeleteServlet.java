import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
            try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "");
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE name = ?")) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // 処理が終わったら一覧画面に戻る
        resp.sendRedirect("list");
    }
}