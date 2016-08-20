package com.nano.ras.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BorrowList {
	
	@XmlElement(name = "borrowAmount")
	private List<BorrowAmount> amounts;
	
	public void addAmount(BorrowAmount amt){
		if(amounts == null){
			amounts = new ArrayList<BorrowAmount>();
		}
		amounts.add(amt);
	}

	public List<BorrowAmount> getAmounts() {
		return amounts;
	}

}
