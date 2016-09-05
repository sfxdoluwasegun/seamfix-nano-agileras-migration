package com.nano.ras.assessor;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.entity.ras.SubscriberAssessment;
import com.nano.ras.AssessmentManager;
import com.nano.ras.tools.QueryManager;

/**
 * Manages {@link Subscriber} assessment based on RAS criteria.
 * 
 * @author segz
 *
 */

@Stateless
public class AssessorManager {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private QueryManager rasAssessmentDS;
	
	@Inject
	private AssessmentManager assessmentManager ;

	/**
	 * Initialize assessment process.
	 * 
	 * @param msisdn
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void initAssessment(String msisdn){

		Subscriber subscriber = rasAssessmentDS.getSubscriberByMsisdn(msisdn);
		if(subscriber == null){
			log.warn("***********************SUBSCRIBER IS NULL WITH MSISDN:" + msisdn);
			return;
		}
		
		log.info("Subscriber MSISDN for RAS assessment:" + subscriber.getMsisdn());
		SubscriberAssessment subscriberAssessment = rasAssessmentDS.getSubscriberAssessmentBySubscriber(subscriber);

		if(subscriberAssessment == null)
			assessmentManager.performFreshAssessment(subscriber);
		else
			assessmentManager.reAssessment(subscriber);
	}

}