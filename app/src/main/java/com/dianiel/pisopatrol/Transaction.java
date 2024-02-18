package com.dianiel.pisopatrol;

import java.util.Calendar;
import java.util.Date;

public class Transaction {
    private String title;
    private float amount;
    private String note;
    private String category;
    private String dateDuration;
    private String type;
    private Calendar date;

    private boolean isRetainedSavings;

    private Date timestamp;

    // Constructor
    public Transaction(String title, float amount, String note, String category, String dateDuration, String type) {
        this.title = title;
        this.amount = amount;
        this.note = note;
        this.category = category;
        this.dateDuration = dateDuration;
        this.type = type;
        this.date = Calendar.getInstance();
        this.timestamp = new Date();

    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDateDuration() {
        return dateDuration;
    }

    public void setDateDuration(String dateDuration) {
        this.dateDuration = dateDuration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    // Getters and setters for the new field
    public boolean isRetainedSavings() {
        return isRetainedSavings;
    }

    public void setRetainedSavings(boolean retainedSavings) {
        isRetainedSavings = retainedSavings;
    }

    public void addWeekToDuration() {
        // Assuming date is a Calendar instance representing the transaction date
        date.add(Calendar.WEEK_OF_YEAR, 1);
    }


}