package com.cc.donor.analytics.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cc.donor.analytics.api.Ingestion;

/**
 * 
 * @author ananyap
 *
 */
public class IngestionImpl implements Ingestion {

	private static final String format = "ddmmyyyy";
	private static Set<String> hashSet = new HashSet<String>();
	private static final int EIGHT = 8;
	private static Map<String, Integer> map = new HashMap<String, Integer>();

	/**
	 * This method extracts the data from pipe delimited input file. It takes
	 * valid records into consideration.Filtering logics have been place such as
	 * : mandatory checks, zipcode length, and non-empty OTHER_ID
	 */
	public List<Donor> extract() {
		List<Donor> donorList = new ArrayList<Donor>();
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader("input/itcont.txt"));
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\\|");

				String zipCode = fields[10];
				String otherId = fields[15];
				String transactionDate = fields[13];
				Donor donor = null;

				// Mandatory checks , non-empty OTHER_ID, and length of zipcode
				if (checkMandatoryAttributes(fields) && !isNull(otherId) && !(zipCode.length() < 5)
						&& isValidDate(transactionDate, format)) {
					String uniqueOrderId = fields[0].concat(fields[10].substring(0, 5));
					double amount = Double.valueOf(fields[14]);
					donor = new Donor(fields[0], fields[7], zipCode, fields[13], amount, uniqueOrderId);
					donorList.add(donor);

				}
			}
			br.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return donorList;
	}

	/**
	 * This method checks validity of the date
	 * 
	 * @param transactionDate
	 * @param dateFormat
	 * @return
	 */
	public boolean isValidDate(String transactionDate, String dateFormat) {
		if (transactionDate.length() != EIGHT) {
			return false;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		sdf.setLenient(false);
		try {
			// if not valid, it will throw ParseException
			sdf.parse(transactionDate);

		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * This method checks mandatory fields
	 * 
	 * @param fields
	 * @return boolean
	 */
	private boolean checkMandatoryAttributes(String[] fields) {
		if (isNull(fields[0]) || isNull(fields[7]) || isNull(fields[10]) || isNull(fields[13]) || isNull(fields[14])
				|| isNull(fields[14])) {
			return true;
		}
		return false;
	}

	/**
	 * This method checks for null
	 * 
	 * @param value
	 * @return boolean
	 */
	public boolean isNull(String value) {
		if (value != null && !value.isEmpty()) {
			return true;
		}
		return false;
	}

	public List<DonorVO> transorm(List<Donor> donors) {
		List<DonorVO> donorVOList = new ArrayList<>();

		double percentileValue = readPercentile();
		Map<String, List<Donor>> donorMap = populateDonorMap(donors);

		Map<String, List<Donor>> txnDateMap = new HashMap<String, List<Donor>>();

		// further group by txnDate; k:YYYY, v: List<Donor>
		for (Entry<String, List<Donor>> entry : donorMap.entrySet()) {
			List<Donor> valueList = entry.getValue();
			for (Donor d : valueList) {
				if (!txnDateMap.containsKey(d.getTransactionDate().substring(4))) {
					txnDateMap.put(d.getTransactionDate().substring(4), new ArrayList<Donor>());
				}
				txnDateMap.get(d.getTransactionDate().substring(4)).add(d);
			}
		}
		donorMap.clear();

		// Further Transformations: Calculate count, running percentile, and
		// populate output object list.
		List<Double> percentileList = new ArrayList<>();
		for (Entry<String, List<Donor>> entry : txnDateMap.entrySet()) {
			List<Donor> valueList = entry.getValue();
			if (valueList.size() > 1) {
				double sum = 0.0;
				int count = 0;
				double percentile = 0.0;
				percentileList.clear();
				for (Donor d : valueList) {
					percentileList.add(d.getTransactionAmount());
					sum += d.getTransactionAmount();
					count += 1;
					percentile = calculatePercentile(percentileList, percentileValue);
					donorVOList.add(new DonorVO(d.getRecipientId(), d.getZipcode().substring(0, 5),
							d.getTransactionDate().substring(4), Math.round(percentile), sum, count));
				}
			}
		}
		txnDateMap.clear();
		return donorVOList;
	}

	/**
	 * This mthod populates Donor Map containing k as uniqueId(id+zip) and v as List<Donor>
	 * @param donors
	 * @return Map<String, List<Donor>>
	 */
	private Map<String, List<Donor>> populateDonorMap(List<Donor> donors) {
		Map<String, List<Donor>> donorMap = new HashMap<String, List<Donor>>();

		// populate map with the count, k:id+zip, v:count
		for (Donor d : donors) {
			String uniqueId = d.getUniqueId();
			Integer count = map.get(uniqueId);
			map.put(uniqueId, (count == null) ? 1 : count + 1);
		}

		// hashset contains only those donors whose count greater than 1
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (entry.getValue() > 1) {
				hashSet.add(entry.getKey());
			}
		}
		// group by uniqueId available in the hashset; k:id+zip, v: List<Donor>
		for (Donor d : donors) {
			if (hashSet.contains(d.getUniqueId())) {
				if (!donorMap.containsKey(d.getUniqueId())) {
					donorMap.put(d.getUniqueId(), new ArrayList<Donor>());
				}
				donorMap.get(d.getUniqueId()).add(d);
			}
		}
		return donorMap;
	}

	/**
	 * This method calculates the running percentile
	 * 
	 * @param percentileList
	 *            list containing transaction amounts
	 * @param percentile
	 *            value of the percentile
	 * @return
	 */
	public double calculatePercentile(List<Double> percentileList, double percentile) {

		Arrays.sort(percentileList.toArray());

		int index = (int) ((percentile / 100) * percentileList.size());
		return percentileList.get(index);

	}

	/**
	 * This method reads the percentile.txt and extracts the percentile value
	 * 
	 * @return double
	 */
	public double readPercentile() {
		double value = 0.0;
		try (BufferedReader br = new BufferedReader(new FileReader("input/percentile.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				value = Double.valueOf(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * This method loads the output data into an output file.
	 */
	public void load(List<DonorVO> donorVOs) {
		 File outputFile = new File("output/repeat_donors.txt");
		    if (outputFile.exists()) {
		    	outputFile.delete();     
		    }
		
		try (FileWriter fw = new FileWriter("output/repeat_donors.txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			for (DonorVO vo : donorVOs) {
				out.println(vo.getRecipientId() + "|" + vo.getZipcode().substring(0, 5) + "|" + vo.getTransactionYear()
						+ "|" + vo.getPercentile() + "|" + vo.getTransactionAmount() + "|" + vo.getCount());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
