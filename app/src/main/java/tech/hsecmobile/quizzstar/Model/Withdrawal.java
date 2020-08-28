package tech.hsecmobile.quizzstar.Model;

public class Withdrawal {
    private String amount, status, payment_method, payment_account, date;
    private int points;

    public Withdrawal(String amount, String status, String payment_method, String payment_account, String date, int points) {
        this.amount = amount;
        this.status = status;
        this.payment_method = payment_method;
        this.payment_account = payment_account;
        this.date = date;
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getPayment_account() {
        return payment_account;
    }

    public void setPayment_account(String payment_account) {
        this.payment_account = payment_account;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

