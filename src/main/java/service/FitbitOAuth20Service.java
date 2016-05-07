package service;

import java.util.Base64;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.AbstractRequest;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.oauth.OAuth20Service;

public class FitbitOAuth20Service extends OAuth20Service {
    private final byte[] basic;

    public FitbitOAuth20Service(DefaultApi20 api, OAuthConfig config) {
        super(api, config);
        this.basic = Base64.getEncoder().encode(new String(getConfig().getApiKey() + ":" + getConfig().getApiSecret()).getBytes());
    }

    @Override
    protected <T extends AbstractRequest> T createAccessTokenRequest(String code, T request) {
        request.addHeader("Authorization", "Basic " + new String(basic));
        request.addParameter("code", code);
        return request;
    }

    @Override
    public void signRequest(OAuth2AccessToken accessToken, AbstractRequest request) {
        request.addHeader("Authorization", "Bearer  " + accessToken.getAccessToken());
    }

}
