package com.auction.model.items;

import java.io.Serializable;

public class Vehicle extends Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private double engineCapacity;

    public Vehicle(String id, String name, String description, double startingPrice, double currentPrice) {
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
