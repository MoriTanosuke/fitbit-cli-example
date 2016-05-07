package model;

public enum DataType {
    ACTIVITY_CALORIES("ACTIVITY_CALORIES", "activities/activityCalories", ActivityCalories.class),
    DISTANCE("DISTANCE", "activities/distance", Distance.class),
    ELEVATION("ELEVATION", "activities/elevation", Elevation.class),
    FLOORS("FLOORS", "activities/floors", Floors.class),
    MINUTES_FAIRLY_ACTIVE("MINUTES_FAIRLY_ACTIVE", "activities/minutesFairlyActive", ActivityFairlyActive.class),
    MINUTES_LIGHTLY_ACTIVE("MINUTES_LIGHTLY_ACTIVE", "activities/minutesLightlyActive", ActivityLightlyActive.class),
    MINUTES_SEDENTARY("MINUTES_SEDENTARY", "activities/minutesSedentary", ActivitySedentary.class),
    MINUTES_VERY_ACTIVE("MINUTES_VERY_ACTIVE", "activities/minutesVeryActive", ActivityVeryActive.class),
    STEPS(", Steps", "activities/steps", Steps.class),
    SLEEP_EFFICIENCY("SLEEP_EFFICIENCY", "sleep/efficiency", SleepEfficiency.class),
    AWAKENINGS_COUNT("AWAKENINGS_COUNT", "sleep/awakeningsCount", SleepAwakeningsCount.class),
    MINUTES_AFTER_WAKEUP("MINUTES_AFTER_WAKEUP", "sleep/minutesAfterWakeup", SleepMinutesAfterWakeup.class),
    MINUTES_ASLEEP("MINUTES_ASLEEP", "sleep/minutesAsleep", SleepMinutesAsleep.class),
    MINUTES_AWAKE("MINUTES_AWAKE", "sleep/minutesAwake", SleepMinutesAwake.class),
    MINUTES_TO_FALL_ASLEEP("MINUTES_TO_FALL_ASLEEP", "sleep/minutesToFallAsleep", SleepMinutesToFallAsleep.class),
    TIME_ENTERED_BED("TIME_ENTERED_BED", "sleep/startTime", SleepStartTime.class),
    TIME_IN_BED("TIME_IN_BED", "sleep/timeInBed", SleepTimeInBed.class),
    CALORIES_IN("CALORIES_IN", "foods/log/caloriesIn", CaloriesIn.class),
    CALORIES_OUT("CALORIES_OUT", "foods/log/caloriesOut", CaloriesOut.class),
    FAT("FAT", "body/fat", Fat.class),
    WATER("WATER", "foods/logs/water", Water.class),
    WEIGHT("WEIGHT", "body/weight", Weight.class);

    private final String name;
    private final String path;
    private final Class clazz;

    DataType(String name, String path, Class clazz) {
        this.name = name;
        this.path = path;
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}