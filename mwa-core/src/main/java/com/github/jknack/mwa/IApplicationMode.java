package com.github.jknack.mwa;

/**
 * Interface to be implemented by classes that represent an application mode,
 * ie. DEVEL, TEST, PROD, etc.
 * 
 * An enum would be a typical choice as an implementator. If not using enums,
 * make sure to implement equals method properly. 
 * 
 * @author pgaschuetz
 */
public interface IApplicationMode {
	
	/**
	 * 
	 * @return a human readable form of the mode - may NOT return null or empty/blank
	 */
	public String getName();
	
	/**
	 * 
	 * @return true if this mode is the designated development mode
	 */
	public boolean isDev();
}
