package com.example.firebasestore;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class StoreItem implements Serializable {

    private ArrayList<String> images;
    private String brand;
    private String name;
    private double price;
    private String description;
    private String category;
    private int stock;

    public StoreItem() {

    }

    public StoreItem(ArrayList<String> images, String brand, String name, double price, String description, String category, int stock) {
        this.images = images;
        this.brand = brand;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.stock = stock;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public String getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public String toString() {

        return ("Brand: " + getBrand() + "\n"
                + "Name: " + getName() + "\n"
                + "Price: " + getPrice() + "\n"
                + "Description: " + getDescription() + "\n");
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (!(obj instanceof StoreItem)) {
            return false;
        }

        StoreItem other = (StoreItem) obj;

        return (brand.equals(other.getBrand())
                && name.equals(other.getName())
                && (price == other.getPrice())
                && description.equals(other.getDescription()));
    }
}
