package com.globant.todomvc;

import com.github.jknack.mwa.Startup;
import com.github.jknack.mwa.jpa.JpaModule;
import com.github.jknack.mwa.wro4j.WroModule;

public class TodoMVC extends Startup {

	@Override
	protected Class<?>[] imports() {
		return new Class<?>[]{JpaModule.class, WroModule.class};
	}

	@Override
	protected String[] namespace() {
		return new String[]{"com.globant", "com.globant.todomvc"};
	}
}
