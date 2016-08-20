package com.nano.ras.tools;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jboss.logging.Logger;

import com.nano.jpa.entity.ras.BorrowableAmount;
import com.nano.jpa.entity.ras.RasCriteria;
import com.nano.ras.jaxb.BorrowAmount;
import com.nano.ras.jaxb.BorrowList;


@Startup
@Singleton
@DependsOn("RasStartup")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class BaseLoader {
	
	private Logger log = Logger.getLogger(getClass());

	@Inject
	protected QueryManager lds;

	protected Unmarshaller um;
	protected File xmlFile;

	@PostConstruct
	protected void init(){
		log.info("Initializing Base Data Loader");

		try {
			xmlFile = new File(System.getProperty("jboss.server.log.dir") + "/ras.xml");
			JAXBContext ctx = JAXBContext.newInstance(BorrowList.class);
			um = ctx.createUnmarshaller();
			generate();
			log.debug("Initializing Base Data Loader Completed sucessfully");
		} catch (JAXBException e) {
			log.error("Exception ", e);
		}
	}

	@PreDestroy
	protected void cleanUp(){
		//
	}

	protected void generate(){
		try {
			BorrowList bl = (BorrowList) um.unmarshal(xmlFile);
			for(BorrowAmount ba: bl.getAmounts()){
				validateExistence(ba);
			}
		} catch (JAXBException e) {
			log.error("Exception ", e);
		}
	}

	/**
	 *
	 * @param amt BorrowAmount XML
	 * @return {@link BorrowableAmount} if created and null if it already exists
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	protected BorrowableAmount validateExistence(BorrowAmount amt){
		log.debug("Validating Borrowable Amount: " + amt.getAmount());
		
		if(lds.getBorrowableAmountByAmount(amt.getAmount()) == null){
			BorrowableAmount ba = new BorrowableAmount();
			ba.setAmount(amt.getAmount());
			ba.setServiceFee(amt.getServiceFee()); //expressed in percentage

			RasCriteria rasc = new RasCriteria();
			rasc.setMinAgeOnNetwork(amt.getMinAgeOnNetwork());
			rasc.setMinBalance(amt.getMinBalance());
			rasc.setMinTopUps(amt.getMinTopUps());
			rasc.setMinTopUpsDuration(amt.getMinTopUpsDuration());
			rasc.setMinTopUpValue(amt.getMinTopUpValue());
			ba.setCriteria(rasc);
			lds.create(ba);

			return ba;
		}

		return null;
	}

}