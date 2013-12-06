import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.*;
import com.fitbit.api.client.service.FitbitAPIClientService;
import com.fitbit.api.common.model.timeseries.Data;
import com.fitbit.api.common.model.timeseries.TimePeriod;
import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.common.service.FitbitApiService;
import com.fitbit.api.model.APIResourceCredentials;
import com.fitbit.api.model.FitbitUser;
import org.joda.time.LocalDate;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FitbitConsoleApplication {
    private static final String apiBaseUrl = "api.fitbit.com";
    private static final String fitbitSiteBaseUrl = "http://www.fitbit.com";
    private static final String clientConsumerKey = System.getProperty("CONSUMER_KEY");
    private static final String clientSecret = System.getProperty("CONSUMER_SECRET");

    private static String outputFile = null;
    // load all data that we can get by default
    private static TimePeriod period = TimePeriod.MAX;

    // always parse input in US locale
    private static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

    public static void main(String[] args) throws FitbitAPIException, IOException {
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

        FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMapImpl();
        FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMapImpl();
        FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemoryImpl();
        FitbitAPIClientService<FitbitApiClientAgent> fitbit = new FitbitAPIClientService<FitbitApiClientAgent>(
                new FitbitApiClientAgent(apiBaseUrl, fitbitSiteBaseUrl, credentialsCache),
                clientConsumerKey, clientSecret, credentialsCache, entityCache, subscriptionStore);

        final LocalUserDetail localUser = new LocalUserDetail("1");

        // if (load(localUser, fitbit) == null) {
        String url = fitbit.getResourceOwnerAuthorizationURL(localUser, "");
        System.out.println("Open " + url);
        System.out.print("Enter PIN:");
        String pin = readFromUser();
        APIResourceCredentials creds = fitbit.getResourceCredentialsByUser(localUser);
        creds.setTempTokenVerifier(pin);
        // }
        fitbit.getTokenCredentials(localUser);
        // save(localUser, fitbit);

        FitbitApiClientAgent client = fitbit.getClient();
        UserInfo profile = client.getUserInfo(localUser);
        System.out.println(profile.getDisplayName() + ", member since " + profile.getMemberSince());
        System.out.println("Loading data for " + period);

        // setup data to load
        LocalDate startDate = FitbitApiService.getValidLocalDateOrNull(today());
        TimeSeriesResourceType[] typesToLoad = new TimeSeriesResourceType[]{
                TimeSeriesResourceType.ACTIVE_SCORE,
                TimeSeriesResourceType.ACTIVITY_CALORIES,
                TimeSeriesResourceType.AWAKENINGS_COUNT,
                TimeSeriesResourceType.CALORIES_IN,
                TimeSeriesResourceType.CALORIES_OUT,
                TimeSeriesResourceType.DISTANCE,
                TimeSeriesResourceType.EFFICIENCY,
                TimeSeriesResourceType.ELEVATION,
                TimeSeriesResourceType.FAT,
                TimeSeriesResourceType.FLOORS,
                TimeSeriesResourceType.MINUTES_AFTER_WAKEUP,
                TimeSeriesResourceType.MINUTES_ASLEEP,
                TimeSeriesResourceType.MINUTES_AWAKE,
                TimeSeriesResourceType.MINUTES_FAIRLY_ACTIVE,
                TimeSeriesResourceType.MINUTES_LIGHTLY_ACTIVE,
                TimeSeriesResourceType.MINUTES_SEDENTARY,
                TimeSeriesResourceType.MINUTES_TO_FALL_ASLEEP,
                TimeSeriesResourceType.MINUTES_VERY_ACTIVE,
                TimeSeriesResourceType.STEPS,
                TimeSeriesResourceType.TIME_ENTERED_BED,
                TimeSeriesResourceType.TIME_IN_BED,
                TimeSeriesResourceType.WATER,
                TimeSeriesResourceType.WEIGHT};

        // load over all types and load data
        FitbitUser fitbitUser = new FitbitUser("-");
        TimeSeriesKeyComparator timeSeriesKeyComparator = new TimeSeriesKeyComparator();
        Map<TimeSeriesResourceType, List<Data>> data = new TreeMap<TimeSeriesResourceType, List<Data>>(timeSeriesKeyComparator);
        for (TimeSeriesResourceType type : typesToLoad) {
            data.put(
                    type,
                    loadData(localUser, client, startDate, fitbitUser, type, period));
        }

        Writer dest = new StringWriter();
        if (outputFile != null) {
            dest = new FileWriter(outputFile);
            System.out.println("Writing data to " + outputFile);
        }
        final BufferedWriter writer = new BufferedWriter(dest);

        // transform into Map<DATE, Data[]>
        Map<String, List<Number>> dataPerDay = new TreeMap<String, List<Number>>();
        writer.write("Date");
        for (TimeSeriesResourceType type : data.keySet()) {
            writer.write("\t" + type);
            List<Data> series = data.get(type);
            for (Data day : series) {
                String dateTime = day.getDateTime();
                if (!dataPerDay.containsKey(dateTime)) {
                    dataPerDay.put(dateTime, new ArrayList<Number>());
                }

                // add to list
                List<Number> d = dataPerDay.get(dateTime);
                String value = day.getValue();
                try {
                    d.add(numberFormat.parse(value));
                } catch (ParseException e) {
                    System.err.println("Can not parse value '" + value
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
                writer.write("\t" + value);
            }
            writer.write("\n");
        }
        writer.close();

        System.out.println("All done.");
    }

    private static APIResourceCredentials load(LocalUserDetail localUser,
                                               FitbitAPIClientService<FitbitApiClientAgent> fitbit) {
        APIResourceCredentials creds = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader("token.properties"));
            String accessToken = in.readLine();
            in.close();
            BufferedReader in2 = new BufferedReader(new FileReader("secret.properties"));
            String accessTokenSecret = in2.readLine();
            in2.close();
            creds = new APIResourceCredentials(localUser.getUserId(), "", "");
            // creds = fitbit.getResourceCredentialsByUser(localUser);
            creds.setAccessToken(accessToken);
            creds.setAccessTokenSecret(accessTokenSecret);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return creds;
    }

    private static void save(LocalUserDetail localUser,
                             FitbitAPIClientService<FitbitApiClientAgent> fitbit) {
        try {
            FileWriter out = new FileWriter("token.properties");
            out.write(fitbit.getResourceCredentialsByUser(localUser).getAccessToken());
            out.close();
            FileWriter out2 = new FileWriter("secret.properties");
            out2.write(fitbit.getResourceCredentialsByUser(localUser).getAccessTokenSecret());
            out2.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static List<Data> loadData(final LocalUserDetail localUser,
                                       FitbitApiClientAgent client, LocalDate startDate,
                                       FitbitUser fitbitUser, TimeSeriesResourceType type,
                                       TimePeriod period) throws FitbitAPIException {
        System.out.println("Loading " + type.name() + "...");
        List<Data> timeSeries = client.getTimeSeries(localUser, fitbitUser, type, startDate, period);
        /*
		 * System.out.print(">>> " + type + " "); for (Data data : timeSeries) {
		 * System.out.print(data.getDateTime() + ":" + data.getValue() + " "); }
		 * System.out.println();
		 */
        return timeSeries;
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
        return new SimpleDateFormat(FitbitApiService.LOCAL_DATE_PATTERN).format(date);
    }

    private static String readFromUser() throws IOException {
        StringBuffer pin = new StringBuffer();
        int in = -1;
        while ((in = System.in.read()) != '\n') {
            pin.append((char) in);
        }
        return pin.toString().trim();
    }
}
