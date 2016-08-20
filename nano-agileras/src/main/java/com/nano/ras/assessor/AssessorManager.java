package com.nano.ras.assessor;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.entity.SubscriberHistory;
import com.nano.jpa.entity.SubscriberState;
import com.nano.jpa.entity.ras.BorrowableAmount;
import com.nano.jpa.entity.ras.RasCriteria;
import com.nano.jpa.entity.ras.SmsMessage;
import com.nano.jpa.entity.ras.SubscriberAssessment;
import com.nano.jpa.enums.SmsMessageId;
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

	private List<BorrowableAmount> borrowableAmounts ;

	@Inject
	private QueryManager rasAssessmentDS;

	@PostConstruct
	public void init(){

		borrowableAmounts = rasAssessmentDS.getBorrowableAmountListDesc();
	}

	/**
	 * Initialize assessment process.
	 * 
	 * @param msisdn
	 */
	public void initAssessment(String msisdn){

		Subscriber subscriber = rasAssessmentDS.getSubscriberByMsisdn(msisdn);
		if(subscriber == null){
			log.warn("***********************SUBSCRIBER IS NULL WITH MSISDN:" + msisdn);
			return;
		}
		
		log.info("Subscriber MSISDN for RAS assessment:" + subscriber.getMsisdn());

		if(subscriber.getAssessment() == null)
			performFreshAssessment(subscriber);
		else
			reAssessment(subscriber);
	}

	/**
	 * Reassess {@link Subscriber} based on RAS criteria.
	 * 
	 * @param subscriber
	 */
	private void reAssessment(Subscriber subscriber) {
		// TODO Auto-generated method stub

		boolean eligible = false;

		SubscriberAssessment subscriberAssessment = subscriber.getAssessment();
		long minutes = new Timestamp(subscriberAssessment.getLastProcessed().getTime()).toLocalDateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES);
		log.info("Time of last assessment:" + minutes);
		if (minutes < 30L)
			return;

		subscriberAssessment.setLastProcessed(Timestamp.valueOf(LocalDateTime.now()));

		SubscriberState subscriberState = rasAssessmentDS.getSubscriberStateByMsisdn(subscriber.getMsisdn());
		if(subscriberState != null)
			subscriberAssessment.setTariffPlan(subscriberState.getPayType());

		List<SubscriberHistory> subscriberHistories = rasAssessmentDS.getSubscriberHistoryBySubscriber(subscriber);
		if (subscriberHistories == null || subscriberHistories.isEmpty())
			return;

		for (BorrowableAmount borrowableAmount : borrowableAmounts) {
			Map<String, Object> map = assessSubscriberEligibilityForAmount(subscriber, subscriberAssessment, borrowableAmount, subscriberHistories);
			subscriberAssessment = (SubscriberAssessment) map.get("subscriberAssessment");
			if ((boolean) map.get("eligible")){
				eligible = true;
				break;
			}
		}

		if (!eligible)
			rasAssessmentDS.update(subscriberAssessment);
		
		if (subscriberState == null)
			rasAssessmentDS.createSubscriberHistory(subscriber.getMsisdn(), BigDecimal.ZERO);
		
		log.info("Re assessment for subscriber:" + subscriber.getMsisdn() + " completed");
	}

	/**
	 * Perform first Subscriber assessment based on RAS criteria.
	 * 
	 * @param subscriber
	 */
	private void performFreshAssessment(Subscriber subscriber) {
		// TODO Auto-generated method stub

		boolean eligible = false;

		SubscriberState subscriberState = rasAssessmentDS.getSubscriberStateByMsisdn(subscriber.getMsisdn());
		SubscriberAssessment subscriberAssessment = rasAssessmentDS.createNewAssessment(subscriber, subscriberState);

		List<SubscriberHistory> subscriberHistories = rasAssessmentDS.getSubscriberHistoryBySubscriber(subscriber);
		if (subscriberHistories == null || subscriberHistories.isEmpty())
			return;

		for (BorrowableAmount borrowableAmount : borrowableAmounts) {
			Map<String, Object> map = assessSubscriberEligibilityForAmount(subscriber, subscriberAssessment, borrowableAmount, subscriberHistories);
			subscriberAssessment = (SubscriberAssessment) map.get("subscriberAssessment");
			if ((boolean) map.get("eligible")){
				eligible = true;
				break;
			}
		}

		if (!eligible)
			rasAssessmentDS.update(subscriberAssessment);
		
		if (subscriberState == null)
			rasAssessmentDS.createSubscriberHistory(subscriber.getMsisdn(), BigDecimal.ZERO);
		
		log.info("Fresh assessment for subscriber:" + subscriber.getMsisdn() + " completed");
	}

	/**
	 * Determine if {@link Subscriber} is eligible for {@link BorrowableAmount} argument.
	 * 
	 * @param subscriber
	 * @param subscriberAssessment
	 * @param borrowableAmount
	 * @param subscriberHistories
	 * @return true if eligible
	 */
	private Map<String, Object> assessSubscriberEligibilityForAmount(Subscriber subscriber, 
			SubscriberAssessment subscriberAssessment, BorrowableAmount borrowableAmount, 
			List<SubscriberHistory> subscriberHistories){

		subscriberAssessment = refreshSubscriberAssessment(subscriber, subscriberAssessment);
		RasCriteria rasCriteria = borrowableAmount.getCriteria();

		Map<String, Object> map = blacklistStatus(subscriberAssessment);
		boolean blacklist = (boolean) map.get("eligible");

		map = tarrifPlan((SubscriberAssessment) map.get("subscriberAssessment"));
		boolean tarrifPlan = (boolean) map.get("eligible");

		map = ageOnNetwork(subscriber, (SubscriberAssessment) map.get("subscriberAssessment"), rasCriteria, borrowableAmount, subscriberHistories);
		boolean ageOnNetwork = (boolean) map.get("eligible");

		map = numberOfTopupsForSpecifiedDuration((SubscriberAssessment) map.get("subscriberAssessment"), 
				rasCriteria, borrowableAmount, subscriberHistories);
		boolean numberOfTopUps = (boolean) map.get("eligible");

		map = cumulativeTopupAmountForSpecifiedDuration((SubscriberAssessment) map.get("subscriberAssessment"), 
				borrowableAmount, rasCriteria, subscriberHistories);
		boolean topUpAmount = (boolean) map.get("eligible");

		if (blacklist 
				&& tarrifPlan
				&& ageOnNetwork
				&& numberOfTopUps
				&& topUpAmount){
			rasAssessmentDS.update((SubscriberAssessment) map.get("subscriberAssessment"));
			return initializeResponse(true, subscriberAssessment);
		}

		return initializeResponse(false, subscriberAssessment);
	}

	/**
	 * Refresh SubscriberAssessment record in anticipation of new assessment.
	 * 
	 * @param subscriber
	 * @param subscriberAssessment
	 * @return {@link SubscriberAssessment}
	 */
	private SubscriberAssessment refreshSubscriberAssessment(Subscriber subscriber, 
			SubscriberAssessment subscriberAssessment){

		Long daysOnNetwork = 0L;

		java.util.Date activation = subscriber.getActivation();
		if (activation != null){
			daysOnNetwork = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - activation.getTime());
		}else{
			Timestamp timestamp = rasAssessmentDS.getLatestSubscriberHistoryTimeBySubscriber(subscriber);
			daysOnNetwork = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - timestamp.getTime());
		}

		log.debug("daysOnNetwork:" + daysOnNetwork);

		subscriberAssessment.setAgeOnNetwork(subscriberAssessment.getAgeOnNetwork() + daysOnNetwork.intValue());
		subscriberAssessment.setNumberOfTopUps(0);
		subscriberAssessment.setAssessmentInitTime(Timestamp.valueOf(LocalDateTime.now()));
		subscriberAssessment.setTotalTopUpValue(0);

		return subscriberAssessment;
	}

	/**
	 * Determine if {@link Subscriber} re-charge value meets required criteria for the {@link BorrowableAmount}.
	 * 
	 * @param subscriberAssessment
	 * @param borrowableAmount
	 * @param rasCriteria
	 * @param subscriberHistories
	 * @return true if {@link Subscriber} re-charge value satisfies minimum top-up value requirement
	 */
	private Map<String, Object> cumulativeTopupAmountForSpecifiedDuration(SubscriberAssessment subscriberAssessment, 
			BorrowableAmount borrowableAmount, RasCriteria rasCriteria, 
			List<SubscriberHistory> subscriberHistories){

		for (SubscriberHistory subscriberHistory : subscriberHistories) {
			Long days = getDays(subscriberHistory.getRechargeTime(), subscriberAssessment);
			if(days <= rasCriteria.getMinTopUpsDuration())
				subscriberAssessment.setTotalTopUpValue(subscriberAssessment.getTotalTopUpValue() + (subscriberHistory.getRechargeForPrepaid().multiply(new BigDecimal("100.00")).intValue()));
		}

		if(subscriberAssessment.getTotalTopUpValue() >= rasCriteria.getMinTopUpValue()){
			subscriberAssessment.setSmsMessage(null);
			subscriberAssessment.setMaxBorrowableAmount(borrowableAmount);
			return initializeResponse(true, subscriberAssessment);
		}

		SmsMessage smsMessage = getSmsMessage(subscriberAssessment);
		smsMessage.setTopupsAmountLeft(rasCriteria.getMinTopUpValue() - subscriberAssessment.getTotalTopUpValue());
		smsMessage.setMessageId(SmsMessageId.MSG_TOPUPS_AMOUNT_SCORING);
		smsMessage.setMinAllowedDays(rasCriteria.getMinTopUpsDuration());
		smsMessage.setMinAllowedTopupsAmount(rasCriteria.getMinTopUpValue());
		
		subscriberAssessment.setMaxBorrowableAmount(null);
		subscriberAssessment.setSmsMessage(smsMessage);

		return initializeResponse(false, subscriberAssessment);
	}

	/**
	 * Determine if {@link Subscriber} has recharged the required number of times for this {@link BorrowableAmount}.
	 * 
	 * @param subscriberAssessment
	 * @param rasCriteria
	 * @param borrowableAmount
	 * @param subscriberHistories
	 * @return true if {@link Subscriber} satisfies minimum number of top-ups requirement
	 */
	private Map<String, Object> numberOfTopupsForSpecifiedDuration(SubscriberAssessment subscriberAssessment, 
			RasCriteria rasCriteria, BorrowableAmount borrowableAmount, 
			List<SubscriberHistory> subscriberHistories){

		for (SubscriberHistory subscriberHistory : subscriberHistories) {
			Long days = getDays(subscriberHistory.getRechargeTime(), subscriberAssessment);
			if(days <= rasCriteria.getMinTopUpsDuration()) // gets top ups that happened with the specified days limit
				subscriberAssessment.setNumberOfTopUps(subscriberAssessment.getNumberOfTopUps() + 1);
		}

		if(subscriberAssessment.getNumberOfTopUps() >= rasCriteria.getMinTopUps()){
			subscriberAssessment.setSmsMessage(null);
			subscriberAssessment.setMaxBorrowableAmount(borrowableAmount);
			return initializeResponse(true, subscriberAssessment);
		}

		SmsMessage smsMessage = getSmsMessage(subscriberAssessment);
		smsMessage.setTopupsLeft(rasCriteria.getMinTopUps() - subscriberAssessment.getNumberOfTopUps());
		smsMessage.setMessageId(SmsMessageId.MSG_TOPUPS_AMOUNT_SCORING_SIMULATE);
		smsMessage.setMinAllowedTopups(rasCriteria.getMinTopUps());
		smsMessage.setMinAllowedDays(rasCriteria.getMinTopUpsDuration());
		
		subscriberAssessment.setMaxBorrowableAmount(null);
		subscriberAssessment.setSmsMessage(smsMessage);

		return initializeResponse(false, subscriberAssessment);
	}

	/**
	 * Determine if {@link Subscriber} has spent the required age on the network.
	 * 
	 * @param subscriber
	 * @param subscriberAssessment
	 * @param rasCriteria
	 * @param borrowableAmount
	 * @param subscriberHistories
	 * @return true if {@link Subscriber} satisfies age on network requirement
	 */
	private Map<String, Object> ageOnNetwork(Subscriber subscriber, 
			SubscriberAssessment subscriberAssessment, 
			RasCriteria rasCriteria, BorrowableAmount borrowableAmount, 
			List<SubscriberHistory> subscriberHistories){

		if(subscriberAssessment.getAgeOnNetwork() >= rasCriteria.getMinAgeOnNetwork()){
			subscriberAssessment.setMaxBorrowableAmount(borrowableAmount);
			subscriberAssessment.setSmsMessage(null);
			return initializeResponse(true, subscriberAssessment);
		}

		SmsMessage smsMessage = getSmsMessage(subscriberAssessment);
		smsMessage.setDaysLeft(rasCriteria.getMinAgeOnNetwork() - subscriberAssessment.getAgeOnNetwork());
		smsMessage.setMessageId(SmsMessageId.MSG_NETWORK_LIFETIME_SCORING);
		smsMessage.setMinAllowedDays(rasCriteria.getMinTopUpsDuration());

		subscriberAssessment.setMaxBorrowableAmount(null);
		subscriberAssessment.setSmsMessage(smsMessage);

		return initializeResponse(false, subscriberAssessment);
	}

	/**
	 * Confirm {@link Subscriber} blacklist status on the network.
	 * 
	 * @param subscriberAssessment
	 * @return true if {@link Subscriber} is not black listed
	 */
	private Map<String, Object> blacklistStatus(SubscriberAssessment subscriberAssessment){

		return initializeResponse(true, subscriberAssessment);
	}

	/**
	 * Confirm {@link Subscriber} tarrifPlan conforms to expected criteria.
	 * 
	 * @param subscriberAssessment
	 * @return true if {@link Subscriber} tariff plan satisfies criteria requirement
	 */
	private Map<String, Object> tarrifPlan(SubscriberAssessment subscriberAssessment){

		return initializeResponse(true, subscriberAssessment);
	}

	/**
	 * Create response for Assessment APIs.
	 * 
	 * @param eligible
	 * @param subscriberAssessment
	 * @return Map containing eligibility status and updated {@link SubscriberAssessment}
	 */
	private Map<String, Object> initializeResponse(Boolean eligible, 
			SubscriberAssessment subscriberAssessment){

		Map<String, Object> map = new HashMap<>();
		map.put("eligible", eligible);
		map.put("subscriberAssessment", subscriberAssessment);

		return map;
	}

	/**
	 * Fetch existing SmsMessage or initialize new instance.
	 * 
	 * @param subscriberAssessment
	 * @return {@link SmsMessage}
	 */
	protected SmsMessage getSmsMessage(SubscriberAssessment subscriberAssessment){

		return subscriberAssessment.getSmsMessage() == null ? new SmsMessage() : subscriberAssessment.getSmsMessage();
	}

	/**
	 * Calculate number of days in a date range.
	 * 
	 * @param rechargeTime
	 * @param subscriberAssessment
	 * @return long
	 */
	protected Long getDays(Timestamp rechargeTime, 
			SubscriberAssessment subscriberAssessment){

		return ChronoUnit.DAYS.between(new Date(rechargeTime.getTime()).toLocalDate(), new Date(subscriberAssessment.getAssessmentInitTime().getTime()).toLocalDate());
		//return TimeUnit.MILLISECONDS.toDays(rechargeTime.getTime() - subscriberAssessment.getAssessmentInitTime().getTime());
	}

}