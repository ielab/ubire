/**
 * 
 */
package au.edu.qut.ubire;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author zuccong
 *
 *	This class models additional characteristics that are associated to documents (for a query, if necessary)
 *	Characteristics may be e.g. readability scores
 *
 *
 */
public class DocCharacteristics {
	
	HashMap<String,ArrayList< Entry<String,Double> > > docCharacteristics = new HashMap<String,ArrayList<Entry<String,Double>>>(); // <docid, Array<typeOfMeasurement, score>

	public HashMap<String, ArrayList<Entry<String, Double>>> getDocCharacteristics() {
		return docCharacteristics;
	}

	public void setDocCharacteristics(
			HashMap<String, ArrayList<Entry<String, Double>>> docCharacteristics) {
		this.docCharacteristics = docCharacteristics;
	}
	
	public void addDocCharacteristics(String docid, String measure, Double score){
		if(docCharacteristics.containsKey(docid)){
			ArrayList<Entry<String, Double>> docArray = docCharacteristics.get(docid);
			if(docArray.contains(measure)){
				throw new IllegalArgumentException("The docArray already contains a value for measure " + measure);
			}else{
				Entry<String, Double> e = new AbstractMap.SimpleEntry<String, Double>(measure, score);
				docArray.add(e);
			}
		}else{
			Entry<String, Double> e = new AbstractMap.SimpleEntry<String, Double>(measure, score);
			ArrayList<Entry<String, Double>> docArray = new ArrayList<Entry<String, Double>>();
			docArray.add(e);
			docCharacteristics.put(docid, docArray);
		}
			
	}

	public Double getDocCharacteristicsScore(String docid, String measure){
		if(docCharacteristics.containsKey(docid)){
			ArrayList<Entry<String, Double>> docArray = docCharacteristics.get(docid); // Entry<String, Double> e = new AbstractMap.SimpleEntry<String, Double>(measure, score);
			boolean found=false;
			for(int i = 0; i< docArray.size();i++){
				Entry<String, Double> e = docArray.get(i);
				if(e.getKey().equalsIgnoreCase(measure)){
					found=true;
					return e.getValue();
				}
			}
			if(!found)
				throw new IllegalArgumentException("The docArray does not contain a value for measure " + measure);
		}else
			throw new IllegalArgumentException("The docCharacteristics does not contain a value for document " + docid);
		return null;
	}
	
	/**
	 * Standard DocCharacteristics format is assumed:
	 * 
	 * DOCID      MEASURE      SCORE       
	 * 
	 * 
	 * */
	public void readDocCharacteristicsFile(String filepath, String separation) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		try {
			String line = br.readLine();
			while (line != null) {
				line = br.readLine();
				String[] fields = line.split(separation);
				String docid = fields[0];
				String measure = fields[1];
				Double score = Double.parseDouble(fields[2]);
				this.addDocCharacteristics(docid, measure, score);
			}
		} finally {
			br.close();
		}
	}
	
	
	
	/**
	 * Standard DocCharacteristics format is assumed:
	 * 
	 * 1	0	aldf.1864_12_000027	3      
	 * 
	 * 
	 * */
	public void readDocCharacteristicsFileQrelsFormat(String filepath, String separation) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		try {
			String line = br.readLine();
			while (line != null) {
				line = br.readLine();
				String[] fields = line.split(separation);
				String query = fields[0];
				String docid = fields[2];
				String measure = "readability";
				Double score = Double.parseDouble(fields[3]);
				this.addDocCharacteristics(docid, measure, score);
			}
		} finally {
			br.close();
		}
	}
}
