/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.parsersiteucl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author march
 */
public class MyThreadPicture extends Thread{
    Team team;
    String url;
    public MyThreadPicture(Team team) {
        super("Поток - " + team.teamName);
        this.team = team;
        this.url = team.urlName;
    }
    
    
    
    @Override
    public void run() {
        System.out.println("Начало потока " + getName());
        boolean f = true;
        while (f){
        try {
            System.out.println("Начало потока в цикле" + getName());
            Document doc = Jsoup.connect(url).get();
            Element div = doc.selectFirst("div.team-info__logo");
            Element img = div.getElementsByTag("img").first();
            System.out.println(getName() + " URL = " + img.attr("src"));
            
            
                URL connection = new URL("http://football.businesschampions.ru"+img.attr("src"));
                HttpURLConnection urlCon = (HttpURLConnection) connection.openConnection();
                urlCon.setRequestMethod("GET");
                urlCon.connect();
                InputStream in;
                in = urlCon.getInputStream();
                String nameFile = team.teamName+".png";
                File file = new File("D:\\Pictures\\UCL\\"+team.nameDivision+"\\"+nameFile);
                OutputStream writer = new FileOutputStream(file);
                byte buffer[] = new byte[1];
                int c = in.read(buffer);
                while (c > 0) {
                    writer.write(buffer, 0, c);
                    c = in.read(buffer);
                }
                writer.flush();
                writer.close();
                in.close();
                f = false;
                Thread.sleep(500);
            
            
        } catch (IOException ex) {
            Logger.getLogger(MyThreadPicture.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MyThreadPicture.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    }
    
}
