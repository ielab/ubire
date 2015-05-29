/**
 * 
 */
package au.edu.qut.ubire;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author zuccong
 *
 */
public class TauAP {


	static <K,V extends Comparable<? super V>> 
	List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

		Collections.sort(sortedEntries, 
				new Comparator<Entry<K,V>>() {
			@Override
			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		}
				);

		return sortedEntries;
	}

	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//load property file
		Properties prop = new Properties();
		//InputStream input = new FileInputStream("resources/TauAP.properties"); //"resources/test.config.properties"
		//prop.load(input);
		String lists = args[0];//"resources/test_system_ranking.txt";//prop.getProperty("lists");
		BufferedReader br = new BufferedReader(new FileReader(lists));
		String line;
		Map<String, Double> list1 = new HashMap<String, Double>();
		Map<String, Double> list2 = new HashMap<String, Double>();
		while ((line = br.readLine()) != null) {
		   String fields[] = line.split("\t");
		   String systemName = fields[0];
		   double list1Score = Double.parseDouble(fields[1]);
		   double list2Score = Double.parseDouble(fields[2]);
		   list1.put(systemName, list1Score);
		   list2.put(systemName, list2Score);
		}
		br.close();
		
		System.out.println("List 1:" + entriesSortedByValues(list1));
		System.out.println("List 2:" + entriesSortedByValues(list2));
		List<Entry<String, Double>> orderedList1 = entriesSortedByValues(list1);
		List<Entry<String, Double>> orderedList2 = entriesSortedByValues(list2);
		double tauAP = 2.0/(double)(orderedList1.size()-1);
		//System.out.println("initial tauAP: " + tauAP);
		double sumComponent = 0.0;
		for(int i=1; i<orderedList1.size(); i++){ //remember the list indexes go from i=0 to i=n-1
			Set<String> subsetList1 = new HashSet<String>();
			Set<String> subsetList2 = new HashSet<String>();
			for(int j=0; j<i; j++){
				subsetList1.add(orderedList1.get(j).getKey());
				subsetList2.add(orderedList2.get(j).getKey());
			}
			//System.out.println("subList 1:" + subsetList1.toString());
			//System.out.println("subList 2:" + subsetList2.toString());
			int c=0;
			for (String s : subsetList1) {
		        if(subsetList2.contains(s))
		        	c++;
		    }
			//System.out.println("c=" + c + "\ti=" + i);
			sumComponent = sumComponent + (double) c/(double)(i); //should be i-1, but remember i starts from 0, so it's already i-1
			//System.out.println("step " + i + " - sumComponent: " + sumComponent);
		}
		
		tauAP = (tauAP * sumComponent) -1.0;
		System.out.println("Tau_AP value is: " + tauAP);
	}

}
