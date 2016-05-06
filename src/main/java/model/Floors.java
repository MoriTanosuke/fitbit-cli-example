package model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Floors extends TimeSeries {
    @SerializedName("activities-floors")
    private List<DateValue> floors = new ArrayList<>();

    @Override
    public List<DateValue> getValues() {
        return floors;
    }
}
