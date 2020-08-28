package tech.hsecmobile.quizzstar.Model;

public class Player {
    private String name, email, member_since, image_url;
    private int points;

    public Player(String name, String email, String member_since, String image_url, int points) {
        this.name = name;
        this.email = email;
        this.member_since = member_since;
        this.image_url = image_url;
        this.points = points;
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

    public String getMember_since() {
        return member_since;
    }

    public void setMember_since(String member_since) {
        this.member_since = member_since;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
