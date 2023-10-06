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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author march
 */
public class TournamentTableParser {
    
    private final Connection dbConnect;
    
    class TournamentTable {
        int position;
        String teamImage;
        String teamUrl;
        String teamName;
        int games;
        int wins;
        int draws;
        int losses;
        int goals_scored;
        int goals_conceded;
        int goals_diferences;
        int points;

        @Override
        public String toString() {
            return "TotnamnetTable{" + "position=" + position + ", teamImage=" + teamImage + ", teamUrl=" + teamUrl + ", teamName=" + teamName + ", games=" + games + ", wins=" + wins + ", draws=" + draws + ", losses=" + losses + ", goals_scored=" + goals_scored + ", goals_conceded=" + goals_conceded + ", goals_diferences=" + goals_diferences + ", points=" + points + "}\n";
        }
    }

    public TournamentTableParser() {
        dbConnect = new DBConnection().connect;
    }

    ArrayList<TournamentTable> parserTournamentTable(String tournamentUrl) throws IOException {
        Document doc = SSLHelper.getConnection(tournamentUrl).get();
        Element tbody = doc.selectFirst("tbody");
        if (tbody == null) throw new NullPointerException("tbody not find");
        ArrayList<TournamentTable> tableList = new ArrayList<>(); 
        Elements trs = tbody.select("tr");
        trs.forEach(tr -> {
            TournamentTable table = new TournamentTable();
            Elements tds = tr.select("td");
            int index = 0;
            for(Element td : tds) {
                switch(index) {
                    case 0:
                        //Позиция
                        String position = td.text();
                        table.position = ParserLflHelper.parseInt(position);
                        break;
                    case 1:
                        //Фотка
                        String image = td.selectFirst("img").attr("src");
                        table.teamImage = image;
                        break;
                    case 2:
                        //Название команды
                        Element a = td.selectFirst("a");
                        String url = a.absUrl("href");
                        String name = a.text();
                        table.teamUrl = url;
                        table.teamName = name;
                        break;
                    case 3:
                        //Игры
                        table.games = ParserLflHelper.parseInt(td.text());
                        break;
                    case 4:
                        //Победа
                        table.wins = ParserLflHelper.parseInt(td.text());
                        break;
                    case 5:
                        table.draws = ParserLflHelper.parseInt(td.text());
                        //Ничьи
                        break;
                    case 6:
                        table.losses = ParserLflHelper.parseInt(td.text());
                        //Поражения
                        break;
                    case 7:
                        table.goals_scored = ParserLflHelper.parseInt(td.text());
                        //Забитые
                        break;
                    case 8:
                        table.goals_conceded = ParserLflHelper.parseInt(td.text());
                        //Пропущеные
                        break;
                    case 9:
                        table.goals_diferences = ParserLflHelper.parseInt(td.text());
                        //Разница
                        break;
                    case 10:
                        table.points = ParserLflHelper.parseInt(td.text());
                        //Очки
                        break;
                }
                index++;
            }
            tableList.add(table);
        });
        System.out.print(tableList);
        return tableList;
    }
    
    void inserTableInDB(String tournamentUrl, String tournamentId) throws IOException, SQLException {
        ArrayList<TournamentTable> tableList = parserTournamentTable(tournamentUrl);
        clearTable(tournamentId);
        addTable(tournamentId, tableList);
    }
    
    private void clearTable(String tournamentUrl) throws SQLException {
        String sql = "delete from tournament_table where tournament_url = ?";
        PreparedStatement prStatement = dbConnect.prepareStatement(sql);
        prStatement.setString(1, tournamentUrl);
        prStatement.executeUpdate();
    }
    
    private void addTable(String tournamentUrl, ArrayList<TournamentTable> tableList) throws SQLException {
        String sql = "INSERT INTO tournament_table (tournament_url, position, team_image, team_url, team_name, games, wins, draws, losses, goals_scored, goals_conceded, goals_difference, points)"
                + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        for(TournamentTable tableRow : tableList) {
            PreparedStatement prStatement = dbConnect.prepareStatement(sql);
            prStatement.setString(1, tournamentUrl);
            prStatement.setInt(2, tableRow.position);
            prStatement.setString(3, tableRow.teamImage);
            prStatement.setString(4, tableRow.teamUrl);
            prStatement.setString(5, tableRow.teamName);
            prStatement.setInt(6, tableRow.games);
            prStatement.setInt(7, tableRow.wins);
            prStatement.setInt(8, tableRow.draws);
            prStatement.setInt(9, tableRow.losses);
            prStatement.setInt(10, tableRow.goals_scored);
            prStatement.setInt(11, tableRow.goals_conceded);
            prStatement.setInt(12, tableRow.goals_diferences);
            prStatement.setInt(13, tableRow.points);
            prStatement.executeUpdate();
        }
    }

}
