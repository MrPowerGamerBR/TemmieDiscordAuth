package com.mrpowergamerbr.temmiediscordauth.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class OAuthTokenResponse {
	@SerializedName("access_token")
	private String accessToken;
	@SerializedName("token_type")
	private String tokenType = null;
	@SerializedName("expires_in")
	private long expiresIn = 0;
	@SerializedName("refresh_token")
	private String refreshToken = null;
	private String scope = null;
}
