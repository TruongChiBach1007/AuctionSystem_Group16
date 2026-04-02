package com.auction.model;

public class Vehicle extends Item {
    private double engineCapacity;

    public Vehicle(String id, String name, String description, double startingPrice, double currentPrice, double engineCapacity) {
        super(id, name, description, startingPrice, currentPrice);
        this.engineCapacity = engineCapacity;
    }

    public double getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(double engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    @Override
    public String getDetailedInfo() {
        return "Phương tiện: Động cơ " + engineCapacity + "L.";
    }
}
