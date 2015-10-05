/**
 * 
 */
package au.edu.qut.ubire;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author zuccong
 *
 */
public class IRmeasures {
	
	Qrels qrels;
	DocCharacteristics docCharacteristics;
	Ranking ranking;
	String discount;
	String docCharacteristicsMeasure;
	String docCharacteristicsKernel;
	HashMap<Double,Double> docCharacteristicsGradeMapping = new HashMap<Double,Double>();
	

	
	/**
	 * @return the docCharacteristicsGradeMapping
	 */
	public HashMap<Double, Double> getDocCharacteristicsGradeMapping() {
		return docCharacteristicsGradeMapping;
	}

	/**
	 * @param docCharacteristicsGradeMapping the docCharacteristicsGradeMapping to set
	 */
	public void setDocCharacteristicsGradeMapping(
			HashMap<Double, Double> docCharacteristicsGradeMapping) {
		this.docCharacteristicsGradeMapping = docCharacteristicsGradeMapping;
	}

	public IRmeasures(Qrels qrels, DocCharacteristics docCharacteristics, Ranking ranking, 
			String discount, String docCharacteristicsMeasure, String docCharacteristicsKernel){
		this.qrels=qrels;
		this.docCharacteristics=docCharacteristics;
		this.ranking=ranking;
		this.discount=discount;
		this.docCharacteristicsMeasure=docCharacteristicsMeasure;
		this.docCharacteristicsKernel=docCharacteristicsKernel;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> 
	sortByValue( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list =
				new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
				{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o2.getValue()).compareTo( o1.getValue() ); // this does reverse the order of the comparison; as opposed to o1.getValue() compareTo o2.getValue
			}
				} );

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}


	/*
	 * We assume the rank position starts from 0
	 * TODO: check this assumption
	 * */
	public double getDiscount(Integer rank){
		if(this.discount.equals("log"))
			return logDiscount(rank);
		if(this.discount.equals("inverse"))
			return reverseDiscount(rank);
		//default
		return logDiscount(rank);
	}
	
	public double log2(double x){
		return Math.log(x)/Math.log(2);
	}
	
	public double logBase(double x, double base){
		return Math.log(x)/Math.log(base);
	}
	
	public double logDiscount(Integer rank){
		return 1/log2((double)rank+1.0);
	}
	
	public double reverseDiscount(Integer rank){
		return (double)1.0/(double)(rank+1);
	}
	
	public Double ndcg(boolean considerDocCharacteristics){
		if(considerDocCharacteristics)
			return ndcgDocCharacteristics();
		else
			return ndcgJarvelin();
		
	}
	
	public Double ndcgDocCharacteristics(){
		Double ndcg=0.0;
		
		for(Entry<Integer,String> e : this.ranking.ranking.entrySet()){
			Integer rank = e.getKey();
			String docid = e.getValue();
			double characteristicsScore=0.0;
			try{
				characteristicsScore = (double)docCharacteristics.getDocCharacteristicsScore(docid, this.docCharacteristicsMeasure);
			}
			catch(Exception IllegalArgumentException){
				characteristicsScore=0; //this is to accomodate for the fact that at CLEF they are idiots and they like to update the collection without say anything
			}
			double discount=0.0;
			if(rank==1)
				discount = 1;
			else
				discount = getDiscount(rank-1);
			//System.out.println("rank:"+ rank+"\tdiscount:"+discount);
			int relevance; 
			try {
				relevance = qrels.getAQrel(docid); //this return the integer relevance label
		    }
		    catch (Exception IllegalArgumentException) {
		        relevance = 0;
		    }
			double relevanceScore = relevanceToScore(relevance);
			ndcg = ndcg + discount * relevanceScore * applyUserModel(this.docCharacteristicsKernel, characteristicsScore);
			//System.out.println(discount * relevanceScore);
		}
		//so far we got the cumulative discounted gain
		//now normalisation stage
		double idealndcg = computeIdealNdcgJarvelin();
		
		//System.out.println("ndcg=" + ndcg + "\tIdeal ndcg=" + idealndcg); ;
		if(idealndcg>0)
			return ndcg/idealndcg;
		else
			return 0.0;
	}
	
	
	
	public Double ndcgBurges(){
		Double ndcg=0.0;
		
		for(Entry<Integer,String> e : this.ranking.ranking.entrySet()){
			Integer rank = e.getKey();
			String docid = e.getValue();
			double discount = getDiscount(rank);
			System.out.println("discount:"+discount);
			int relevance; 
			try {
				relevance = qrels.getAQrel(docid); //this return the integer relevance label
		    }
		    catch (Exception IllegalArgumentException) {
		        relevance = 0;
		    }
			double relevanceScore = relevanceToScore(relevance);
			ndcg = ndcg + discount * (Math.pow(2, relevanceScore) -1);
			System.out.println(discount * (Math.pow(2, relevanceScore) -1));
		}
		//so far we got the cumulative discounted gain
		//now normalisation stage
		double idealndcg = computeIdealNdcg();
		//System.out.println("ndcg=" + ndcg + "\tIdeal ndcg=" + idealndcg); ;
		ndcg = ndcg/idealndcg;
		return ndcg;
	}
	
	public Double ndcgJarvelin(){
		Double ndcg=0.0;
		
		for(Entry<Integer,String> e : this.ranking.ranking.entrySet()){
			Integer rank = e.getKey();
			String docid = e.getValue();
			double discount=0.0;
			if(rank==1)
				discount = 1;
			else
				discount = getDiscount(rank-1);
			//System.out.println("rank:"+ rank+"\tdiscount:"+discount);
			int relevance; 
			try {
				relevance = qrels.getAQrel(docid); //this return the integer relevance label
		    }
		    catch (Exception IllegalArgumentException) {
		        relevance = 0;
		    }
			double relevanceScore = relevanceToScore(relevance);
			ndcg = ndcg + discount * relevanceScore;
			//System.out.println(discount * relevanceScore);
		}
		//so far we got the cumulative discounted gain
		//now normalisation stage
		double idealndcg = computeIdealNdcgJarvelin();
		
		//System.out.println("ndcg=" + ndcg + "\tIdeal ndcg=" + idealndcg); ;
		if(idealndcg>0)
			return ndcg/idealndcg;
		else
			return 0.0;
	}


	private double computeIdealNdcg() {
		Map<String, Integer> sortedQrels = sortByValue(qrels.qrels);
		//compute dcg for this
		double idealndgc=dcgStandard(sortedQrels);
		return idealndgc;
	}
	
	public Double dcgStandard(Map<String, Integer> qrels){
		Double dcg=0.0;
		int rank=0;
		for(Entry<String, Integer> e : qrels.entrySet()){
			Integer relevance = e.getValue(); //this return the integer relevance label
			//System.out.println(relevance);
			double discount = getDiscount(rank);
			double relevanceScore = relevanceToScore(relevance); 
			dcg = dcg + discount * (Math.pow(2, relevanceScore) -1);
			rank++;
		}
		return dcg;
	}
	
	private double computeIdealNdcgJarvelin() {
		Map<String, Integer> sortedQrels = sortByValue(qrels.qrels);
		//compute dcg for this
		double idealndgc=dcgStandardJarvelin(sortedQrels);
		return idealndgc;
	}
	
	public Double dcgStandardJarvelin(Map<String, Integer> qrels){
		Double dcg=0.0;
		int rank=1;
		for(Entry<String, Integer> e : qrels.entrySet()){
			Integer relevance = e.getValue(); //this return the integer relevance label
			//System.out.println(relevance);
			double discount=0.0;
			if(rank==1)
				discount = 1;
			else
				discount = getDiscount(rank-1);
			double relevanceScore = relevanceToScore(relevance); 
			dcg = dcg +  discount * relevanceScore;
			rank++;
		}
		return dcg;
	}
	

	private double relevanceToScore(Integer relevance) {
		double score=0.0;
		score = (double)relevance;
		/*
		switch (relevance) {
		case 1:  score = 0.33;
		break;
		case 2:  score = 0.50;
		break;
		case 3:  score = 0.66;
		break;
		case 4:  score = 0.88;
		break;
		case 5:  score = 1.0;
		break;
		default: score=0;
		break;
		}
		*/
		return score;
	}

	public double applyUserModel(String model, double characteristicsScore){
		double score=0.0;
		switch (model) {
		case "model1":  score = constantUserModel(characteristicsScore, 20.0);
		break;
		case "model2":  score = arctanUserModel(characteristicsScore, 20.0);
		break;
		case "model3":  score = gainUserModel(characteristicsScore, 0);
		break;
		case "model4":  score = gradedConstantUserModel(characteristicsScore, this.docCharacteristicsGradeMapping);
		break;
		case "model5":  score = binaryUserModel(characteristicsScore, 2.0);
		break;
		default: score=0;
		break;
		}
		return score;
	}

	public double constantUserModel(double characteristicsScore, double thresholdScore){
		if(characteristicsScore>=thresholdScore)
			return 0.0;
		else
			return 1.0;
	}
	
	public double binaryUserModel(double characteristicsScore, double thresholdScore){
		//System.out.println(characteristicsScore);
		if(characteristicsScore>=thresholdScore)
			return 1.0;
		else
			return 0.0;
	}
	
	public double gradedConstantUserModel(double characteristicsScore, HashMap<Double,Double>gradeMapping){
		return gradeMapping.get(characteristicsScore);
	}
	
	public double arctanUserModel(double characteristicsScore, double thresholdScore){
		double pread = 0.5 - Math.atan(characteristicsScore-thresholdScore)/(Math.PI);
		//pread = pread/25.853; //TODO: division by integral value from qrel; now use the know integral
		return pread;
	}
	
	public double gainUserModel(double characteristicsScore, double thresholdScore){
		double pread = Math.pow(2, characteristicsScore)-1;
		return pread;
	}
	
	
	public double rbp(double p, int stoprank){
		double rbp=0.0;
		for(Entry<Integer,String> e : this.ranking.ranking.entrySet()){
			Integer rank = e.getKey();
			if(rank > stoprank)
				break;
			String docid = e.getValue();
			double discount=Math.pow(p,rank-1);
			int relevance; 
			try {
				relevance = qrels.getAQrel(docid); //this return the integer relevance label
				if(relevance>0)
					relevance=1; //this is because of binary relevance
		    }
		    catch (Exception IllegalArgumentException) {
		        relevance = 0;
		    }
			double relevanceScore = relevanceToScore(relevance);
			rbp=rbp + relevance * discount;
			//System.out.println(discount * relevanceScore);
		}
		//normalise
		rbp=(1-p) * rbp;
		if(rbp>1.0) {
			for(Entry<Integer,String> e : this.ranking.ranking.entrySet()){
				Integer rank = e.getKey();
				double discount=Math.pow(p,rank-1);
				System.err.println(rank + "\t" + discount);
			}
			
		}
		
		return rbp;
	}
	
	public double rbpDocCharacteristics(double p, int stoprank){
		double rbp=0.0;
		for(Entry<Integer,String> e : this.ranking.ranking.entrySet()){
			Integer rank = e.getKey();
			if(rank > stoprank)
				break;
			String docid = e.getValue();
			double characteristicsScore=0.0;
			try{
				characteristicsScore = (double)docCharacteristics.getDocCharacteristicsScore(docid, this.docCharacteristicsMeasure);
				//System.out.println(docid + "\tcharacteristicsScore = " + characteristicsScore);
			}
			catch(Exception IllegalArgumentException){
				characteristicsScore=0; //this is to accomodate for the fact that at CLEF they are idiots and they like to update the collection without say anything
			}
			double discount=Math.pow(p,rank-1);
			int relevance; 
			try {
				relevance = qrels.getAQrel(docid); //this return the integer relevance label
				if(relevance>0)
					relevance=1; //this is because of binary relevance
		    }
		    catch (Exception IllegalArgumentException) {
		        relevance = 0;
		    }
			double relevanceScore = relevanceToScore(relevance);
			rbp=rbp + relevance * discount * applyUserModel(this.docCharacteristicsKernel, characteristicsScore);
			//System.out.println(rbp);
		}
		//normalise
		rbp=(1-p) * rbp;
		return rbp;
	}
}
