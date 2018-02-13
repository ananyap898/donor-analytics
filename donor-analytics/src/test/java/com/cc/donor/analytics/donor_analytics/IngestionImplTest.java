package com.cc.donor.analytics.donor_analytics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cc.donor.analytics.service.Donor;
import com.cc.donor.analytics.service.DonorVO;
import com.cc.donor.analytics.service.IngestionImpl;

public class IngestionImplTest {

	IngestionImpl impl = null;

	@Before
	public void setUp() throws Exception {
		impl = new IngestionImpl();
	}

	@Test
	public void testExtract() {
		List<Donor> donorList = impl.extract();
		assertTrue(donorList.size() == 8);
	}

	@Test
	public void testTransorm() {
		
		List<Donor> donors = new ArrayList<>();
		donors.add(new Donor("C00384517","Ananya","02895","01012018",100.0,"C0038451702895"));
		donors.add(new Donor("C00384517","Punya","02895","01012018",200.0,"C0038451702895"));
		List<DonorVO> donorVos = impl.transorm(donors);
		assertTrue(donorVos.size() == 2);
	}

	@Test
	public void testisNull(){
		String donorId = "";
		assertFalse(impl.isNull(donorId));
	}
	
	@Test
	public void testisNotNull(){
		String donorId = "Ananya";
		assertTrue(impl.isNull(donorId));
	}

	@Test
	public void testYearIsInvalid() {
		assertTrue(impl.isValidDate("03012018", "ddMMyyyy"));
	}
	
	@Test
	public void calculatePercentileTest(){
		List<Double> list = new ArrayList<>();
		list.add(333.0);
		list.add(374.0);
		double actual = impl.calculatePercentile(list, 0.3);
		assertTrue(actual == 333.0);
	}

	@Test
	public void readPercentileTest(){
		double percentile = impl.readPercentile();
		assertTrue(percentile == 30);
	}
	
}
