package org.docksidestage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kyota.yasuda
 */
@SuppressWarnings("serial")
public class GameWelcome extends HttpServlet {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(GameWelcome.class);

    // 状態を持ち続ける変数
    String gamemainUrl = "/gamemain?id=";
    boolean isGaming = false;
    Map<String, Boolean> machingMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("id");
        machingMap.put(userId, true);

        /**
         * ブラウザへHTMLを返す
         */
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!doctype html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta http-equiv=\"refresh\" content=\"5\">");
        out.println("<title>blackjack</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("<p>ようこそ " + userId + "さん！</p>");
        out.println("<br>");
        out.print("<p>エントリー中の人：");
        for (Entry<String, Boolean> userEntry : machingMap.entrySet()) {
            out.print(userEntry.getKey() + " ");
        }
        out.println("</p>");
        out.println("<br>");
        out.println("<p>この" + machingMap.size() + "人でゲームを開始しますか？</p>");
        makeButton(out, "はい", gamemainUrl + userId + "&numUser=" + machingMap.size() + "&serial=0");
        out.println("<br>");

        out.println("</body>");
        out.println("</html>");

        out.close();
    }

    private void outOnelineTableBy2dList(PrintWriter out, List<String> tableLine) {
        out.println("<table border=1>");
        out.println("<tr>");
        for (String cell : tableLine) {
            out.printf("<td>%s</td>", cell);
        }
        out.println("</tr>");
        out.println("</table>");
    }

    private void makeButton(PrintWriter out, String buttonName, String url) {
        List<String> tableList = new ArrayList<String>();
        String tableContents = "<a href=\"" + url + "\">" + buttonName + "</a>";
        tableList.add(tableContents);
        outOnelineTableBy2dList(out, tableList);
    }
}
