package com.group7.eece411.A3;

public class NotFoundCmdException extends Exception {
	
	private String msg;
	public NotFoundCmdException(String msg) {
		super(msg);
		this.msg = msg;
	}
}
