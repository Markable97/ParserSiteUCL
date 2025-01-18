/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lfl_vao;

import java.io.File;
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
        String groupName;
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
        String colorPosition;

        @Override
        public String toString() {
            return "TournamentTable{" + "groupName=" + groupName + ", position=" + position + ", teamImage=" + teamImage + ", teamUrl=" + teamUrl + ", teamName=" + teamName + ", games=" + games + ", wins=" + wins + ", draws=" + draws + ", losses=" + losses + ", goals_scored=" + goals_scored + ", goals_conceded=" + goals_conceded + ", goals_diferences=" + goals_diferences + ", points=" + points + ", colorPosition=" + colorPosition + "}\n";
        }
    }

    public TournamentTableParser() {
        dbConnect = new DBConnection().connect;
    }
    
    private String getColorPosition(String attrClass) {
        String colorHex = null;
        if(attrClass.contains("up_selected")){
            colorHex = "e4f7e3";
        } else if (attrClass.contains("up2_selected")) {
            colorHex = "FEFFDB";
        } else if (attrClass.contains("down_selected")) {
            colorHex = "FFD5CF";
        } else if (attrClass.contains("down2_selected")) {
            colorHex = "FFEDEA";
        }
        return colorHex;
    }
    
    ArrayList<TournamentTable> parserTournamentGroupTable(String tournamentUrl) throws IOException {
        Document doc = SSLHelper.getConnection(tournamentUrl).get();
        Element tbody = doc.selectFirst("tbody");
        if (tbody == null) throw new NullPointerException("tbody not find");
        ArrayList<TournamentTable> tableList = new ArrayList<>(); 
        Elements trs = tbody.select("tr");
        String groupName = "";
        for(Element tr : trs) {
            Elements tds = tr.select("td");
            if (tds.size() == 1) {
                groupName = tds.get(0).text().replace("Шахматка", "").trim();
                continue;
            }
            TournamentTable table = getRow(tr, groupName);
            tableList.add(table);
        }
        System.out.print(tableList);
        return tableList;
    }
    
    private TournamentTable getRow(Element tr, String groupName) {
        TournamentTable table = new TournamentTable();
        table.groupName = groupName;
        table.colorPosition = getColorPosition(tr.attr("class"));
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
                    String url = a.attr("href");
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
        return table;
    }
    
    ArrayList<TournamentTable> parserTournamentTable(String tournamentUrl) throws IOException {
//        File input = new File("D:\\Загрузки\\lfl.ru.html");
//        Document doc = Jsoup.parse(input, "Windows-1251");
        Document doc = SSLHelper.getConnection(tournamentUrl).get();
        Element tbody = doc.selectFirst("tbody");
        if (tbody == null) {
            System.out.print("!!!!!!!!!!!!!!!!");
            System.out.print("!!!!!!!!!!!!!!!!");
            System.out.print("Table no fount " + tournamentUrl);
            System.out.print("!!!!!!!!!!!!!!!!");
            System.out.print("!!!!!!!!!!!!!!!!");
            return new ArrayList<>();
        }
        ArrayList<TournamentTable> tableList = new ArrayList<>(); 
        Elements trs = tbody.select("tr");
        trs.forEach(tr -> {
            if(tr.text().contains("Шахматка")) {
                return;
            }
            if(tr.text().contains(";")) {
                return;
            }
            TournamentTable table = getRow(tr, "");
            tableList.add(table);
        });
        System.out.print(tableList);
        return tableList;
    }
    
    void inserTableInDB(String tournamentUrl, String tournamentId) throws IOException, SQLException {
        ArrayList<TournamentTable> tableList;
        boolean isGroupTournament;
        if(false) {
            tableList = parserTournamentGroupTable(tournamentUrl);
            isGroupTournament = true;
        } else {
            tableList = parserTournamentTable(tournamentUrl);
            isGroupTournament = false;
        }
        clearTable(tournamentId, isGroupTournament);
        addTable(tournamentId, tableList, isGroupTournament);
    }
    
    private void clearTable(String tournamentUrl, boolean isGroupTournament) throws SQLException {
        String sql;
        if(isGroupTournament) {
            sql = "delete from tournament_table_group where tournament_url = ?";
        } else {
            sql = "delete from tournament_table where tournament_url = ?";
        }
        PreparedStatement prStatement = dbConnect.prepareStatement(sql);
        prStatement.setString(1, tournamentUrl);
        prStatement.executeUpdate();
    }
    
    private void addTable(String tournamentUrl, ArrayList<TournamentTable> tableList, boolean isGroupTournament) throws SQLException {
        String sql;
        if(isGroupTournament) {
            sql = "INSERT INTO tournament_table_group (tournament_url, position, team_image, team_url, team_name, games, wins, draws, losses, goals_scored, goals_conceded, goals_difference, points, color_position, group_name)"
                + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
             sql = "INSERT INTO tournament_table (tournament_url, position, team_image, team_url, team_name, games, wins, draws, losses, goals_scored, goals_conceded, goals_difference, points, color_position)"
                + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
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
            prStatement.setString(14, tableRow.colorPosition);
            if(isGroupTournament) {
                prStatement.setString(15, tableRow.groupName);
            }
            prStatement.executeUpdate();
        }
    }

}
