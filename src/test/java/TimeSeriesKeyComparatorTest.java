import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;
import org.junit.*;

import static org.junit.Assert.*;

public class TimeSeriesKeyComparatorTest {
    @Test
    public void testComparisionBasedOnNames() {
        TimeSeriesKeyComparator comparator = new TimeSeriesKeyComparator();
        int result = comparator.compare(TimeSeriesResourceType.CALORIES_IN, TimeSeriesResourceType.CALORIES_OUT);
        assertTrue("CALORIES_IN is not smaller than CALORIES_OUT: " + result, result < 0);
        result = comparator.compare(TimeSeriesResourceType.WATER, TimeSeriesResourceType.EFFICIENCY);
        assertTrue("WATER is not larger than EFFICIENCY: " + result, result > 0);
    }
}
