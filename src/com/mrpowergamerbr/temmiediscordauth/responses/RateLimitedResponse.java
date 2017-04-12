package com.mrpowergamerbr.temmiediscordauth.responses;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

@Getter
public class RateLimitedResponse {
	private boolean global;
	private String message;
	@SerializedName("retry_after")
	private int retryAfter;
}
