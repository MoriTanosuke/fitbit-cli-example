import java.io.IOException;

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
import com.fitbit.api.common.model.sleep.Sleep;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.common.service.FitbitApiService;
import com.fitbit.api.model.APIResourceCredentials;
import com.fitbit.api.model.FitbitUser;

public class FitbitConsoleApplication {
	private static final String apiBaseUrl = "api.fitbit.com";
	private static final String fitbitSiteBaseUrl = "http://www.fitbit.com";
	private static final String clientConsumerKey = System
			.getProperty("CONSUMER_KEY");
	private static final String clientSecret = System
			.getProperty("CONSUMER_SECRET");

	public static void main(String[] args) throws FitbitAPIException,
			IOException {
		FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMapImpl();
		FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMapImpl();
		FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemoryImpl();
		FitbitAPIClientService<FitbitApiClientAgent> fitbit = new FitbitAPIClientService<FitbitApiClientAgent>(
				new FitbitApiClientAgent(apiBaseUrl, fitbitSiteBaseUrl,
						credentialsCache), clientConsumerKey, clientSecret,
				credentialsCache, entityCache, subscriptionStore);

		final LocalUserDetail localUser = new LocalUserDetail("1");
		String url = fitbit.getResourceOwnerAuthorizationURL(localUser, "");
		System.out.println("Open " + url);
		System.out.print("Enter PIN:");
		String pin = readFromUser();

		APIResourceCredentials creds = fitbit
				.getResourceCredentialsByUser(localUser);
		creds.setTempTokenVerifier(pin);
		fitbit.getTokenCredentials(localUser);
		
		// you can save the access_token here to reuse later
//		System.out.println("Your access_token: " + creds.getAccessToken());
//		System.out.println("Your access_token_secret: " + creds.getAccessTokenSecret());

		UserInfo profile = fitbit.getClient().getUserInfo(localUser);
		System.out.println(profile.getDisplayName() + ", member since "
				+ profile.getMemberSince());

		LocalDate date = FitbitApiService.getValidLocalDateOrNull("2012-06-01");
		Sleep sleep = fitbit.getClient().getSleep(localUser,
				FitbitUser.CURRENT_AUTHORIZED_USER, date);
		System.out.println("Your sleep on " + date + ": inBed=" + sleep.getSummary().getTotalTimeInBed() + " asleep=" + sleep.getSummary().getTotalMinutesAsleep());
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
