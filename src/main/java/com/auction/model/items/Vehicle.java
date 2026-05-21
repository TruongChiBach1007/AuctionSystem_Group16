package com.auction.model.items;

public class Vehicle extends Item {
    private double engineCapacity;

    public Vehicle(String id, String name, String description, double startingPrice, double currentPrice) {
        super(id, name, description, startingPrice, currentPrice);
        this.engineCapacity = 0.0;
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
