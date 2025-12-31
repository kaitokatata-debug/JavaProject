import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import jakarta.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.loader.WebappLoader;

public class App {
    public static void main(String[] args) throws LifecycleException, ServletException {
        // データベースの初期化（テーブル作成）
        // jdbc:h2:./mydb はプロジェクトフォルダに mydb というファイルを作る設定です
        try (Connection conn = DriverManager.getConnection("jdbc:h2:./mydb", "sa", "")) {
            Statement stmt = conn.createStatement();
            // usersテーブルに score カラムを追加（デフォルト値 0）
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), score INT DEFAULT 0)");
            // 既存のDBファイルを使っている場合のために、カラム追加を試みる（既に存在すれば何もしない）
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS score INT DEFAULT 0");
            // スコアランキング用のテーブル作成
            stmt.execute("CREATE TABLE IF NOT EXISTS rankings (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT, game_name VARCHAR(255), score INT)");
            System.out.println("Database initialized.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tomcatインスタンスの作成
        Tomcat tomcat = new Tomcat();
        
        // ポート設定
        tomcat.setPort(8080);
        
        // 作業ディレクトリの設定
        tomcat.setBaseDir("target/tomcat");

        // コネクタの初期化
        tomcat.getConnector();

        // Webアプリケーションの登録 (コンテキストパスとドキュメントベースの設定)
        Context ctx = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());

        // クラスローダーの設定 (Maven依存関係解決のため)
        ctx.setParentClassLoader(App.class.getClassLoader());

        WebappLoader loader = new WebappLoader();
        loader.setDelegate(true);
        ctx.setLoader(loader);

        // ウェルカムファイルの設定
        ctx.addWelcomeFile("index.jsp");

        // FormServletを登録して、/submit というパスに割り当てる
        Tomcat.addServlet(ctx, "formServlet", new FormServlet());
        ctx.addServletMappingDecoded("/submit", "formServlet");

        // ListServletを登録して、/list というパスに割り当てる
        Tomcat.addServlet(ctx, "listServlet", new ListServlet());
        ctx.addServletMappingDecoded("/list", "listServlet");

        // DeleteServletを登録して、/delete というパスに割り当てる
        Tomcat.addServlet(ctx, "deleteServlet", new DeleteServlet());
        ctx.addServletMappingDecoded("/delete", "deleteServlet");

        // ScoreServlet (スコア保存)
        Tomcat.addServlet(ctx, "scoreServlet", new ScoreServlet());
        ctx.addServletMappingDecoded("/saveScore", "scoreServlet");

        // LogoutServlet (ログアウト)
        Tomcat.addServlet(ctx, "logoutServlet", new LogoutServlet());
        ctx.addServletMappingDecoded("/logout", "logoutServlet");

        System.out.println("Tomcat started on port 8080");
        
        // サーバーの起動と待機
        tomcat.start();
        tomcat.getServer().await();
    }
}