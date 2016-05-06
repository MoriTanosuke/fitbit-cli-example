import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import model.DataType;
import model.DateValue;
import model.Profile;
import model.TimeSeries;

public class FitbitConsoleApplication {
    private static final Logger LOG = Logger.getLogger(FitbitConsoleApplication.class.getName());
    private static final String clientConsumerKey = System.getProperty("CONSUMER_KEY");
    private static final String clientSecret = System.getProperty("CONSUMER_SECRET");
    public static final String BASE_URL = "https://api.fitbit.com/1/user/-";
    public static final String EXTENSION_JSON = ".json";

    private static String outputFile = null;
    private static final Gson GSON = new Gson();


    private enum TimePeriod {
        ONE_WEEK("1w"),
        THREE_MONTH("3m"),
        ONE_YEAR("1y");

        private final String value;

        TimePeriod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static TimePeriod findByShortForm(String value) {
            for (TimePeriod v : values()) {
                if (v.getValue().equals(value)) return v;
            }
            throw new IllegalArgumentException("Can not find value: " + value);
        }
    }

    // always parse input in US locale
    private static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) throws IOException {
        // default to 1 week
        TimePeriod period = TimePeriod.ONE_WEEK;

        if (args != null && args.length > 0) {
            if (args.length >= 1) {
                outputFile = args[0];
            }
            if (args.length >= 2) {
                period = TimePeriod.findByShortForm(args[1]);
                if (period == null) {
                    throw new IllegalArgumentException(
                            "Can not parse TimePeriod '" + args[1]
                                    + "'. Provide one of 1d, 7d, 30d, 1w, 3m, 6m, 1y or max.");
                }
            }
        }

        // TODO authorize
        OAuth20Service service = new ServiceBuilder()
                .apiKey(clientConsumerKey)
                .apiSecret(clientSecret)
                .build(FitbitApi.instance());
        LOG.info("=== Fitbits OAuth Workflow ===");

        Scanner in = new Scanner(System.in);
        // Obtain the Authorization URL
        LOG.info("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl();
        LOG.info("Got the Authorization URL!");
        System.out.println("Now go and authorize the application here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization code here");
        System.out.print(">> ");
        final String code = in.nextLine();

        // Trade the Request Token and Verfier for the Access Token
        LOG.info("Trading the Request Token for an Access Token...");
        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        LOG.info("Got the Access Token: " + accessToken);
        //TODO load user profile
        final OAuthRequest request = new OAuthRequest(Verb.GET, BASE_URL + "/profile.json", service);
        service.signRequest(accessToken, request);
        final Response response = request.send();
        Profile profile = GSON.fromJson(response.getBody(), Profile.class);
        System.out.println("Loading data for user " + profile.getUser().getDisplayName() + ", member since " + profile.getUser().getMemberSince());

        LOG.info("Loading data for " + period);
        // setup data to load
        final String startDate = today();
        final DataType[] typesToLoad = new DataType[]{
//                DataType.ACTIVITY_CALORIES,
//                DataType.AWAKENINGS_COUNT,
//                DataType.CALORIES_IN,
//                DataType.CALORIES_OUT,
                DataType.DISTANCE,
                DataType.SLEEP_EFFICIENCY,
//                DataType.ELEVATION,
//                DataType.FAT,
                DataType.FLOORS,
                DataType.MINUTES_AFTER_WAKEUP,
                DataType.MINUTES_ASLEEP,
                DataType.MINUTES_AWAKE,
                DataType.MINUTES_FAIRLY_ACTIVE,
                DataType.MINUTES_LIGHTLY_ACTIVE,
                DataType.MINUTES_SEDENTARY,
                DataType.MINUTES_TO_FALL_ASLEEP,
                DataType.MINUTES_VERY_ACTIVE,
                DataType.STEPS,
//                DataType.TIME_ENTERED_BED,
//                DataType.TIME_IN_BED,
//                DataType.WATER,
//                DataType.WEIGHT
        };

        // load over all types and load data
        final Comparator timeSeriesKeyComparator = new TimeSeriesKeyComparator();
        Map<DataType, List<DateValue>> data = new TreeMap<>(timeSeriesKeyComparator);
        for (DataType type : typesToLoad) {
            data.put(type, loadData(service, accessToken, startDate, type, period));
        }

        writeCsv(outputFile, data);

        LOG.info("All done.");
    }

    private static void writeCsv(String fileName, Map<DataType, List<DateValue>> data) throws IOException {
        Writer dest = new StringWriter();
        if (fileName != null) {
            dest = new FileWriter(fileName);
            LOG.info("Writing data to " + fileName);
        }
        final BufferedWriter writer = new BufferedWriter(dest);

        // transform into Map<DATE, Data[]>
        Map<String, List<Number>> dataPerDay = new TreeMap<>();
        writer.write("Date");
        for (DataType type : data.keySet()) {
            writer.write(";" + type);
            List<DateValue> series = data.get(type);
            for (DateValue day : series) {
                String dateTime = day.getDateTime();
                if (!dataPerDay.containsKey(dateTime)) {
                    dataPerDay.put(dateTime, new ArrayList<>());
                }

                // add to list
                List<Number> d = dataPerDay.get(dateTime);
                String value = day.getValue();
                try {
                    d.add(numberFormat.parse(value));
                } catch (ParseException e) {
                    LOG.severe("Can not parse value '" + value
                            + "' for type '" + type + "', using ZERO. Original message: "
                            + e.getMessage());
                    d.add(0);
                }
            }
        }
        writer.write("\n");

        // write data
        for (String dateTime : dataPerDay.keySet()) {
            List<Number> list = dataPerDay.get(dateTime);
            writer.write(dateTime);
            for (Number value : list) {
                writer.write(";" + value);
            }
            writer.write("\n");
        }
        writer.close();
    }

    private static List<DateValue> loadData(OAuth20Service service, OAuth2AccessToken accessToken, String startDate,
                                            DataType type,
                                            TimePeriod period) {
        LOG.info("Loading " + type.name() + "...");
        final List<DateValue> timeSeries = new ArrayList<>();

        final String url = buildUrlForTimeSeries(startDate, type, period);
        LOG.info("Requesting URL " + url);

        final OAuthRequest request = new OAuthRequest(Verb.GET, url, service);
        service.signRequest(accessToken, request);
        final Response response = request.send();

        Object o = GSON.fromJson(new StringReader(response.getBody()), type.getClazz());
        if (o instanceof TimeSeries) {
            TimeSeries values = (TimeSeries) o;
            if (values.getValues().isEmpty()) {
                LOG.info("No values received: " + o + " " + response.getBody());
            }
            timeSeries.addAll(values.getValues());
        } else {
            LOG.info("Not yet mapped: " + o + " " + response.getBody());
        }

        return timeSeries;
    }

    private static String buildUrlForTimeSeries(String startDate, DataType type, TimePeriod period) {
        return BASE_URL + "/" + type.getPath() + "/date/" + startDate + "/" + period.getValue() + EXTENSION_JSON;
    }

    private static String today() {
        return today(0);
    }

    private static String today(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return today(cal.getTime());
    }

    private static String today(Date date) {
        return dateFormat.format(date);
    }

}
