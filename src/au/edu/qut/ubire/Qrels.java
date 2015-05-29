/**
 * 
 */
package au.edu.qut.ubire;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author zuccong
 *
 * This class models the qrels for a query
 *
 *
 */
public class Qrels {
	
	HashMap<String, Integer> qrels = new HashMap<String, Integer>(); // <docid, relevance label>

	public HashMap<String, Integer> getQrels() {
		return qrels;
	}

	public void setQrels(HashMap<String, Integer> qrels) {
		this.qrels = qrels;
	}
	
	public void addAQrel(String docid, Integer relevance){
		if(!this.qrels.containsKey(docid))
			this.qrels.put(docid, relevance);
		else{
			throw new IllegalArgumentException("This docid has already a relevance label associated\nThe old relevance label for doc " + docid 
					+ " is " + relevance + "\nThe conflicting relevance label for this doc is " + relevance 
					+ "The relevance label for this document has not been updated");
		}
	}
	
	public Integer getAQrel(String docid){
		if(this.qrels.containsKey(docid))
			return this.qrels.get(docid);
		else{
			throw new IllegalArgumentException("The requested docid (" + docid+") is not in the current qrels");
		}
	}
	
	public ArrayList<String> getQrelsOfValue(Integer relevance){
		ArrayList<String> list = new ArrayList();
		for (Entry<String, Integer> entry : this.qrels.entrySet()) {
		    String docid = entry.getKey();
		    Integer currentRelevance = entry.getValue();
		    if(currentRelevance==relevance){
		    	list.add(docid);
		    }
		}
		return list;
	}
	
	

	
}
