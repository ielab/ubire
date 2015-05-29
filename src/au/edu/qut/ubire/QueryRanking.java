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
public class QueryRanking {
	HashMap<String,Ranking> queryRanking = new HashMap<String,Ranking>();	// <queryid, Ranking> note that queryid can be a string

	public HashMap<String, Ranking> getQueryRanking() {
		return queryRanking;
	}

	public void setQueryRanking(HashMap<String, Ranking> queryRanking) {
		this.queryRanking = queryRanking;
	}
	
	
	public void addAQueryRanking(String queryid, String docid, Integer rank){
		if(!this.queryRanking.containsKey(queryid)){
			Ranking ranking = new Ranking();
			ranking.addToRanking(rank, docid);
			this.queryRanking.put(queryid, ranking);
		}
		else{
			Ranking ranking = queryRanking.get(queryid);
			//System.out.println(queryid);
			ranking.addToRanking(rank, docid);
			this.queryRanking.put(queryid, ranking);
		}
	}
	
	public Integer getRankOfDoc(String queryid, String docid){
		if(this.queryRanking.containsKey(queryid)){
			Ranking ranking = queryRanking.get(queryid);
			return ranking.getRankOfDoc(docid);
		}
		else{
			throw new IllegalArgumentException("The requested query (" + queryid+") is not in the current query ranking");
		}
	}
	
	public String getDocAtRank(String queryid, Integer rank){
		if(this.queryRanking.containsKey(queryid)){
			Ranking ranking = queryRanking.get(queryid);
			return ranking.getDocAtRank(rank);
		}
		else{
			throw new IllegalArgumentException("The requested query (" + queryid+") is not in the current query ranking");
		}
	}

	/**
	 * Standard TREC result format is assumed:
	 * 
	 * TOPIC      ITERATION      DOCUMENT#      RELEVANCY 	maybe_other_fields
	 * 
	 * Other fields will be ignored
	 * 
	 * TODO: extend the reading mode to non-standard TREC formats
	 * 
	 * */
	public void readRankingFile(String filepath, String separation) throws IOException{
		//System.out.println("Processing " + filepath);
		if(separation.equalsIgnoreCase(""))
			separation="\t";
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		int previousrank=-1;
		String previousqueryid="";
		int rankcounter=2;
		try {
			boolean addonetorankings=false;
			String line = br.readLine();
			while (line != null) {
				String[] fields = line.split(separation);
				//note that if there is a java.lang.ArrayIndexOutOfBoundsException exception raised here, this may because of the incorrect field separator in the qrels, e.g. tab instead of space
				//TODO: should make separators dynamic to support both tab and space
				String queryid = fields[0];
				String iteration = fields[1];
				String docid = fields[2];
				Integer rank = Integer.parseInt(fields[3]);
				if(rank==0 && queryid.equalsIgnoreCase(previousqueryid)){
					//the run does not comply to TREC formats as it does not increment rank positions.
					//Taking actions...
					rank = rankcounter;
					rankcounter++;
				}
				if(rankcounter>1 && !queryid.equalsIgnoreCase(previousqueryid))
					rankcounter=2;
				if(addonetorankings) {
					rank = rank +1;
					//System.out.println("added 1");
				}
				if(rank==0) {
					rank=1;
					addonetorankings=true;
					//System.out.println("enabling addition of 1");
				}
				//next, we deal with nan values by setting them to zero
				Double score = 0.0;
				if(!fields[4].equalsIgnoreCase("nan")) {
					score = Double.parseDouble(fields[4]);
				}
				String systemName = fields[5];
				this.addAQueryRanking(queryid, docid, rank);
				line = br.readLine();
				previousqueryid = queryid;
			}
		}
		finally {
			br.close();
		}
	}
	
	
}
