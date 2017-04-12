package com.mrpowergamerbr.temmiediscordauth.exceptions;

public class DiscordAuthenticationException extends RuntimeException {
	public DiscordAuthenticationException(String reason) {
		super(reason);
	}
}
