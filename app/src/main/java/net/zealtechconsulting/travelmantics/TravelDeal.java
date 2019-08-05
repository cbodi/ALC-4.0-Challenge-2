package net.zealtechconsulting.travelmantics;

import java.io.Serializable;

//The TravelDeal class implements the Serializable class so that the TravelDeal class can be passed in the putExtra
//method of an Intent. For performance reasons, it will be better to use the Parcelable interface
public class TravelDeal implements Serializable {
    private String id;
    private String title;
    private String description;
    private String price;
    private String imageUrl;
    private String imagePath;

    public TravelDeal(String title, String description, String price, String imageUrl, String imagePath) {
        this.setId(id);
        this.setTitle(title);
        this.setDescription(description);
        this.setPrice(price);
        this.setImageUrl(imageUrl);
        this.setImagePath(imagePath);
    }

    public TravelDeal() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
