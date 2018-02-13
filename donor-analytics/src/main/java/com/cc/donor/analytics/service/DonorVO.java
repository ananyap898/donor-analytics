package com.cc.donor.analytics.service;



public class DonorVO {
	
	private String recipientId;
	
	private String zipcode;
	private String transactionYear;
	private long percentile;
	private Double transactionAmount;
	private int count;
	
	public DonorVO(String recipientId, String zipcode, String transactionYear, long percentile,
			Double transactionAmount, int count) {
		super();
		this.recipientId = recipientId;
		this.zipcode = zipcode;
		this.transactionYear = transactionYear;
		this.percentile = percentile;
		this.transactionAmount = transactionAmount;
		this.count = count;
	}

	public String getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getTransactionYear() {
		return transactionYear;
	}

	public void setTransactionYear(String transactionYear) {
		this.transactionYear = transactionYear;
	}

	public long getPercentile() {
		return percentile;
	}

	public void setPercentile(long percentile) {
		this.percentile = percentile;
	}

	public Double getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(Double transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "Composite [recipientId=" + recipientId + ", zipcode=" + zipcode + ", transactionYear=" + transactionYear
				+ ", percentile=" + percentile + ", transactionAmount=" + transactionAmount + ", count=" + count + "]";
	}
	 
	
	
}
