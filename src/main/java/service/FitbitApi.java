package service;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.utils.OAuthEncoder;

public class FitbitApi extends DefaultApi20 {
    private static final String DEFAULT_SCOPE = String.join(" ", "sleep", "nutrition", "activity", "profile", "weight");
    public static final String AUTHORIZATION_URL = "https://www.fitbit.com/oauth2/authorize?client_id=%s&response_type=%s&scope=%s";
    public static final String RESPONSE_TYPE = "code";

    protected FitbitApi() {
    }

    private static class InstanceHolder {
        private static final FitbitApi INSTANCE = new FitbitApi();
    }

    public static FitbitApi instance() {
        return InstanceHolder.INSTANCE;
    }

    public String getAccessTokenEndpoint() {
        return String.format("https://api.fitbit.com/oauth2/token?grant_type=%s", OAuthConstants.AUTHORIZATION_CODE);
    }

    public String getAuthorizationUrl(OAuthConfig config) {
        return String.format(AUTHORIZATION_URL, config.getApiKey(), OAuthEncoder.encode(RESPONSE_TYPE), OAuthEncoder.encode(DEFAULT_SCOPE));
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public OAuth20Service createService(OAuthConfig config) {
        return new FitbitOAuth20Service(this, config);
    }
}
