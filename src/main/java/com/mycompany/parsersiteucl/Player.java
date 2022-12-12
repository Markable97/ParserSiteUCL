/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

/**
 *
 * @author m.glushko
 */
public class Player {
    String teamName;
    String name;
    String urlName;
    String urlPictures;
    String amplua;
    int height;
    int weight;
    String post;
    String birthday;
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

    Player() {
    }

    @Override
    public String toString() {
        return "Player{" + "name=" + name + ", urlName=" + urlName + ", urlPictures=" + urlPictures + ", amplua=" + amplua + ", birthday=" + birthday + '}';
    }
    
    

    
}
