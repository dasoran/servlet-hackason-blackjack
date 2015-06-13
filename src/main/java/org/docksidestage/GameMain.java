package org.docksidestage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
public class GameMain extends HttpServlet {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(GameMain.class);

    private class Card {
        public String type;
        public Integer number;

        public Card(String type, Integer number) {
            this.type = type;
            this.number = number;
        }
    }

    enum Mode {
        MATCHING, NORMAL, STANDING, FINISH
    };

    // 状態を持ち続ける変数
    List<Card> deck;
    Map<String, List<Card>> userStatus = new HashMap<>();
    List<String> userNameList = new ArrayList<>();
    String nowUser = null;
    Integer nowCommandSerial = 0;
    Boolean isStand = false;
    String standUser = null;

    public GameMain() {
        generateNewDeck();
    }

    private void generateNewDeck() {
        deck = new ArrayList<Card>();
        String[] types = new String[] { "♣", "♦", "♥", "♠" };
        for (String type : types) {
            for (int i = 1; i <= 13; i++) {
                deck.add(new Card(type, i));
            }
        }
        Collections.shuffle(deck);
    }

    private Card getCardFromDeck() {
        return deck.remove(0);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /**
         * ロジック処理
         */
        String userId = request.getParameter("id");
        Integer numberUser = Integer.valueOf(request.getParameter("numUser"));
        Integer commandSerial = Integer.valueOf(request.getParameter("serial"));
        if (!userStatus.containsKey(userId)) {
            nowUser = userId;
            userNameList.add(userId);
            userStatus.put(userId, new ArrayList<Card>());
            userStatus.get(userId).add(getCardFromDeck());
        }

        Mode mode;
        if (numberUser != userStatus.size()) {
            mode = Mode.MATCHING;
        } else if (isStand && standUser.equals(nowUser)) {
            mode = Mode.FINISH;
        } else if (nowCommandSerial >= commandSerial) {
            mode = Mode.NORMAL;
        } else if (Boolean.valueOf(request.getParameter("stand"))) {
            mode = Mode.STANDING;
            nowCommandSerial += 1;

            isStand = true;
            if (standUser == null) {
                standUser = nowUser;
            }

            int nowIndex = userNameList.indexOf(userId);
            if (nowIndex + 1 == userNameList.size()) {
                nowUser = userNameList.get(0);
            } else {
                nowUser = userNameList.get(nowIndex + 1);
            }
        } else {
            mode = Mode.NORMAL;
            userStatus.get(userId).add(getCardFromDeck());
            nowCommandSerial += 1;

            int nowIndex = userNameList.indexOf(userId);
            if (nowIndex + 1 == userNameList.size()) {
                nowUser = userNameList.get(0);
            } else {
                nowUser = userNameList.get(nowIndex + 1);
            }
        }

        // 手札集計
        int sumHandsNumber = 0;
        // TODO Aの処理
        for (Card card : userStatus.get(userId)) {
            sumHandsNumber += card.number > 10 ? 10 : card.number;
        }

        /**
         * データ処理
         */
        String yamafudaUrl = "/gamemain?id=" + userId + "&numUser=" + numberUser;
        String standUrl = "/gamemain?id=" + userId + "&numUser=" + numberUser + "&stand=true";

        List<String> handsString = new ArrayList<>();
        for (Card card : userStatus.get(userId)) {
            handsString.add(card.type + card.number);
        }

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

        if (mode == Mode.MATCHING) {
            out.println("<p>マッチング中です....</p>");
            out.print("<p>ゲーム開始可能な人：");
            for (Entry<String, List<Card>> userEntry : userStatus.entrySet()) {
                out.print(userEntry.getKey() + " ");
            }
            out.println("</p>");
            out.println("<br>");
        } else if (mode == Mode.FINISH) {
            out.println("<p>終了</p>");
            out.println("<br>");

            Map<Integer, String> sumList = new HashMap<>();
            int maxNumber = 0;
            for (Entry<String, List<Card>> userEntry : userStatus.entrySet()) {
                out.print("<p>");
                out.print(userEntry.getKey() + "：");

                // 手札集計
                int sumHandsNumberForFinish = 0;
                // TODO Aの処理
                for (Card card : userEntry.getValue()) {
                    sumHandsNumberForFinish += card.number > 10 ? 10 : card.number;
                }

                out.print(sumHandsNumberForFinish);
                if (maxNumber < sumHandsNumberForFinish && sumHandsNumberForFinish <= 21) {
                    maxNumber = sumHandsNumberForFinish;
                }
                sumList.put(sumHandsNumberForFinish, userEntry.getKey());

                out.println("</p>");
                out.println("<br>");
            }
            out.println("<br>");
            out.println("<p>勝者：" + sumList.get(maxNumber) + "</p>");
            out.println("<br>");

        } else {
            if (isStand) {
                out.println("<p>スタンドされました！</p>");
                out.println("<br>");
            }
            out.println("<p>現在のプレイヤー：" + nowUser + "</p>");
            out.println("<br>");
            out.println("<p>残り山札枚数：" + deck.size() + "</p>");
            out.println("<br>");

            if (nowUser.equals(userId)) {
                makeButton(out, "山札", yamafudaUrl + "&serial=" + (nowCommandSerial + 1));
                out.println("<br>");
            }
            outOnelineTableBy2dList(out, handsString);
            out.println("<br>");

            out.println("<p>手札合計：" + sumHandsNumber + "</p>");

            if (nowUser.equals(userId)) {
                if (mode == Mode.NORMAL) {
                    makeButton(out, "スタンド", standUrl + "&serial=" + (nowCommandSerial + 1));
                } else if (mode == Mode.STANDING) {}
            }

        }
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
