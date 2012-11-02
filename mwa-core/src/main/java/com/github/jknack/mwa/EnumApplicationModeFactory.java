package com.github.jknack.mwa;

import java.util.EnumSet;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract factory working with enums
 * 
 * @author pgaschuetz
 */
public class EnumApplicationModeFactory<T extends Enum<T> & ApplicationMode> implements ApplicationModeFactory<T> {
	
	private final static Logger logger = LoggerFactory.getLogger(EnumApplicationModeFactory.class);
	
	private final T devMode;
	private final EnumSet<T> enumValues;
	
	public EnumApplicationModeFactory(Class<T> enumClazz) {
		enumValues = EnumSet.allOf(enumClazz);
		for(T mode : enumValues) {
			if(mode.isDev()) {
				devMode = mode;
				return;
			}
		}
		
		final String msg = String.format("No enum field found returning isDev()==true in enum [%s]", enumClazz); 
		throw new IllegalArgumentException(msg);
	}
	
	
	@Override
	public T createForDev() {
		return devMode;
	}

	@Override
	public T createFor(String mode) throws IllegalArgumentException {
		
		Validate.notBlank(mode, "Mode may not be empty or null");
	
		for(T modeValue : enumValues) {
			if(modeValue.getName().equalsIgnoreCase(mode)) {
				return modeValue;
			}
		}
		
		final String msg = String.format("No suitable ApplicationMode found for String [%s]", mode);
		if(logger.isTraceEnabled()) {
			logger.trace(msg);
		}
		throw new IllegalArgumentException(msg);
	}

}
