package model;

public class DateValue {
    private String dateTime;
    private String value;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DateValue{" +
                "dateTime='" + dateTime + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
