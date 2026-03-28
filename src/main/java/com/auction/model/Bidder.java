package com.auction.model;
public class Bidder {
    private int id;
    private String name;

    public Bidder(int id, String name){
        this.id=id;
        this.name=name;
    }
    public String getName(){
        return name;
    }
}
