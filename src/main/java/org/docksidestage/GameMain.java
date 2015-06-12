package org.docksidestage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
public class GameMain extends HttpServlet {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(GameMain.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        /**
         * データ処理
         */
        String yamafudaUrl = "/game";
        String standUrl = "/game";

        List<String> hands = new ArrayList<String>();
        hands.add("A");
        hands.add("2");
        hands.add("3");

        /**
         * ブラウザへHTMLを返す
         */
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!doctype html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<title>blackjack</title>");
        out.println("</head>");
        out.println("<body>");

        makeButton(out, "山札", yamafudaUrl);
        out.println("<br>");
        outOnelineTableBy2dList(out, hands);
        out.println("<br>");
        makeButton(out, "スタンド", standUrl);

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
