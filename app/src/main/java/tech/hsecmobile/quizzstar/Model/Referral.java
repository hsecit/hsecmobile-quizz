package tech.hsecmobile.quizzstar.Model;

public class Referral {
    String name, email, imageUrl, date;

    public Referral(String name, String email, String imageUrl, String date) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
