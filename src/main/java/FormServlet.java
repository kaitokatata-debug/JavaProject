import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE name = ?")) {
                    checkStmt.setString(1, name);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            // 重複している場合はエラーメッセージを出して戻る
                            req.setAttribute("errorMessage", "その名前は既に登録されています！");
                            req.getRequestDispatcher("index.jsp").forward(req, resp);
                            return;
                        }
                    }
                }

                // 重複していなければ保存
                try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (name) VALUES (?)")) {
                    pstmt.setString(1, name);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            // OKなら結果画面へ「リダイレクト」（URLを変えて移動）させる
            // 日本語が含まれる可能性があるため、URLエンコードを行う
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
            resp.sendRedirect("result.jsp?username=" + encodedName);
        }
    }
}