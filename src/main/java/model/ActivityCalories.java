package model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ActivityCalories extends TimeSeries {
    @SerializedName("activities-activityCalories")
    private List<DateValue> values = new ArrayList<>();

    @Override
    public List<DateValue> getValues() {
        return values;
    }
}
