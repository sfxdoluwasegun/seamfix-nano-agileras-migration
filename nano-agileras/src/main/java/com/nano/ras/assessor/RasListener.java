package com.nano.ras.assessor;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

/**
 * {@link JMSConsumer} for RAS Queue.
 * 
 * @author segz
 *
 */

@MessageDriven(mappedName = "agileRasQueue", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"), 
		@ActivationConfigProperty(propertyName="destination", propertyValue="agileRasQueue"), 
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@ResourceAdapter("activemq")
public class RasListener implements MessageListener {
	
	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private AssessorManager assessorManager ;

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		
		try {
			assessorManager.initAssessment(((MapMessage) message).getString("msisdn"));
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

}