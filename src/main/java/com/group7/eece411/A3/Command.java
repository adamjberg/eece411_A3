package com.group7.eece411.A3;

public abstract class Command {
	
	private Datastore ds;
	
	public Command(Datastore ds) {
		this.ds = ds;
	}
	
	public abstract void execute();
}
