import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.junit.Test;

public class NumberFormatTest {
    final String[] NUMBERS = {"82.488", "6311", "25", "44", "1931", "65", "3",
            "86", "0", "0.0", "0.0", "2", "181", "4113", "393",
            "54.864000000000004"};
    final double[] VALUES = {82.488, 6311, 25, 44, 1931, 65, 3,
            86, 0, 0.0, 0.0, 2, 181, 4113, 393,
            54.864000000000004};

    @Test
    public void test() throws ParseException {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
        for (int i = 0; i < NUMBERS.length; i++) {
            String number = NUMBERS[i];
            double parsedValue = numberFormat.parse(number).doubleValue();
            assertEquals(VALUES[i], parsedValue, 0.0001);
        }
    }

}
