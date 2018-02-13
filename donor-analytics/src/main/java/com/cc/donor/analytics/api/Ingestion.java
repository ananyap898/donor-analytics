package com.cc.donor.analytics.api;

import java.util.List;

import com.cc.donor.analytics.service.Donor;
import com.cc.donor.analytics.service.DonorVO;

public interface Ingestion {
	
	public List<Donor> extract();
	public List<DonorVO> transorm(List<Donor> donors);
	public void load(List<DonorVO> donorVOs);

}
