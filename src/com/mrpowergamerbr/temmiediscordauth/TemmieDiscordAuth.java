package com.mrpowergamerbr.temmiediscordauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mrpowergamerbr.temmiediscordauth.exception.DiscordAuthenticationException;
import com.mrpowergamerbr.temmiediscordauth.exception.RateLimitedException;
import com.mrpowergamerbr.temmiediscordauth.response.CurrentUserResponse;
import com.mrpowergamerbr.temmiediscordauth.response.ErrorResponse;
import com.mrpowergamerbr.temmiediscordauth.response.OAuthTokenResponse;
import com.mrpowergamerbr.temmiediscordauth.response.RateLimitedResponse;
import com.mrpowergamerbr.temmiediscordauth.utils.TemmieGuild;

public class TemmieDiscordAuth {
	private String authCode;
	private String redirectUri;
	private String clientId;
	private String clientSecret;
	private String accessToken;
	private final static String API_BASE_URL = "https://discordapp.com/api";
	private final static String USER_IDENTIFICATION_URL = API_BASE_URL + "/users/@me";
	private final static String USER_GUILDS_URL = USER_IDENTIFICATION_URL + "/guilds";
	private final static String TOKEN_BASE_URL = API_BASE_URL + "/oauth2/token";
	private final static Gson gson = new Gson();
	private boolean waitOnRateLimit = true;
	private String refreshToken;
	
	/**
	 * Initializes a TemmieDiscordAuth instance
	 * 
	 * @param authCode authentication Code received on the URL callback, configure your URL callback on your Discord application config
	 * @param redirectUri your URL callback
	 * @param clientId your application client ID, get it on your Discord application config
	 * @param clientSecret your application secret, get it on your Discord application config
	 */
	public TemmieDiscordAuth(String authCode, String redirectUri, String clientId, String clientSecret) {
		this(authCode, redirectUri, clientId, clientSecret, true);
	}

	/**
	 * Initializes a TemmieDiscordAuth instance
	 * 
	 * @param authCode authentication Code received on the URL callback, configure your URL callback on your Discord application config
	 * @param redirectUri your URL callback
	 * @param clientId your application client ID, get it on your Discord application config
	 * @param clientSecret your application secret, get it on your Discord application config
	 * @param waitOnRateLimit if the client is rate limited, we block the current thread to try again, if false, the method throws an exception
	 */
	public TemmieDiscordAuth(String authCode, String redirectUri, String clientId, String clientSecret, boolean waitOnRateLimit) {
		this.authCode = authCode;
		this.redirectUri = redirectUri;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.waitOnRateLimit = waitOnRateLimit;
	}
	
	/**
	 * Starts a OAuth2 token exchange process
	 * 
	 * @return the authentication response
	 */
	public OAuthTokenResponse doTokenExchange() {
		Map<String, Object> payload = getAccessTokenPayload();
		payload.put("code", authCode);

		return doTokenExchange(payload);
	}

	/**
	 * Gets the OAuth2 access token
	 * 
	 * @return access token
	 */
	public String getAccessToken() {
		return doTokenExchange().getAccessToken();
	}

	/**
	 * Regenerates the access token using the stored refresh token
	 * 
	 * @return access token
	 */
	public OAuthTokenResponse doTokenExchangeUsingRefreshToken() {
		return doTokenExchangeUsingRefreshToken(refreshToken);
	}
	
	/**
	 * Regenerates the access token using a refresh token
	 * 
	 * @return access token
	 */
	public OAuthTokenResponse doTokenExchangeUsingRefreshToken(String refreshToken) {
		Map<String, Object> payload = getAccessTokenPayload();
		payload.put("refresh_token", refreshToken);

		return doTokenExchange(payload);
	}
	
	private Map<String, Object> getAccessTokenPayload() {
		Map<String, Object> payload = new HashMap<String, Object>();

		payload.put("grant_type", "authorization_code");
		payload.put("redirect_uri", redirectUri);
		payload.put("client_id", clientId);
		payload.put("client_secret", clientSecret);
		
		return payload;
	}
	
	private OAuthTokenResponse doTokenExchange(Map<String, Object> payload) {
		String url = TOKEN_BASE_URL;

		HttpRequest req = HttpRequest
				.post(url)
				.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11")
				.header("Content-Type", "application/x-www-form-urlencoded")
				// PAYLOAD IS NOT JSON
				.send(buildQuery(payload));

		String body = req.body();

		hasErrors(body);

		RateLimitedResponse rate = isRateLimited(body);
		
		if (rate != null) {
			if (waitOnRateLimit) {
				try {
					Thread.sleep(rate.getRetryAfter());
				} catch (InterruptedException e) { }
				return doTokenExchangeUsingRefreshToken(refreshToken);
			} else {
				throw new RateLimitedException();
			}
		}
		
		OAuthTokenResponse s = gson.fromJson(body, OAuthTokenResponse.class);

		this.accessToken = s.getAccessToken(); // Store Access Token for later use
		this.refreshToken = s.getRefreshToken(); // Store Refresh Token for later use
		
		return s;
	}
	
	
	/**
	 * Get the current user info
	 * 
	 * @return current user response
	 */
	public CurrentUserResponse getCurrentUserIdentification() {
		HttpRequest req = HttpRequest
				.get(USER_IDENTIFICATION_URL)
				.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Authorization", "Bearer " + accessToken);

		String body = req.body();

		hasErrors(body);

		RateLimitedResponse rate = isRateLimited(body);
		
		if (rate != null) {
			if (waitOnRateLimit) {
				try {
					Thread.sleep(rate.getRetryAfter());
				} catch (InterruptedException e) { }
				return getCurrentUserIdentification();
			} else {
				throw new RateLimitedException();
			}
		}
		
		CurrentUserResponse s = gson.fromJson(body, CurrentUserResponse.class);

		return s;
	}

	/**
	 * Get the current user guilds
	 * 
	 * @return a list with the user guilds
	 */
	public List<TemmieGuild> getUserGuilds() {
		HttpRequest req = HttpRequest
				.get(USER_GUILDS_URL)
				.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Authorization", "Bearer " + accessToken);

		String body = req.body();

		hasErrors(body);

		RateLimitedResponse rate = isRateLimited(body);
		
		if (rate != null) {
			if (waitOnRateLimit) {
				try {
					Thread.sleep(rate.getRetryAfter());
				} catch (InterruptedException e) { }
				return getUserGuilds();
			} else {
				throw new RateLimitedException();
			}
		}
		
		List<TemmieGuild> s = gson.fromJson(body, new TypeToken<List<TemmieGuild>>(){}.getType());

		return s;
	}

	private void hasErrors(String body) {
		try {
			ErrorResponse err = gson.fromJson(body, ErrorResponse.class);
			if (err.getError() != null || err.getMessage() != null) { throw new DiscordAuthenticationException(err.getError()); }
		} catch (Exception e) { } // dirty workaround ;)
	}

	private RateLimitedResponse isRateLimited(String body) {
		try {
			RateLimitedResponse err = gson.fromJson(body, RateLimitedResponse.class);
			if (err.getMessage().equals("You are being rate limited.") || err.getRetryAfter() != 0) {
				return err;
			}
		} catch (Exception e) { } // dirty workaround ;)
		return null;
	}

	private String buildQuery(Map<String, Object> params) {
		String[] query = new String[params.size()];
		int index = 0;
		for (String key : params.keySet()) {
			String val = String.valueOf(params.get(key) != null ? params.get(key) : "");
			try {
				val = URLEncoder.encode(val, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			query[index++] = key+"="+val;
		}

		return StringUtils.join(query, "&");
	}
}
