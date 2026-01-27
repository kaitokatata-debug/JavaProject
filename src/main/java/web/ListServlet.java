package web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dao.RankingDao;

public class ListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 名前とスコアをセットで扱うため、String配列のリストに変更
        List<String[]> userList = new ArrayList<>();
        
        // DAOを使用してデータを取得（インフラ層への委譲）
        RankingDao dao = new RankingDao();
        List<RankingDao.RankingRecord> rankings = dao.getAllRankings();

        int maxScore = -1;
        boolean isFirst = true;

        // 取得したデータに対して、表示用の加工（王冠ロジック）を行う
        for (RankingDao.RankingRecord record : rankings) {
            // 1位（最初のレコード）のスコアを基準にする
            if (isFirst) {
                maxScore = record.score;
                isFirst = false;
            }

            // 最高スコアと同じなら王冠をつける（同率1位対応）
            String crown = (record.score == maxScore) ? "\uD83D\uDC51 " : "";

            // { "名前", "スコア", "王冠" } という配列を作ってリストに追加
            // 名前(user[0])はDB操作用にそのまま残し、表示用に王冠(user[2])を渡す
            userList.add(new String[] { record.name, String.valueOf(record.score), crown });
        }

        // 取得したリストをリクエストにセットして、list.jsp に転送
        req.setAttribute("users", userList);
        req.getRequestDispatcher("list.jsp").forward(req, resp);
    }
}