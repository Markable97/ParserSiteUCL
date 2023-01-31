
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
    public String urlPlayer;
    public String urlAssist;
    public String time;
    public String action;
    public int countAction;

    public Action(String urlPlayer, String action, int countAction) {
        this.urlPlayer = urlPlayer;
        this.action = action;
        this.countAction = countAction;
    }
    
    
    public Action(String urlPlayer, String urlAssist, String time, String action) {
        this.urlPlayer = urlPlayer;
        this.urlAssist = urlAssist;
        this.time = time;
        this.action = action;
        //Здесь как будто можно поставить 1, чтобы не ломать логику в DBRequest
        this.countAction = 1;
    }

    public static String getTypeCard(String text) {
        String card;
        if(text.contains("popup_yc.png")) {
            card = "Желтая карточка";
        } else {
            card = "Красная карточка";
        }
        return card;
    }
    
    public static String getTypeGoal(String text){
        String goal = "Гол";;
        if(text.contains("(пен)")) {
            goal = "Пенальти";
        }
        if(text.contains("(аг)")) {
            goal = "Автогол";
        }
        return goal;
    }
    
    public static String getTime(String text) {
        String[] texts = text.split(",");
        String time;
        if(texts.length > 1) {
            time = texts[texts.length - 1].replaceAll("\\D+","")+":00";
        } else {
            time = "00:00";
        }
        return time;
    }
    
    @Override
    public String toString() {
        return "Action{" + "urlPlayer=" + urlPlayer + ", urlAssist=" + urlAssist + ", time=" + time + ", action=" + action + ", countAction=" + countAction + '}';
    }

    
    
}
