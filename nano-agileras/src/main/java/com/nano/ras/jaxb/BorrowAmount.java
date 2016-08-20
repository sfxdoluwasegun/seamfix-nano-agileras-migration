package com.nano.ras.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BorrowAmount {
	
	private Integer amount;
	private Integer serviceFee;
	
	private int minAgeOnNetwork = 360;
	private int minTopUps = 3;
	private int minTopUpsDuration = 30;
	private int minTopUpValueDuration = 30;
	private int minBalance = 0;
	
	private Integer minTopUpValue = 3000;

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Integer getServiceFee() {
		return serviceFee;
	}

	public void setServiceFee(Integer serviceFee) {
		this.serviceFee = serviceFee;
	}

	public int getMinAgeOnNetwork() {
		return minAgeOnNetwork;
	}

	public void setMinAgeOnNetwork(int minAgeOnNetwork) {
		this.minAgeOnNetwork = minAgeOnNetwork;
	}

	public int getMinTopUps() {
		return minTopUps;
	}

	public void setMinTopUps(int minTopUps) {
		this.minTopUps = minTopUps;
	}

	public int getMinTopUpsDuration() {
		return minTopUpsDuration;
	}

	public void setMinTopUpsDuration(int minTopUpsDuration) {
		this.minTopUpsDuration = minTopUpsDuration;
	}

	public Integer getMinTopUpValue() {
		return minTopUpValue;
	}

	public void setMinTopUpValue(Integer minTopUpValue) {
		this.minTopUpValue = minTopUpValue;
	}

	public int getMinTopUpValueDuration() {
		return minTopUpValueDuration;
	}

	public void setMinTopUpValueDuration(int minTopUpValueDuration) {
		this.minTopUpValueDuration = minTopUpValueDuration;
	}

	public int getMinBalance() {
		return minBalance;
	}

	public void setMinBalance(int minBalance) {
		this.minBalance = minBalance;
	}

}