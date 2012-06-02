import java.io.Console;
import java.io.IOException;

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
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.model.APIResourceCredentials;

public class FitbitConsoleApplication {
	private static final String apiBaseUrl = "api.fitbit.com";
	private static final String fitbitSiteBaseUrl = "http://www.fitbit.com";
	private static final String clientConsumerKey = "YOUR_APPLICATION_KEY_HERE";
	private static final String clientSecret = "YOUR_APPLICATION_SECRET_HERE";

	static final LocalUserDetail localUser = new LocalUserDetail("1");

	public static void main(String[] args) throws FitbitAPIException,
			IOException {
		FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMapImpl();
		FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMapImpl();
		FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemoryImpl();
		FitbitAPIClientService<FitbitApiClientAgent> fitbit = new FitbitAPIClientService<FitbitApiClientAgent>(
				new FitbitApiClientAgent(apiBaseUrl, fitbitSiteBaseUrl,
						credentialsCache), clientConsumerKey, clientSecret,
				credentialsCache, entityCache, subscriptionStore);

		String url = fitbit.getResourceOwnerAuthorizationURL(localUser, "");
		System.out.println("Open " + url);
		System.out.print("Enter PIN:");
		String pin = readFromUser();

		APIResourceCredentials creds = fitbit
				.getResourceCredentialsByUser(localUser);
		creds.setTempTokenVerifier(pin);
		fitbit.getTokenCredentials(localUser);
		UserInfo profile = fitbit.getClient().getUserInfo(localUser);
		System.out.println(profile.getDisplayName() + ", member since " + profile.getMemberSince());

		LocalDate date;
		Sleep sleep = fitbit.getClient().getSleep(localUser, FitbitUser.CURRENT_AUTHORIZED_USER, FitbitApiService.getValidLocalDateOrNull("2012-06-01"));
	}

	private static String readFromUser() throws IOException {
		StringBuffer pin = new StringBuffer();
		int in = -1;
		while ((in = System.in.read()) != '\n') {
			pin.append((char) in);
		}
		return pin.toString();
	}

}
