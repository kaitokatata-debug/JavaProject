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

public class FormServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 文字化け対策
        req.setCharacterEncoding("UTF-8");
        
        String name = req.getParameter("username");

        // 名前が空っぽ、またはスペースだけの場合
        if (name == null || name.trim().isEmpty()) {
            // エラーメッセージをセットして、元の画面(index.jsp)に戻す
            req.setAttribute("errorMessage", "名前を入力してください！");
            req.getRequestDispatcher("index.jsp").forward(req, resp);
        } 
        else {
            try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "")) {
                // 既に登録済みかどうかをチェック
                try (PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM users WHERE name = ?")) {
                    checkStmt.setString(1, name);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            // 既に登録済みの場合はセッションに保存して結果画面へ
                            int id = rs.getInt("id");
                            req.getSession().setAttribute("username", name);
                            req.getSession().setAttribute("userId", id);
                            resp.sendRedirect("result.jsp");
                            return;
                        }
                    }
                }

                // 重複していなければ保存
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.executeUpdate();
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            req.getSession().setAttribute("userId", generatedKeys.getInt(1));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            // セッションに名前を保存
            req.getSession().setAttribute("username", name);

            // OKなら結果画面へ「リダイレクト」
            resp.sendRedirect("result.jsp");
        }
    }
}