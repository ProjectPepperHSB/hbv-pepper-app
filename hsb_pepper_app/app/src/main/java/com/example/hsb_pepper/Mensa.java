package com.example.hsb_pepper;

import android.graphics.Bitmap;

public class Mensa {
    private String [] day;
    private String [] offer1;
    private String [] offer2;
    private Bitmap mensaImg;

    public Mensa(String [] day, String [] offer1, String [] offer2, Bitmap mensaImg) {
        this.day = day;
        this.offer1 = offer1;
        this.offer2 = offer2;
        this.mensaImg = mensaImg;
    }


    public String [] getDay() { return day; }
    public String [] getOffer1(){ return offer1; }
    public String [] getOffer2(){ return offer2; }
    public Bitmap getMensaImg(){ return mensaImg; }
    public void setDay(String [] day) { this.day = day; }
    public void setOffer1(String [] offer1) { this.offer1 = offer1; }
    public void setOffer2(String [] offer2) { this.offer2 = offer2; }
    public void setMensaImg(Bitmap mensaImg) { this.mensaImg = mensaImg; }
}

