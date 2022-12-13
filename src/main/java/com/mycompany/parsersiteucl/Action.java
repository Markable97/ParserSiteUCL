
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

/**
 *
 * @author march
 */
public class Action {
    String urlPlayer;
    String urlAssist;
    String time;
    String action;

    public Action(String urlPlayer, String urlAssist, String time, String action) {
        this.urlPlayer = urlPlayer;
        this.urlAssist = urlAssist;
        this.time = time;
        this.action = action;
    }

    @Override
    public String toString() {
        return "Action{" + "urlPlayer=" + urlPlayer + ", urlAssist=" + urlAssist + ", time=" + time + ", action=" + action + '}';
    }
    
    
    
}
