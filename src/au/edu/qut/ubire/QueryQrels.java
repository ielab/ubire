/**
 * 
 */
package au.edu.qut.ubire;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author zuccong
 * 
 * This class models the qrels for a set of queries
 *
 */
public class QueryQrels {
	HashMap<String,Qrels> queryQrels = new HashMap<String,Qrels>();	// <queryid, Qrels> note that queryid can be a string
	DocCharacteristics docCharacteristics = new DocCharacteristics();
	
	/**
	 * @return the docCharacteristics
	 */
	public DocCharacteristics getDocCharacteristics() {
		return this.docCharacteristics;
	}

	/**
	 * @param docCharacteristics the docCharacteristics to set
	 */
	public void setDocCharacteristics(DocCharacteristics docCharacteristics) {
		this.docCharacteristics = docCharacteristics;
	}

	public HashMap<String, Qrels> getQueryQrels() {
		return queryQrels;
	}

	public void setQueryQrels(HashMap<String, Qrels> queryQrels) {
		this.queryQrels = queryQrels;
	}
	
	
	public void addAQueryQrel(String queryid, String docid, Integer relevance){
		if(!this.queryQrels.containsKey(queryid)){
			Qrels qrels = new Qrels();
			qrels.addAQrel(docid, relevance);
			this.queryQrels.put(queryid, qrels);
		}
		else{
			Qrels qrels = queryQrels.get(queryid);
			qrels.addAQrel(docid, relevance);
			this.queryQrels.put(queryid, qrels);
		}
	}
	
	public Integer getAQueryQrel(String queryid, String docid){
		if(this.queryQrels.containsKey(queryid)){
			Qrels qrels = queryQrels.get(queryid);
			return qrels.getAQrel(docid);
		}
		else{
			throw new IllegalArgumentException("The requested query (" + queryid+") is not in the current query qrels");
		}
	}
	
	public ArrayList<String> getQueryQrelsOfValue(String queryid, Integer relevance){
		ArrayList<String> list = new ArrayList();
		if(!this.queryQrels.containsKey(queryid))
			throw new IllegalArgumentException("The requested query (" + queryid+") is not in the current query qrels");
		else
			return this.queryQrels.get(queryid).getQrelsOfValue(relevance);
	}

	
	/**
	 * Standard TREC format is assumed:
	 * 
	 * TOPIC      ITERATION      DOCUMENT#      RELEVANCY 	maybe_other_fields
	 * 
	 * Other fields will be ignored
	 * 
	 * TODO: extend the reading mode to non-standard TREC formats
	 * 
	 * */
	public void readQrelsFile(String filepath, String separation) throws IOException{
		if(separation.equalsIgnoreCase(""))
			separation="\t";
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		try {
			String line = br.readLine();
			while (line != null) {
				String[] fields = line.split(separation);
				String queryid = fields[0];
				String iteration = fields[1];
				String docid = fields[2];
				Integer relevance = Integer.parseInt(fields[3]);
				this.addAQueryQrel(queryid, docid, relevance);
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}
	
	/**
	 * Standard TREC format is assumed:
	 * 
	 * TOPIC      ITERATION      DOCUMENT#      RELEVANCY 	maybe_other_fields
	 * 
	 * Other fields will be ignored
	 * 
	 * TODO: extend the reading mode to non-standard TREC formats
	 * 
	 * */
	public void readQrelsFile(String filepath, String separation, boolean docCharacteristicsEnabled) throws IOException{
		if(separation.equalsIgnoreCase(""))
			separation="\t";
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		DocCharacteristics dc = new DocCharacteristics();
		try {
			String line = br.readLine();
			while (line != null) {
				String[] fields = line.split(separation);
				String queryid = fields[0];
				String iteration = fields[1];
				String docid = fields[2];
				Integer relevance = Integer.parseInt(fields[3]);
				if(docCharacteristicsEnabled){
					ArrayList<Entry<String,Double>> docCharArray = new ArrayList<Entry<String,Double>>();
					for(int n = 4; n<fields.length;n++){
						double docCharScore = Double.parseDouble(fields[n]);
						dc.addDocCharacteristics(docid, "measure", docCharScore);
					}
				}
					
				
				this.addAQueryQrel(queryid, docid, relevance);
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		if(docCharacteristicsEnabled)
			this.docCharacteristics=dc;
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
		DocCharacteristics dc = new DocCharacteristics();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split(separation);
				String query = fields[0];
				String docid = fields[2];
				String measure = "readability";
				Double score = Double.parseDouble(fields[3]);
				dc.addDocCharacteristics(docid, measure, score);
			}
		} finally {
			br.close();
		}
		this.docCharacteristics=dc;
	}

}
