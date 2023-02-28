/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author m.glushko
 */
public class Player {
    public String teamName;
    public String teamUrl;
    public String name;
    public String urlName;
    public String urlPictures;
    public String amplua;
    int height;
    int weight;
    String post;
    public String birthday;
    int games;
    int goal;
    int assist;
    int yellow;
    int red;
    int penalty;
    int penaltyOut;
    int ownGoal;    

    public Player(String teamName, String name, String urlName) {
        this.teamName = teamName;
        this.name = name;
        this.urlName = urlName;
    }

    public Player(String teamName, String name, String urlName, String amplua) {
        this.teamName = teamName;
        this.name = name;
        this.urlName = urlName;
        this.amplua = amplua;
    }

    public Player() {
    }

    @Override
    public String toString() {
        return "Player{" + "name=" + name + ", urlName=" + urlName + ", urlPictures=" + urlPictures + ", amplua=" + amplua + ", birthday=" + birthday + ", teamName" + teamName +'}';
    }
    
    public void parserLfl(Element divPlayer){
        Element divPlayerLogo = divPlayer.selectFirst("div.player_logo");
        urlPictures = divPlayerLogo.selectFirst("a").attr("href");
        Element divPlayerInfo = divPlayer.selectFirst("div.player_title");
        Element pTitle = divPlayerInfo.selectFirst("p.player_title_name");
        urlName = pTitle.selectFirst("a").attr("href");
        name = pTitle.text();
        Elements p = divPlayerInfo.select("p");
        amplua = p.get(2).text().replace("Амплуа: ", "").trim();
        birthday = p.get(4).text().replace("Дата рождения: ", "").trim();
    }
    
    public void parserLflInMainPageTeam(Element aPlayer){
        urlName = aPlayer.attr("href");
        String nameInfo = aPlayer.text().replaceAll("[^a-zA-Zа-яёА-ЯЁ -]", "").trim();
        name = nameInfo;
    } 
    
    public void parserLflFastStatistic(Element trPlayer) {
        birthday = "";
        Element tdPlayerInfo = trPlayer.selectFirst("td.left_align_table.FIO");       
        amplua = tdPlayerInfo.selectFirst("span").text();
        Element aName = tdPlayerInfo.selectFirst("a.player");
        urlName = aName.attr("href");
        name = aName.text();
        urlPictures = tdPlayerInfo.selectFirst("a.usr-image_link").attr("style").replace("background: url(", "").replace(") no-repeat center center; background-size: contain;", "");
    }
    
  
}
