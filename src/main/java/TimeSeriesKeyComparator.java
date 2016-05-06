import java.util.Comparator;

import model.DataType;

/**
 * Compares {@link DataType} based on the name of the series.
 * <p>
 * For example, {@link DataType#WATER} comes after {@link DataType#SLEEP_EFFICIENCY}.
 */
public class TimeSeriesKeyComparator implements Comparator<DataType> {
    public int compare(DataType o1, DataType o2) {
        return o1.name().compareTo(o2.name());
    }
}
