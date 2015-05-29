/**
 * 
 */
package au.edu.qut.ubire;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author zuccong
 *
 */
public class Ranking {
	HashMap<Integer, String> ranking = new HashMap<Integer, String>();
	HashMap<String, Integer> reverseRanking = new HashMap<String, Integer>();
	
	public HashMap<Integer, String> getRanking() {
		return ranking;
	}
	public void setRanking(HashMap<Integer, String> ranking) {
		this.ranking = ranking;
	}
	public HashMap<String, Integer> getReverseRanking() {
		return reverseRanking;
	}
	public void setReverseRanking(HashMap<String, Integer> reverseRanking) {
		this.reverseRanking = reverseRanking;
	}
	
	public void addToRanking(Integer rank, String docid){
		if(!ranking.containsKey(rank)){
			ranking.put(rank,docid);
			if(!reverseRanking.containsKey(docid)){
				reverseRanking.put(docid, rank);
			}else{
				throw new IllegalArgumentException("the reverseRanking already contains document " + docid);
			}
		}else{
			throw new IllegalArgumentException("The ranking already contains a document (" + ranking.get(rank)+
					") at rank " + rank + " thus document " + docid + " cannot be added to the ranking");
		}
	}
	
	public String getDocAtRank(Integer rank){
		if(ranking.containsKey(rank))
			return ranking.get(rank);
		else
			throw new IllegalArgumentException("No document at rank " + rank);
	}
	
	public Integer getRankOfDoc(String docid){
		if(reverseRanking.containsKey(docid))
			return reverseRanking.get(docid);
		else
			throw new IllegalArgumentException("Document " + docid + " is not present in the ranking");
	}
	
	
}
