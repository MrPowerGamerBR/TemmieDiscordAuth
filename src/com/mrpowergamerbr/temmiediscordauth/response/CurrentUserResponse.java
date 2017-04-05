package com.mrpowergamerbr.temmiediscordauth.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class CurrentUserResponse {
	private String id;
	private String username;
	private String discriminator;
	private String avatar;
	private boolean bot;
	@SerializedName("mfa_enabled")
	private boolean twoFactorAuthenticationEnabled;
	private boolean verified;
	private String email;
}
