package com.github.jknack.mwa;

public enum DefaultApplicationMode implements ApplicationMode {
	DEV,
	TEST,
	PROD;
	
	public boolean isDev() {
		return DEV.equals(this);
	}
	
	@Override
	public String getName() {
		return name();
	}
}
