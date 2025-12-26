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

public class RankingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<String> rankingList = new ArrayList<>();
        
        // スコアの高い順にトップ10を取得
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, score FROM users ORDER BY score DESC LIMIT 10")) {
            
            while (rs.next()) {
                // 表示用に "名前 (スコア点)" という文字列を作る
                rankingList.add(rs.getString("name") + " (" + rs.getInt("score") + "点)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        req.setAttribute("rankings", rankingList);
        req.getRequestDispatcher("ranking.jsp").forward(req, resp);
    }
}