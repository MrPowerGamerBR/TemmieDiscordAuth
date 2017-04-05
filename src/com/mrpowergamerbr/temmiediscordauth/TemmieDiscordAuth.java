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
import com.mrpowergamerbr.temmiediscordauth.response.CurrentUserResponse;
import com.mrpowergamerbr.temmiediscordauth.response.ErrorResponse;
import com.mrpowergamerbr.temmiediscordauth.response.OAuthTokenResponse;
import com.mrpowergamerbr.temmiediscordauth.utils.TemmieGuild;

public class TemmieDiscordAuth {
	private String authCode;
	private String redirectUri;
	private String clientId;
	private String clientSecret;
	private String accessToken;
	private final static String API_BASE_URL = "https://discordapp.com/api";
	private final static String TOKEN_BASE_URL = "/oauth2/token";
	private final static Gson gson = new Gson();

	/**
	 * Initializes a TemmieDiscordAuth instance
	 * 
	 * @param authCode authentication Code received on the URL callback, configure your URL callback on your Discord application config
	 * @param redirectUri your URL callback
	 * @param clientId your application client ID, get it on your Discord application config
	 * @param clientSecret your application secret, get it on your Discord application config
	 */
	public TemmieDiscordAuth(String authCode, String redirectUri, String clientId, String clientSecret) {
		this.authCode = authCode;
		this.redirectUri = redirectUri;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	/**
	 * Starts a OAuth2 token exchange process
	 * 
	 * @return the authentication response
	 */
	public OAuthTokenResponse doTokenExchange() {
		String url = API_BASE_URL + TOKEN_BASE_URL;

		Map<String, Object> payload = new HashMap<String, Object>();

		payload.put("grant_type", "authorization_code");
		payload.put("code", authCode);
		payload.put("redirect_uri", redirectUri);
		payload.put("client_id", clientId);
		payload.put("client_secret", clientSecret);

		HttpRequest req = HttpRequest
				.post(url)
				.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11")
				.header("Content-Type", "application/x-www-form-urlencoded")
				// PAYLOAD IS NOT FUCKING JSON
				.send(buildQuery(payload));

		String body = req.body();

		hasErrors(body);

		OAuthTokenResponse s = gson.fromJson(body, OAuthTokenResponse.class);

		this.accessToken = s.getAccessToken();
		
		return s;
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
	 * Get the current user info
	 * 
	 * @return current user response
	 */
	public CurrentUserResponse getCurrentUserIdentification() {
		HttpRequest req = HttpRequest
				.get(API_BASE_URL + "/users/@me")
				.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Authorization", "Bearer " + accessToken);

		String body = req.body();

		hasErrors(body);

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
				.get(API_BASE_URL + "/users/@me/guilds")
				.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Authorization", "Bearer " + accessToken);

		String body = req.body();

		System.out.println(body);
		
		hasErrors(body);

		List<TemmieGuild> s = gson.fromJson(body, new TypeToken<List<TemmieGuild>>(){}.getType());

		return s;
	}
	
	private void hasErrors(String body) {
		try {
		ErrorResponse err = gson.fromJson(body, ErrorResponse.class);
		if (err.getError() != null) { throw new DiscordAuthenticationException(err.getError()); }
		} catch (Exception e) { } // dirty workaround ;)
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
