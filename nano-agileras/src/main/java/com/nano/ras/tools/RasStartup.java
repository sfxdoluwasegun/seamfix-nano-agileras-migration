package com.nano.ras.tools;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.logging.Logger;

@Startup
@Singleton
public class RasStartup {
	
	private Logger log = Logger.getLogger(getClass());

	@PostConstruct
	public void init(){
		
		log.info("Hello RAS");
	}

}
