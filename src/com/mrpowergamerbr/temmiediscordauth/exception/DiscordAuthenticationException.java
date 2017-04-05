package com.mrpowergamerbr.temmiediscordauth.exception;

public class DiscordAuthenticationException extends RuntimeException {
	public DiscordAuthenticationException(String reason) {
		super(reason);
	}
}
