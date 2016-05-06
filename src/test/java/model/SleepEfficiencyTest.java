package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;

import com.google.gson.Gson;
import org.junit.Test;

public class SleepEfficiencyTest {
    @Test
    public void canParseJson() {
        Gson gson = new Gson();
        SleepEfficiency model = gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/sleep-efficiency.json")), SleepEfficiency.class);
        assertNotNull(model);
        assertEquals(7, model.getValues().size());
        assertEquals("2016-05-06", model.getValues().get(6).getDateTime());
    }
}