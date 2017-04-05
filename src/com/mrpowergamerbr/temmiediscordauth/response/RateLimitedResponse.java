package com.mrpowergamerbr.temmiediscordauth.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class RateLimitedResponse {
	private boolean global;
	private String message;
	@SerializedName("retry_after")
	private int retryAfter;
}
