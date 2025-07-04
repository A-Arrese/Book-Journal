package paagbat.model;

public class ReadingTrackerItem {
    private int userId;
    private int year;
    private int month;
    private int day;
    private String status;

    public ReadingTrackerItem(int userId, int year, int month, int day, String status) {
        this.userId = userId;
        this.year = year;
        this.month = month;
        this.day = day;
        this.status = status;
    }

    public int getUserId() { return userId; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public String getStatus() { return status; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setYear(int year) { this.year = year; }
    public void setMonth(int month) { this.month = month; }
    public void setDay(int day) { this.day = day; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "ReadingTrackerItem{" +
                "userId=" + userId +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", status='" + status + '\'' +
                '}';
    }
    
}