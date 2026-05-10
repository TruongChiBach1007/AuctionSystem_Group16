package com.auction.model.users;

public class Bidder extends User {
    private double balance;


    public Bidder(int id, String username,String password,String fullname , String email,double balance){
        super(id,username,password,fullname,email);
        this.balance=balance;
    }
    public double getBalance(){
        return balance;
    }
    public void setBalance(double balance){
        this.balance=balance;
    }
    public void displayRoleInfo(){
        System.out.println("Role:Bidder");
    }
}
