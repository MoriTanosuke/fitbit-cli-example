package model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Distance extends TimeSeries {
    @SerializedName("activities-distance")
    private List<DateValue> distances = new ArrayList<>();

    @Override
    public List<DateValue> getValues() {
        return distances;
    }
}
