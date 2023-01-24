/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author march
 */
public class MatchLocal {
        String date;
        String time;
        String teamHome;
        String teamHomeUrl;
        String teamGuest;
        String teamGuestUrl;
        String tour;
        String url;
        String goalsHome;
        String goalsGuest;
        String stadium;

        public MatchLocal() {
            
        }

    @Override
    public String toString() {
        return "MatchLocal{" + "date=" + date + ", time=" + time + ", teamHome=" + teamHome + ", teamHomeUrl=" + teamHomeUrl + ", teamGuest=" + teamGuest + ", teamGuestUrl=" + teamGuestUrl + ", tour=" + tour + ", url=" + url + ", goalsHome=" + goalsHome + ", goalsGuest=" + goalsGuest + ", stadium=" + stadium + '}';
    }

        
        
    void parserMatchInfoLfl(Element tr){
        Elements tds = tr.select("td");
        Element teamHome = tr.selectFirst("td.right_align_table").selectFirst("a"); //Не спрашивайте меня почему так PS перепутано право с левом
        Element teamGuest = tr.selectFirst("td.left_align_table").selectFirst("a");
        Element date = tds.get(1).selectFirst("a");
        this.date = getDateWithoutDay(date.text());
        this.time = tds.get(2).text();
        this.tour = tds.get(0).text() + " тур";
        url = date.attr("href");
        this.teamHome = teamHome.attr("title");
        this.teamHomeUrl = teamHome.attr("href");
        this.teamGuest = teamGuest.attr("title");
        this.teamGuestUrl = teamGuest.attr("href");
        Element score = tds.get(4);
        String[] scores = score.text().split(":");
        if(scores.length == 2) {
            this.goalsHome = scores[0].trim();
            this.goalsGuest = scores[1].trim();
        } else {
            this.goalsHome = "-";
            this.goalsGuest = "-";
        }
        
        stadium = tds.get(6).text();
    }

    private String getDateWithoutDay(String date){
        String[] info = date.split(" ");
        return info[0];
    }
}
