import static org.junit.Assert.assertTrue;

import model.DataType;
import org.junit.Test;

public class TimeSeriesKeyComparatorTest {
    @Test
    public void testComparisionBasedOnNames() {
        TimeSeriesKeyComparator comparator = new TimeSeriesKeyComparator();
        int result = comparator.compare(DataType.ACTIVITY_CALORIES, DataType.AWAKENINGS_COUNT);
        assertTrue("CALORIES_IN is not smaller than CALORIES_OUT: " + result, result < 0);
        result = comparator.compare(DataType.SLEEP_EFFICIENCY, DataType.ACTIVITY_CALORIES);
        assertTrue("WATER is not larger than SLEEP_EFFICIENCY: " + result, result > 0);
    }
}
