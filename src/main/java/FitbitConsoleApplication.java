import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.FitbitAPIEntityCache;
import com.fitbit.api.client.FitbitApiClientAgent;
import com.fitbit.api.client.FitbitApiCredentialsCache;
import com.fitbit.api.client.FitbitApiCredentialsCacheMapImpl;
import com.fitbit.api.client.FitbitApiEntityCacheMapImpl;
import com.fitbit.api.client.FitbitApiSubscriptionStorage;
import com.fitbit.api.client.FitbitApiSubscriptionStorageInMemoryImpl;
import com.fitbit.api.client.LocalUserDetail;
import com.fitbit.api.client.service.FitbitAPIClientService;
import com.fitbit.api.common.model.timeseries.Data;
import com.fitbit.api.common.model.timeseries.TimePeriod;
import com.fitbit.api.common.model.timeseries.TimeSeriesResourceType;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.common.service.FitbitApiService;
import com.fitbit.api.model.APIResourceCredentials;
import com.fitbit.api.model.FitbitUser;

public class FitbitConsoleApplication {
  private static final String apiBaseUrl = "api.fitbit.com";
  private static final String fitbitSiteBaseUrl = "http://www.fitbit.com";
  private static final String clientConsumerKey = System.getProperty("CONSUMER_KEY");
  private static final String clientSecret = System.getProperty("CONSUMER_SECRET");

  public static void main(String[] args) throws FitbitAPIException, IOException {
    FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMapImpl();
    FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMapImpl();
    FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemoryImpl();
    FitbitAPIClientService<FitbitApiClientAgent> fitbit = new FitbitAPIClientService<FitbitApiClientAgent>(
        new FitbitApiClientAgent(apiBaseUrl, fitbitSiteBaseUrl, credentialsCache), clientConsumerKey, clientSecret,
        credentialsCache, entityCache, subscriptionStore);

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

    // setup data to load
    LocalDate startDate = FitbitApiService.getValidLocalDateOrNull(today());
    // load all data we can get
    TimePeriod period = TimePeriod.MAX;
    TimeSeriesResourceType[] typesToLoad = new TimeSeriesResourceType[] { TimeSeriesResourceType.ACTIVE_SCORE,
        TimeSeriesResourceType.ACTIVITY_CALORIES, TimeSeriesResourceType.AWAKENINGS_COUNT,
        TimeSeriesResourceType.CALORIES_IN, TimeSeriesResourceType.CALORIES_OUT, TimeSeriesResourceType.DISTANCE,
        TimeSeriesResourceType.EFFICIENCY, TimeSeriesResourceType.ELEVATION, TimeSeriesResourceType.FAT,
        TimeSeriesResourceType.FLOORS, TimeSeriesResourceType.MINUTES_AFTER_WAKEUP,
        TimeSeriesResourceType.MINUTES_ASLEEP, TimeSeriesResourceType.MINUTES_AWAKE,
        TimeSeriesResourceType.MINUTES_FAIRLY_ACTIVE, TimeSeriesResourceType.MINUTES_LIGHTLY_ACTIVE,
        TimeSeriesResourceType.MINUTES_SEDENTARY, TimeSeriesResourceType.MINUTES_TO_FALL_ASLEEP,
        TimeSeriesResourceType.MINUTES_VERY_ACTIVE, TimeSeriesResourceType.STEPS,
        TimeSeriesResourceType.TIME_ENTERED_BED, TimeSeriesResourceType.TIME_IN_BED, TimeSeriesResourceType.WATER,
        TimeSeriesResourceType.WEIGHT };

    // load over all types and load data
    FitbitUser fitbitUser = new FitbitUser("-");
    Map<TimeSeriesResourceType, List<Data>> data = new HashMap<TimeSeriesResourceType, List<Data>>();
    for (TimeSeriesResourceType type : typesToLoad) {
      data.put(type, loadData(localUser, client, startDate, fitbitUser, type, period));
    }

    // transform into Map<DATE, Data[]>
    Map<String, List<String>> dataPerDay = new HashMap<String, List<String>>();
    System.out.print("Date");
    for (TimeSeriesResourceType type : data.keySet()) {
      System.out.print("\t" + type);
      List<Data> series = data.get(type);
      for (Data day : series) {
        String dateTime = day.getDateTime();
        if (!dataPerDay.containsKey(dateTime)) {
          dataPerDay.put(dateTime, new ArrayList<String>());
        }
        // add to list
        List<String> d = dataPerDay.get(dateTime);
        d.add(day.getValue());
      }
    }
    System.out.println();

    // print data
    for (String dateTime : dataPerDay.keySet()) {
      List<String> list = dataPerDay.get(dateTime);
      System.out.print(dateTime);
      for (String value : list) {
        System.out.print("\t" + value);
      }
      System.out.println();
    }
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

  private static void save(LocalUserDetail localUser, FitbitAPIClientService<FitbitApiClientAgent> fitbit) {
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

  private static List<Data> loadData(final LocalUserDetail localUser, FitbitApiClientAgent client, LocalDate startDate,
      FitbitUser fitbitUser, TimeSeriesResourceType type, TimePeriod period) throws FitbitAPIException {
    System.out.println("Loading " + type.name() + "...");
    List<Data> timeSeries = client.getTimeSeries(localUser, fitbitUser, type, startDate, period);
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
