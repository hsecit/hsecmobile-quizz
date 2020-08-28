package tech.hsecmobile.quizzstar.Model;

public class Completed {
    private String categoryName, categoryLevel;
    private int totalPoints, earnedPoints, wastedPoints, percentage;

    public Completed(String categoryName, String categoryLevel, int totalPoints, int earnedPoints, int wastedPoints, int percentage) {
        this.categoryName = categoryName;
        this.categoryLevel = categoryLevel;
        this.totalPoints = totalPoints;
        this.earnedPoints = earnedPoints;
        this.wastedPoints = wastedPoints;
        this.percentage = percentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryLevel() {
        return categoryLevel;
    }

    public void setCategoryLevel(String categoryLevel) {
        this.categoryLevel = categoryLevel;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(int earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    public int getWastedPoints() {
        return wastedPoints;
    }

    public void setWastedPoints(int wastedPoints) {
        this.wastedPoints = wastedPoints;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}



