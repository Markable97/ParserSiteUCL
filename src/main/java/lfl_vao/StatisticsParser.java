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
public class StatisticsParser {
    
    private final Connection dbConnect;
    
    enum Action {
        GOALS("Голы"), ASSISTS("Пасы"), YELLOW("ЖК");
        
        private final String title;

        private Action(String title) {
            this.title = title;
        }
    }

    class Statistic {
        String tournamentUrl;
        String playerUrl;
        Action action;
        String playerName;
        String playerImage;
        String playerTeam;
        String playerTeamUrl;
        String amplua;
        int amount;

        @Override
        public String toString() {
            return "Statistic{" + "tournamentUrl=" + tournamentUrl + ", playerUrl=" + playerUrl + ", action=" + action + ", playerName=" + playerName + ", playerImage=" + playerImage + ", playerTeam=" + playerTeam + ", playerTeamUrl=" + playerTeamUrl + ", amplua=" + amplua + ", amount=" + amount + "}\n";
        }

        
    }
    
    public StatisticsParser() {
        dbConnect = new DBConnection().connect;
    }
    
    
    void parserStatistics(String tournamentUrl) throws IOException, SQLException {
        String id;
        String ajaxGoals;
        String ajaxAssist;
        String ajaxYellow;
        if(tournamentUrl.contains("division")) {
            id = tournamentUrl.replace("/division", "");
            ajaxGoals = String.format("https://lfl.ru/?ajax=1&method=tournament_strikers_table&division_id=%s", id);
            ajaxAssist = String.format("https://lfl.ru/?ajax=1&method=tournament_assistants_table&division_id=%s&limit=400", id);
            ajaxYellow = String.format("https://lfl.ru/?ajax=1&method=tournament_cardsY_table&division_id=%s&limit=400", id);
        } else {
            id = tournamentUrl.replace("/tournament", "");
            ajaxGoals = String.format("https://lfl.ru/?ajax=1&method=tournament_strikers_table&tournament_id=%s", id);
            ajaxAssist = String.format("https://lfl.ru/?ajax=1&method=tournament_assistants_table&tournament_id=%s&limit=400", id);
            ajaxYellow = String.format("https://lfl.ru/?ajax=1&method=tournament_cardsY_table&tournament_id=%s&limit=400", id);
        }
        
        ArrayList<Statistic> goals = parserStatisticsInternal(ajaxGoals, Action.GOALS, tournamentUrl);
        inserOrUpdateDB(goals);
        
        ArrayList<Statistic> assists = parserStatisticsInternal(ajaxAssist, Action.ASSISTS, tournamentUrl);
        inserOrUpdateDB(assists);
        
        ArrayList<Statistic> yellow = parserStatisticsInternal(ajaxYellow, Action.YELLOW, tournamentUrl);
        inserOrUpdateDB(yellow);
    };
    
    private ArrayList<Statistic> parserStatisticsInternal(String url, Action action, String tournamnetUrl) throws IOException {
        ArrayList<Statistic> statisctics = new ArrayList<>();
        Document doc = SSLHelper.getConnection(url).get();
        Element tbody = doc.selectFirst("tbody");
        if (tbody == null) return new ArrayList<>();
        Elements trs = tbody.select("tr");
        for(Element tr : trs) {
            Statistic statistic = new Statistic();
            statistic.tournamentUrl = tournamnetUrl;
            statistic.action = action;
            Elements tds = tr.select("td");
            //Player
            Element tdPlayer = tds.get(1);
            Element aPlayerName = tdPlayer.selectFirst("a.player");
            statistic.playerName = aPlayerName.text();
            statistic.playerUrl = aPlayerName.attr("href");
            String styleImage = tdPlayer.select("a.usr-image_link").attr("style").split(";")[0];
            statistic.playerImage = styleImage.split(" ")[1].replace("url(", "").replace(")", "");
            statistic.amplua = tdPlayer.selectFirst("span.amplua").text();
            //TEAM
            Element tdTeam = tds.get(2);
            Element ulTeam = tdTeam.selectFirst("ul");
            if(ulTeam != null) {
                Elements lis =  ulTeam.select("li");
                ArrayList<String> teams = new ArrayList<>();
                ArrayList<String> teamsUrls = new ArrayList<>();
                for(Element li : lis) {
                    teams.add(li.text().trim());
                    teamsUrls.add(li.selectFirst("a.club").attr("href"));
                }
                statistic.playerTeam = teams.toString().replace("[", "").replace("]", "");
                statistic.playerTeamUrl = teamsUrls.toString().replace("[", "").replace("]", "");
            } else {
                statistic.playerTeam = tdTeam.text();
                statistic.playerTeamUrl = tdTeam.selectFirst("a.club").attr("href");
            }
            //Amount
            statistic.amount = Integer.parseInt(tds.get(3).text());
            if(action == Action.GOALS) {
                statistic.amount = statistic.amount + Integer.parseInt(tds.get(4).text());
            }
            statisctics.add(statistic);
        }
        System.out.println(statisctics);
        return statisctics;
    }
    
    
    void inserOrUpdateDB(ArrayList<Statistic> statistics) throws SQLException {
        String sql = "INSERT INTO tournament_statistics (tournament_url, player_url, action, player_name, player_image, player_team, amplua, amount)\n" +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)\n" +
                     "ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), player_image = VALUES(player_image), player_team = VALUES(player_team), " +
                     "amplua = VALUES(amplua), amount = VALUES(amount);";
        for(Statistic st : statistics) {
            PreparedStatement ps = dbConnect.prepareStatement(sql);
            ps.setString(1, st.tournamentUrl);
            ps.setString(2, st.playerUrl);
            ps.setString(3, st.action.title );
            ps.setString(4, st.playerName);
            ps.setString(5, st.playerImage);
            ps.setString(6, st.playerTeam);
            ps.setString(7, st.amplua);
            ps.setInt(8, st.amount);
            ps.executeUpdate();
        }
    }
    
    
}
