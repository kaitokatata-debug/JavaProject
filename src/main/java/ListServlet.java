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
        // 名前とスコアをセットで扱うため、String配列のリストに変更
        List<String[]> userList = new ArrayList<>();
        
        // データベースから全ユーザー名とスコアを取得
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT u.name, COALESCE(MAX(r.score), 0) as score FROM users u LEFT JOIN rankings r ON u.id = r.user_id GROUP BY u.id, u.name ORDER BY score DESC")) {
            
            int maxScore = -1;
            boolean isFirst = true;

            while (rs.next()) {
                String name = rs.getString("name");
                int score = rs.getInt("score");

                // 1位（最初のレコード）のスコアを基準にする
                if (isFirst) {
                    maxScore = score;
                    isFirst = false;
                }

                // 最高スコアと同じなら王冠をつける（同率1位対応）
                String crown = (score == maxScore) ? "\uD83D\uDC51 " : "";

                // { "名前", "スコア", "王冠" } という配列を作ってリストに追加
                // 名前(user[0])はDB操作用にそのまま残し、表示用に王冠(user[2])を渡す
                userList.add(new String[] { name, String.valueOf(score), crown });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 取得したリストをリクエストにセットして、list.jsp に転送
        req.setAttribute("users", userList);
        req.getRequestDispatcher("list.jsp").forward(req, resp);
    }
}