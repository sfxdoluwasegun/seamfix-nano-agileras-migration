package com.nano.ras.tools;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.Subscriber;
import com.nano.jpa.entity.SubscriberHistory;
import com.nano.jpa.entity.SubscriberHistory_;
import com.nano.jpa.entity.SubscriberState;
import com.nano.jpa.entity.SubscriberState_;
import com.nano.jpa.entity.Subscriber_;
import com.nano.jpa.entity.ras.BorrowableAmount;
import com.nano.jpa.entity.ras.BorrowableAmount_;
import com.nano.jpa.entity.ras.SubscriberAssessment;
import com.nano.jpa.entity.ras.SubscriberAssessment_;
import com.nano.jpa.enums.ActiveStatus;
import com.nano.jpa.enums.PayType;

/**
 * Manage {@link PersistenceContext} layer for application.
 * 
 * @author segz
 *
 */

@Stateless
public class QueryManager {
	
	protected Logger log = Logger.getLogger(getClass());

	protected CriteriaBuilder criteriaBuilder ;

	@PersistenceContext
	protected EntityManager entityManager ;

	@PostConstruct
	public void init(){
		criteriaBuilder = entityManager.getCriteriaBuilder();
	}
	
	/**
	 * Fetch earliest Time stamp from {@link SubscriberHistory} by {@link Subscriber} property.
	 *
	 * @param subscriber
	 * @return {@link Timestamp}
	 */
	public Timestamp getEarliestSubscriberHistoryTimeBySubscriber(Subscriber subscriber){
		
		CriteriaQuery<Timestamp> criteriaQuery = criteriaBuilder.createQuery(Timestamp.class);
		Root<SubscriberHistory> root = criteriaQuery.from(SubscriberHistory.class);
		
		criteriaQuery.select(criteriaBuilder.least(root.get(SubscriberHistory_.rechargeTime)));
		criteriaQuery.where(criteriaBuilder.equal(root.get(SubscriberHistory_.msisdn), subscriber.getMsisdn()));
		
		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriberHistory instance found for msisdn:" + subscriber.getMsisdn());
		}
		
