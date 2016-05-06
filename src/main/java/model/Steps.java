package model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Steps extends TimeSeries {
    @SerializedName("activities-steps")
    private List<DateValue> activiesSteps = new ArrayList<>();

    @Override
    public String toString() {
        return "Steps{" +
                "activiesSteps=" + activiesSteps +
                '}';
    }

    @Override
    public List<DateValue> getValues() {
        return activiesSteps;
    }
}
