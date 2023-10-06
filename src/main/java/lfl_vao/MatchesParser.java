/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lfl_vao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author march
 */
public class MatchesParser {
    private final Connection dbConnect;

    public MatchesParser() {
        dbConnect = new DBConnection().connect;
    }
    
    void parser(String id) throws IOException, SQLException {
        String resultAjax = String.format("https://lfl.ru/?ajax=1&method=tournament_resault_table&tournament_id=%s&limit=400", id);
        String calendarAjax = String.format("https://lfl.ru/?ajax=1&method=tournament_calendar_table&tournament_id=%s&limit=400", id);
        System.out.println("----------" + id + "------------------------");
        System.out.println("----------RESULT------------------------");
        ArrayList<MatchLocal> results = parserResults(resultAjax);
        System.out.println("----------CALENDAR------------------------");
        ArrayList<MatchLocal> calendar = parserResults(calendarAjax);
        results.addAll(calendar);
        inserOrUpdateDB(results, id);
    }
    
    ArrayList<MatchLocal> parserResults(String url) throws IOException {
        Document doc = SSLHelper.getConnection(url).get();
        Element tbody = doc.selectFirst("tbody");
        if (tbody == null) throw new NullPointerException("tbody not find");
        Elements trs = tbody.select("tr");
        ArrayList<MatchLocal> matches = new ArrayList<>();
        for(Element tr : trs){
            if(tr.children().size() == 1) {
                //Строка тура
                System.out.println(tr.text());
            } else {
                MatchLocal match = new MatchLocal();
                match.parserMatchInfoLfl(tr);
                matches.add(match);
            }
        }
        System.out.println(matches);
        return matches;
    }
    
    void inserOrUpdateDB(ArrayList<MatchLocal> matches, String toutnamentUrl) throws SQLException {
        String sql = "INSERT INTO match_v2 (tournament_url, match_url, tour, division_name, date, stadium_name, referee_name, team_home_url, team_home_image, team_home_name, "
                   + "team_home_goals, team_guest_goals, team_guest_name, team_guest_url, team_guest_image, photo_url, video_url)\n"
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) \n"
                   + "ON DUPLICATE KEY UPDATE tournament_url = VALUES(tournament_url), tour = VALUES(tour), division_name = VALUES(division_name), date = VALUES(date), "
                   + "stadium_name = VALUES(stadium_name), referee_name = VALUES(referee_name), team_home_url = VALUES(team_home_url), team_home_image = VALUES(team_home_image), team_home_name = VALUES(team_home_name), team_home_goals = VALUES(team_home_goals),"
                   + "team_guest_goals = VALUES(team_guest_goals), team_guest_name = VALUES(team_guest_name), team_guest_url = VALUES(team_guest_url), team_guest_image = VALUES(team_guest_image),"
                   + "photo_url = VALUES(photo_url), video_url = VALUES(video_url);";
        for(MatchLocal match : matches) {
            PreparedStatement ps = dbConnect.prepareStatement(sql);
            ps.setString(1, toutnamentUrl);
            ps.setString(2, match.url);
            ps.setString(3, match.tour);
            ps.setString(4, ""); //Дивизион проставлю через SQL
            ps.setString(5, match.date + " " + match.time);
            ps.setString(6, match.stadium);
            ps.setString(7, ""); //refere
            ps.setString(8, match.teamHomeUrl);
            ps.setString(9, match.teamHomeImage);
            ps.setString(10, match.teamHome);
            ps.setString(11, match.goalsHome);
            ps.setString(12, match.goalsGuest);
            ps.setString(13, match.teamGuest);
            ps.setString(14, match.teamGuestUrl);
            ps.setString(15, match.teamGuestImage);
            ps.setString(16, match.photoUrl);
            ps.setString(17, match.videoUrl);
            //System.out.println(ps);
            ps.executeUpdate();
        }
    }
    
}