		return null;
	}
	
	/**
	 * Fetch {@link SubscriberAssessment} by {@link Subscriber} property.
	 * 
	 * @param subscriber
	 * @return {@link Subscriber}
	 */
	public SubscriberAssessment getSubscriberAssessmentBySubscriber(Subscriber subscriber){
		
		CriteriaQuery<SubscriberAssessment> criteriaQuery = criteriaBuilder.createQuery(SubscriberAssessment.class);
		Root<SubscriberAssessment> root = criteriaQuery.from(SubscriberAssessment.class);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(SubscriberAssessment_.subscriber), subscriber));
		
		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriberAssessment instance was found with subscriber:" + subscriber.getPk());;
		}
		
		return null;
	}
	
	/**
	 * Fetch {@link BorrowableAmount} by amount property.
	 * 
	 * @param amount
	 * @return {@link BorrowableAmount}
	 */
	public BorrowableAmount getBorrowableAmountByAmount(Integer amount){
		
		CriteriaQuery<BorrowableAmount> criteriaQuery = criteriaBuilder.createQuery(BorrowableAmount.class);
		Root<BorrowableAmount> root = criteriaQuery.from(BorrowableAmount.class);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(BorrowableAmount_.amount), amount));
		
		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No borrowableAmount instance found with amount:" + amount);
		}
		
		return null;
	}
	
	/**
	 * Fetch ordered {@link BorrowableAmount} list.
	 * 
	 * @return
	 */
	public List<BorrowableAmount> getBorrowableAmountList(){
		
		CriteriaQuery<BorrowableAmount> criteriaQuery = criteriaBuilder.createQuery(BorrowableAmount.class);
		Root<BorrowableAmount> root = criteriaQuery.from(BorrowableAmount.class);
		
		criteriaQuery.select(root);
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(BorrowableAmount_.amount)));
		
		try {
			return entityManager.createQuery(criteriaQuery).getResultList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No borrowableAmount instance found");
		}

		return null;
	}
	
	/**
	 * Fetch ordered {@link BorrowableAmount} list.
	 * 
	 * @return
	 */
	public List<BorrowableAmount> getBorrowableAmountListDesc(){
		
		CriteriaQuery<BorrowableAmount> criteriaQuery = criteriaBuilder.createQuery(BorrowableAmount.class);
		Root<BorrowableAmount> root = criteriaQuery.from(BorrowableAmount.class);
		
		criteriaQuery.select(root);
		criteriaQuery.orderBy(criteriaBuilder.desc(root.get(BorrowableAmount_.amount)));
		
		try {
			return entityManager.createQuery(criteriaQuery).getResultList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No borrowableAmount instance found");
		}

		return null;
	}

	/**
	 * Fetch {@link Subscriber} by MSISDN property.
	 * 
	 * @param msisdn
	 * @return {@link Subscriber}
	 */
	public Subscriber getSubscriberByMsisdn(String msisdn){
		
		CriteriaQuery<Subscriber> criteriaQuery = criteriaBuilder.createQuery(Subscriber.class);
		Root<Subscriber> root = criteriaQuery.from(Subscriber.class);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(Subscriber_.msisdn), formatMisisdn(msisdn)));
		
		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriber instance was found with msisdn:" + msisdn);;
		}
		
		return null;
	}
	
	/**
	 * Fetch {@link SubscriberAssessment} by MSISDN property.
	 * 
	 * @param msisdn
	 * @return {@link Subscriber}
	 */
	public SubscriberAssessment getSubscriberAssessmentByMsisdn(String msisdn){
		
		CriteriaQuery<SubscriberAssessment> criteriaQuery = criteriaBuilder.createQuery(SubscriberAssessment.class);
		Root<SubscriberAssessment> root = criteriaQuery.from(SubscriberAssessment.class);
		
		Join<SubscriberAssessment, Subscriber> join = root.join(SubscriberAssessment_.subscriber);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(join.get(Subscriber_.msisdn), formatMisisdn(msisdn)));
		
		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriberAssessment instance was found with msisdn:" + msisdn);;
		}
		
		return null;
	}

	/**
	 * Fetch {@link SubscriberState} by MSISDN.
	 *
	 * @param msisdn
	 * @return {@link SubscriberState}
	 */
	public SubscriberState getSubscriberStateByMsisdn(String msisdn){
		
		CriteriaQuery<SubscriberState> criteriaQuery = criteriaBuilder.createQuery(SubscriberState.class);
		Root<SubscriberState> root = criteriaQuery.from(SubscriberState.class);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(SubscriberState_.msisdn), msisdn));
		
		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriberState instance was found with msisdn:" + msisdn);
		}
		
		return null;
	}

	/**
	 * Fetch {@link SubscriberHistory} by {@link Subscriber} property.
	 *
	 * @param subscriber
	 * @return list of {@link SubscriberHistory}
	 */
	public List<SubscriberHistory> getSubscriberHistoryBySubscriber(Subscriber subscriber){
		
		CriteriaQuery<SubscriberHistory> criteriaQuery = criteriaBuilder.createQuery(SubscriberHistory.class);
		Root<SubscriberHistory> root = criteriaQuery.from(SubscriberHistory.class);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.equal(root.get(SubscriberHistory_.msisdn), subscriber.getMsisdn()));
		
		try {
			return entityManager.createQuery(criteriaQuery).getResultList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriberHistory instance found for msisdn:" + subscriber.getMsisdn());
		}
		
		return null;
	}
	
	/**
	 * Fetch latest Time stamp from {@link SubscriberHistory} by {@link Subscriber} property.
	 *
	 * @param subscriber
	 * @return {@link Timestamp}
	 */
	public Timestamp getLatestSubscriberHistoryTimeBySubscriber(Subscriber subscriber){
		
		CriteriaQuery<Timestamp> criteriaQuery = criteriaBuilder.createQuery(Timestamp.class);
		Root<SubscriberHistory> root = criteriaQuery.from(SubscriberHistory.class);
		
		criteriaQuery.select(criteriaBuilder.greatest(root.get(SubscriberHistory_.rechargeTime)));
		criteriaQuery.where(criteriaBuilder.equal(root.get(SubscriberHistory_.msisdn), subscriber.getMsisdn()));
		
		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("No subscriberHistory instance found for msisdn:" + subscriber.getMsisdn());
		}
		
		return null;
	}
	
	/**
	 * Creates or fetches a unique {@link Subscriber} record.
	 *
	 * @param msisdn the MSISDN
	 * @return {@link Subscriber}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Subscriber createSubscriber(String msisdn){
		
		Subscriber subscriber = getSubscriberByMsisdn(formatMisisdn(msisdn));
		
		if (subscriber != null)
			return subscriber;

		subscriber = new Subscriber();
		subscriber.setInDebt(false);
		subscriber.setAutoRecharge(false);
		subscriber.setMsisdn(formatMisisdn(msisdn));

		return (Subscriber) create(subscriber);
	}
	
	/**
	 * Create a fresh SubscriberAssessment.
	 * 
	 * @param subscriber
	 * @param subscriberState
	 * @return {@link SubscriberAssessment}
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public SubscriberAssessment createNewAssessment(Subscriber subscriber, 
			SubscriberState subscriberState){
		
		SubscriberAssessment subscriberAssessment = new SubscriberAssessment();
		subscriberAssessment.setAgeOnNetwork(0);
		subscriberAssessment.setInDebt(subscriber.isInDebt());
		subscriberAssessment.setLastProcessed(Timestamp.valueOf(LocalDateTime.now()));
		subscriberAssessment.setNumberOfTopUps(0);
		subscriberAssessment.setSubscriber(subscriber);
		subscriberAssessment.setTopUpDuration(0);
		subscriberAssessment.setTopUpValueDuration(0);
		subscriberAssessment.setTotalTopUpValue(0);
		
		if(subscriberState != null)
			subscriberAssessment.setTariffPlan(subscriberState.getPayType());
		
		return (SubscriberAssessment) create(subscriberAssessment);
	}

	/**
	 * Create {@link SubscriberState}.
	 * 
	 * @param msisdn
	 * @param currentBalance
	 */
	public void createSubscriberHistory(String msisdn, 
			BigDecimal currentBalance) {
		// TODO Auto-generated method stub
		
		SubscriberState subscriberState = new SubscriberState();
		subscriberState.setActiveStatus(ActiveStatus.ACTIVE);
		subscriberState.setBlacklisted(false);
		subscriberState.setCurrentBalance(currentBalance);
		subscriberState.setLastUpdated(Timestamp.valueOf(LocalDateTime.now()));
		subscriberState.setMsisdn(formatMisisdn(msisdn));
		subscriberState.setPayType(PayType.PREPAID);
		
		create(subscriberState);
	}
	
	/**
	 * Format MSISDN.
	 *
	 * @param msisdn
	 * @return formatted MSISDN
	 */
	public String formatMisisdn(String msisdn){
		
		if (msisdn.startsWith("234"))
			msisdn = "0" + msisdn.substring(3, msisdn.length());
		
		if (msisdn.startsWith("+234"))
			msisdn = "0" + msisdn.substring(4, msisdn.length());
		
		if (!msisdn.startsWith("0"))
			msisdn = "0" + msisdn;
		
		return msisdn;
	}
	
	/**
	 * Persist entity and add entity instance to {@link EntityManager}.
	 * 
	 * @param entity
	 * @return persisted entity instance
	 */
	public <T> Object create(T entity){

		entityManager.persist(entity);

		try {
			return entity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
		return null;
	}

	/**
	 * Merge the state of the given entity into the current {@link PersistenceContext}.
	 * 
	 * @param entity
	 * @return the managed instance that the state was merged to
	 */
	public <T> Object update(T entity){

		entityManager.merge(entity);
		try {
			return entity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}

		return null;
	}
	
	/**
	 * Merge the state of the given entity into the current {@link PersistenceContext}.
	 * 
	 * @param entity
	 * @return the managed instance that the state was merged to
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <T> Object updateWithNewTransaction(T entity){

		entityManager.merge(entity);
		try {
			return entity;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}

		return null;
	}

	/**
	 * Remove the entity instance.
	 * 
	 * @param entity
	 */
	public <T> void delete(T entity){
		try {
			entityManager.remove(entity);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

}