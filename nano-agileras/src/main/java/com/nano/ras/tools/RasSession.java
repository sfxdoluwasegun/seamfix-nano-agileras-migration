package com.nano.ras.tools;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import org.jboss.logging.Logger;

import com.nano.jpa.entity.ras.BorrowableAmount;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@AccessTimeout(unit = TimeUnit.SECONDS, value = 90)
public class RasSession {
	
	private Logger log = Logger.getLogger(getClass());

	private List<BorrowableAmount> borrowableList;
	
	private List<BorrowableAmount> descBorrowableList;

	@Inject
	private QueryManager vds;

	@PostConstruct
	public void init(){
		log.debug("Initializing RAS App Session");
		borrowableList = vds.getBorrowableAmountList();
		descBorrowableList = vds.getBorrowableAmountListDesc();
	}

	public void addBorrowableAmount(BorrowableAmount amt){
		borrowableList.add(amt);
	}

	public List<BorrowableAmount> getBorrowableList(){
		return this.borrowableList;
	}
	
	public void addBorrowableAmountToDescList(BorrowableAmount amt){
		descBorrowableList.add(amt);
	}

	public List<BorrowableAmount> getBorrowableListDesc(){
		return this.descBorrowableList;
	}

}
