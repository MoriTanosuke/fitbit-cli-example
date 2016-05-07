import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
import service.FitbitApi;

public class FitbitConsoleApplication {
    private static final Logger LOG = Logger.getLogger(FitbitConsoleApplication.class.getName());
    private static final String clientConsumerKey = System.getProperty("CONSUMER_KEY");
    private static final String clientSecret = System.getProperty("CONSUMER_SECRET");
    private static final String BASE_URL = "https://api.fitbit.com/1/user/-";
    private static final String EXTENSION_JSON = ".json";

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

    public static void main(String[] args) throws IOException, BackingStoreException {
        // default to 1 week
        TimePeriod period = TimePeriod.ONE_WEEK;
        String outputFile = "fitbit.csv";

        final List<String> arguments = new ArrayList<>();
        Arrays.stream(args).forEach(a -> arguments.add(a));
        LOG.info("Arguments: " + arguments);

        if (arguments.contains("--reset")) {
            // remove accessToken from preferences
            final Preferences prefs = Preferences.userNodeForPackage(FitbitConsoleApplication.class);
            prefs.clear();
            arguments.remove("--reset");
            LOG.info("Preferences cleared.");
        }

        if (!arguments.isEmpty()) {
            if (arguments.size() >= 1) {
                outputFile = args[0];
            }
            if (arguments.size() >= 2) {
                period = TimePeriod.findByShortForm(args[1]);
                if (period == null) {
                    throw new IllegalArgumentException(
                            "Can not parse TimePeriod '" + args[1]
                                    + "'. Provide one of 1d, 7d, 30d, 1w, 3m, 6m, 1y or max.");
                }
            }
        }

        final OAuth20Service service = new ServiceBuilder()
                .apiKey(clientConsumerKey)
                .apiSecret(clientSecret)
                .build(FitbitApi.instance());
        LOG.info("=== Fitbits OAuth Workflow ===");
        final OAuth2AccessToken accessToken = getAccessToken(service);

        LOG.info("Got the Access Token: " + accessToken);
        //TODO load user profile
        final OAuthRequest request = new OAuthRequest(Verb.GET, BASE_URL + "/profile.json", service);
        service.signRequest(accessToken, request);
        final Response response = request.send();
        final Profile profile = GSON.fromJson(response.getBody(), Profile.class);
        if (profile != null && profile.getUser() != null) {
            System.out.println("Loading data for user " + profile.getUser().getDisplayName() + ", member since " + profile.getUser().getMemberSince());
        } else {
            LOG.severe("Can not load user profile! (" + response.getBody() + ")");
        }

        LOG.info("Loading data for " + period);
        // setup data to load
        final String startDate = today();
        final DataType[] typesToLoad = new DataType[]{
                DataType.ACTIVITY_CALORIES,
                DataType.AWAKENINGS_COUNT,
                DataType.CALORIES_IN,
                DataType.CALORIES_OUT,
                DataType.DISTANCE,
                DataType.SLEEP_EFFICIENCY,
                DataType.ELEVATION,
                DataType.FAT,
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
                DataType.TIME_ENTERED_BED,
                DataType.TIME_IN_BED,
                DataType.WATER,
                DataType.WEIGHT
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

    private static OAuth2AccessToken getAccessToken(OAuth20Service service) {
        final Preferences prefs = Preferences.userNodeForPackage(FitbitConsoleApplication.class);
        byte[] storedAccessToken = prefs.getByteArray("accessToken", null);
        OAuth2AccessToken accessToken;
        if (storedAccessToken == null) {
            // authorize with fitbit if no accessToken found
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
            accessToken = service.getAccessToken(code);
            LOG.info("Storing access token for later");
            storedAccessToken = accessToken.getAccessToken().getBytes();
            prefs.putByteArray("accessToken", storedAccessToken);
        } else {
            LOG.info("Using stored access token");
            accessToken = new OAuth2AccessToken(new String(storedAccessToken));
        }

        return accessToken;

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
