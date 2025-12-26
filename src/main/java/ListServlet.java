import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<String> userList = new ArrayList<>();
        
        // データベースから全ユーザー名を取得
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM users")) {
            
            while (rs.next()) {
                userList.add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 取得したリストをリクエストにセットして、list.jsp に転送
        req.setAttribute("users", userList);
        req.getRequestDispatcher("list.jsp").forward(req, resp);
    }
}