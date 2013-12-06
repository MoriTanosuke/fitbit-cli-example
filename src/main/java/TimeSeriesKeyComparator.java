import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;

import java.util.Comparator;

/**
 * Compares {@link TimeSeriesResourceType} based on the name of the series.
 *
 * For example, {@link TimeSeriesResourceType#WATER} comes after {@link TimeSeriesResourceType#EFFICIENCY}.
 */
public class TimeSeriesKeyComparator implements Comparator<TimeSeriesResourceType> {
    @Override
    public int compare(TimeSeriesResourceType o1, TimeSeriesResourceType o2) {
        return o1.name().compareTo(o2.name());
    }
}
