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
        String teamHomeImage;
        String teamHomeUrl;
        String teamGuest;
        String teamGuestImage;
        String teamGuestUrl;
        String tour;
        String url;
        String goalsHome;
        String goalsGuest;
        String stadium;
        String photoUrl;
        String videoUrl;

        public MatchLocal() {
            
        }

    @Override
    public String toString() {
        return "MatchLocal{" + "date=" + date + ", time=" + time + ", teamHome=" + teamHome + ", teamHomeImage=" + teamHomeImage + ", teamHomeUrl=" + teamHomeUrl + ", teamGuest=" + teamGuest + ", teamGuestImage=" + teamGuestImage + ", teamGuestUrl=" + teamGuestUrl + ", tour=" + tour + ", url=" + url + ", goalsHome=" + goalsHome + ", goalsGuest=" + goalsGuest + ", stadium=" + stadium + ", photoUrl=" + photoUrl + ", videoUrl=" + videoUrl + "}\n";
    }



    void parserMatchInfoLfl(Element tr){
        Elements tds = tr.select("td");
        Element teamHome = tr.selectFirst("td.right_align_table").selectFirst("a"); //Не спрашивайте меня почему так PS перепутано право с левом
        Element teamGuest = tr.selectFirst("td.left_align_table").selectFirst("a");
        Element date = tds.get(1).selectFirst("a");
        this.date = getDateWithoutDay(date.text());
        this.time = tds.get(2).text();
        this.tour = tds.get(0).text();
        this.url = date.attr("href");
        this.teamHome = teamHome.attr("title");
        this.teamHomeUrl = teamHome.attr("href");
        this.teamHomeImage = teamHome.selectFirst("img").attr("src");
        this.teamGuest = teamGuest.attr("title");
        this.teamGuestUrl = teamGuest.attr("href");
        this.teamGuestImage = teamGuest.selectFirst("img").attr("src");
        
        
        Element score = tds.get(4);
        Element aPhoto = score.selectFirst("a.gallery-icon");
        Element aVideo = score.selectFirst("a.video-icon.video-icon-3");
        if (aPhoto != null) {
            this.photoUrl = aPhoto.absUrl("href");
        }
        if (aVideo != null) {
            this.videoUrl = aVideo.absUrl("href");
        }
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
