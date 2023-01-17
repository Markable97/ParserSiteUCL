/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lfl_vao;

import com.mycompany.parsersiteucl.Team;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author march
 */
public class ParserLflVao {
    public static void main(String[] args) throws IOException, SQLException, InterruptedException{
           parserTeam();
    }
    
    private static void parserTeam() throws IOException{
        DBRequest db = new DBRequest();
        ArrayList<String> urlsTournament = db.getTournamentUrl();
        for(String url : urlsTournament){
            ArrayList<Team> teams = new ArrayList<>();
            String urlParser = "https://lfl.ru/?ajax=1&method=tournament_progress_table&tournament_id=" + url.replaceAll("\\D+","");
            System.out.println(urlParser);
           
            Document doc = SSLHelper.getConnection(urlParser).get();
            Element table = doc.selectFirst("table");
            Elements trs = table.select("tr");
            for(Element tr : trs){
                Team team = new Team();
                team.parserTeamLfl(tr);
                if (team.isTeam()){
                    teams.add(team);
                }
            }
            System.out.println(teams);
            db.addTeams(teams, url);
        }
        db.close();
    }
 
}
