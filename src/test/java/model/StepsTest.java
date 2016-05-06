package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;

import com.google.gson.Gson;
import org.junit.Test;

public class StepsTest {
    @Test
    public void parseJson() {
        Gson gson = new Gson();
        Steps steps = gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/activities-steps.json")), Steps.class);
        assertNotNull(steps);
        assertEquals(7, steps.getValues().size());
    }
}
