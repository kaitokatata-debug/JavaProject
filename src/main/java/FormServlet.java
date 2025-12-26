import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
        } else {
            // データベースに保存
            try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "")) {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (name) VALUES (?)");
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // OKなら結果画面(result.jsp)へ進む
            req.getRequestDispatcher("result.jsp").forward(req, resp);
        }
    }
}