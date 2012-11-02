package com.github.jknack.mwa;



/**
 * <p>
 * Interface to be implemented by factories, that can resolve a textual mode
 * into a Java representation, see {@link ApplicationMode}.
 * </p>
 * <p>
 * The 'dev' mode has special meaning, you can built your application by trusting
 * in dev or not dev mode.
 * <br>
 * For example, you can use an in-memory database if you're running in dev
 * mode.
 * </p>
 * 
 * @author pgaschuetz
 */
public interface ApplicationModeFactory<T extends ApplicationMode> {
	
	/**
	 * 
	 * @return a designated DEV mode
	 */
	public T createForDev();
	
	/**
	 * 
	 * @param mode a String representing the mode
	 * @return a {@link ApplicationMode} represented by the mode parameter
	 * @throws IllegalArgumentException if the mode cannot be converted
	 */
	public T createFor(String mode) throws IllegalArgumentException;
}
