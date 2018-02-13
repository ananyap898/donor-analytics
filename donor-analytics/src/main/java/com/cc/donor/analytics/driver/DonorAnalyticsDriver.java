package com.cc.donor.analytics.driver;

import java.util.List;

import com.cc.donor.analytics.api.Ingestion;
import com.cc.donor.analytics.service.Donor;
import com.cc.donor.analytics.service.DonorVO;
import com.cc.donor.analytics.service.IngestionImpl;


/**
 * This is the main driver program.
 * @author ananyap
 *
 */
public class DonorAnalyticsDriver {

	public static void main(String[] args) {
		Ingestion donor = new IngestionImpl();
		
		System.out.println("Execution begins..");
		
		List<Donor> extractList = donor.extract();
		List<DonorVO> transformList = donor.transorm(extractList);
		donor.load(transformList);
		System.out.println("Execution completes successfully.");
		
	}

}
