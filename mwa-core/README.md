# MWA Core Module

This module add useful addition to Spring through the ```ApplicationContextConfigurer``` class:

* Read ```application.mode``` from the environment and creates a ```com.github.jknack.mwa.Mode``` bean
* Enable ```com.github.jknack.mwa.ModeAware``` contract
* Active the ```application.mode``` as Spring Profile
* Enable the injection of environment properties using ```@Named("prop")``` and ```@Value("${prop}")```