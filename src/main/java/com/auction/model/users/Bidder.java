package com.auction.model.users;

public class Bidder extends User {
    private double balance;


    public Bidder(int id, String username,String password,String fullname , String email,double balance){
        super(id,username,password,fullname,email);
        this.balance=balance;
    }
    public Double getBalance(){
        return balance;
    }
    public void setBalance(){
        this.balance=balance;
    }
    public void displayRoleInfo(){
        System.out.println("Role:Bidder");
    }
}
