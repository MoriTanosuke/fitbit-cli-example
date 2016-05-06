package model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SleepEfficiency extends TimeSeries {
    @SerializedName("sleep-efficiency")
    private List<DateValue> efficiencies = new ArrayList<>();

    @Override
    public List<DateValue> getValues() {
        return efficiencies;
    }
}
