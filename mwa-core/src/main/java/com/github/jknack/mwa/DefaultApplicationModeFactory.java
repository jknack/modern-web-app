package com.github.jknack.mwa;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default factory working with {@link DefaultApplicationMode}
 * 
 * @author pgaschuetz
 */
public class DefaultApplicationModeFactory implements IApplicationModeFactory<DefaultApplicationMode> {
	
	private final static Logger logger = LoggerFactory.getLogger(DefaultApplicationModeFactory.class);
	
	@Override
	public DefaultApplicationMode createForDev() {
		return DefaultApplicationMode.DEV;
	}

	@Override
	public DefaultApplicationMode createFor(String mode) throws IllegalArgumentException {
		
		Validate.notBlank(mode, "Mode may not be empty or null");
		
		for(DefaultApplicationMode aMode : DefaultApplicationMode.values()) {
			if(aMode.getName().equalsIgnoreCase(mode)) {
				return aMode;
			}
		}
		
		final String msg = String.format("No suitable ApplicationMode found for String [%s]", mode);
		if(logger.isTraceEnabled()) {
			logger.trace(msg);
		}
		throw new IllegalArgumentException(msg);
	}

}
