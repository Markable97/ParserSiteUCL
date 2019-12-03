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
    String amplua;
    int height;
    int weight;
    String post;
    String birthday;    

    public Player(String teamName, String name, String urlName) {
        this.teamName = teamName;
        this.name = name;
        this.urlName = urlName;
    }

    @Override
    public String toString() {
        return "Player{" + "teamName=" + teamName + ", name=" + name + ", urlName=" + urlName + '}' + "\n";
    }
    
    
}
