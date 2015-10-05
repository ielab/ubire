/**
 * 
 */
package au.edu.qut.ubire;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import au.edu.qut.ubire.DocCharacteristics;
import au.edu.qut.ubire.IRmeasures;
import au.edu.qut.ubire.Qrels;
import au.edu.qut.ubire.QueryQrels;
import au.edu.qut.ubire.QueryRanking;
import au.edu.qut.ubire.Ranking;

/**
 * @author zuccong
 *
 */
public class Ubire {

	/**
	 * @param args
	 * @throws IOException 
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException {
		
		String qrelsFile = "";
		String qreadFile = "";
		String docCharacteristics = "";
		String rankingFile = "";
		int stoprank=Integer.MAX_VALUE;
		boolean directory=true; //TODO: incorporate this in property file
		double RBPUserPatience = 0.8; //TODO: incorporate this in property file
		boolean allqueries=true; //TODO: incorporate this in property file
		if(args.length < 2) {
			//TODO: better way of handling property file if command line interaction is not used
			//load property file 
			Properties prop = new Properties();
			InputStream input = new FileInputStream("resources/config.properties");
			prop.load(input);
			qrelsFile = prop.getProperty("qrels");
			qreadFile = prop.getProperty("qread");
			docCharacteristics = prop.getProperty("docCharacteristics");
			rankingFile = prop.getProperty("ranking");
			stoprank = Integer.parseInt(prop.getProperty("stoprank"));
		}else {
			CommandLineParser parser = new DefaultParser();
			Options options = new Options();
			options.addOption( "h", "help", false, "show the help menu" );
			options.addOption( "q", "allqueries", false, "provide evaluation for every single query" );
			options.addOption( "d", "directory", false, "evaluate all runs in the given directory, not just a single run" );
			
			options.addOption( OptionBuilder.withLongOpt( "qrels-file" )
					.isRequired()
                    .withDescription( "specify qrels file" )
                    .hasArg()
                    .withArgName("qrels")
                    .create() );
			options.addOption( OptionBuilder.withLongOpt( "qread-file" )
                    .withDescription( "specify qread file (file with readability scores)" )
                    .hasArg()
                    .withArgName("qread")
                    .create() );
			options.addOption( OptionBuilder.withLongOpt( "rbp-p" )
                    .withDescription( "specify the value of the rbp user persistence parameter p" )
                    .hasArg()
                    .withArgName("rbp-p")
                    .create() );
			options.addOption( OptionBuilder.withLongOpt( "ranking-file" )
					.isRequired()
                    .withDescription( "specify file containing document ranking to evaluate" )
                    .hasArg()
                    .withArgName("ranking")
                    .create() );
			options.addOption( OptionBuilder.withLongOpt( "readability" )
                    .withDescription( "specify whether to use readability-biased evaluation" )
                    .create() );
			options.addOption( OptionBuilder.withLongOpt( "stoprank" )
                    .withDescription( "specify at which rank to stop evaluating" )
                    .create() );
			HelpFormatter formatter = new HelpFormatter();
			//TODO: resolve deprecations above
			try {
			    // parse the command line arguments
			    CommandLine line = parser.parse( options, args );
			    
			    qrelsFile = line.getOptionValue("qrels-file");
			    rankingFile = line.getOptionValue("ranking-file");
			    if( line.hasOption( "qread-file" ) )
			    	qreadFile = line.getOptionValue("qread-file");
			    if( line.hasOption( "readability" ) )
			    	docCharacteristics="true";
			    if( line.hasOption( "directory" ) )
			    	directory = true;
			    else
			    	directory = false;
			    if( line.hasOption( "rbp-p" ) )
			    	RBPUserPatience=Double.parseDouble(line.getOptionValue("rbp-p"));
			    if( line.hasOption( "q" ) )
			    	allqueries=true;
			    else
			    	allqueries=false;
			    if( line.hasOption( "h" ) )
			    	formatter.printHelp( "eire", options );
			    if( line.hasOption( "stoprank" ) )
			    	stoprank = Integer.parseInt(line.getOptionValue("stoprank"));
			    else
			    	stoprank = Integer.MAX_VALUE;
			}catch( ParseException exp ) {
			    System.out.println( "Unexpected exception:" + exp.getMessage() );
			}
		}
		
		
		
		
		boolean considerDocCharacteristics=false;
		if(docCharacteristics.length()>0)
			considerDocCharacteristics=true;
		
		//load qrels - with separation character as the default separation character
		QueryQrels qrels = new QueryQrels();
		qrels.readQrelsFile(qrelsFile, "", true);
		qrels.readDocCharacteristicsFileQrelsFormat(qreadFile, "\t");
		TreeMap<String,Qrels> queryQrels = new TreeMap<String,Qrels>(qrels.getQueryQrels());
		DocCharacteristics docChar = new DocCharacteristics();
		if(docCharacteristics.equalsIgnoreCase("true"))
		    	docChar=qrels.getDocCharacteristics();

		HashMap<Double,Double> readabilityGradeMapping = new HashMap<Double,Double>();
		readabilityGradeMapping.put(0.0, 0.0);
		readabilityGradeMapping.put(1.0, 0.4);
		readabilityGradeMapping.put(2.0, 0.8);
		readabilityGradeMapping.put(3.0, 1.0);
		
		if(directory){
			DirectoryStream<Path> ds = Files.newDirectoryStream(FileSystems.getDefault().getPath(rankingFile));
			for (Path p : ds) {
			//for (File file : listOfFiles) {
			    File file = p.toFile();
				if (file.isFile() && !file.getName().startsWith(".")) {
					QueryRanking rankings = new QueryRanking();
					rankings.readRankingFile(file.getAbsolutePath(), " ");
					
					
					double average=0.0,averageRbp =0.0, averageRbpDc=0.0, averageRbpDcGraded=0.0; 
					//double averageNdcg=0.0; double averageNdcgDc=0.0;
					for (Entry<String, Qrels> entry : queryQrels.entrySet()) {
					    String queryid = entry.getKey();
					    Qrels qrel = entry.getValue();
					    Ranking ranking = rankings.getQueryRanking().get(queryid);
					    IRmeasures measures = new IRmeasures(qrel, docChar, ranking, "linear", "readability", "model5");
					    IRmeasures measuresGraded = new IRmeasures(qrel, docChar, ranking, "linear", "readability", "model4");
					    measuresGraded.setDocCharacteristicsGradeMapping(readabilityGradeMapping);
					    //System.out.println("Query " + queryid + "\tndcg: " + measures.ndcgDocCharacteristics());// .ndcgJarvelin());
					    //average = average +  measures.ndcgDocCharacteristics(); //measures.ndcgJarvelin();
					    
					    //System.out.println("Query " + queryid + "\trbp(0.95): " + measures.rbp(0.95));// .ndcgJarvelin());
					    double thisRbp = measures.rbp(RBPUserPatience, stoprank);
					    averageRbp = averageRbp + thisRbp; //measures.ndcgJarvelin();
					    
					    //System.out.println("Query " + queryid + "\trbpDc(0.95): " + measures.rbpDocCharacteristics(0.95));// .ndcgJarvelin());
					   //System.out.println("computing RBP with DocCharact. "); 
					   double thisRbpDc = measures.rbpDocCharacteristics(RBPUserPatience, stoprank);
					   averageRbpDc = averageRbpDc +  thisRbpDc; //measures.ndcgJarvelin();
					   double thisRbpDcGraded = measuresGraded.rbpDocCharacteristics(RBPUserPatience, stoprank);
					   averageRbpDcGraded = averageRbpDcGraded +  thisRbpDcGraded;
					   if(allqueries) {
						   System.out.println("RBP(" + Double.toString(RBPUserPatience) + ")\t" + queryid + "\t" + file.getName() + "\t" + String.format( "%.4f", thisRbp ));
						   System.out.println("uRBP(" + Double.toString(RBPUserPatience) + ")\t" + queryid + "\t" + file.getName() + "\t" + String.format( "%.4f", thisRbpDc )); 
						   System.out.println("uRBPgr(" + Double.toString(RBPUserPatience) + ")\t" + queryid + "\t" + file.getName() + "\t" + String.format( "%.4f", thisRbpDcGraded ));  
					   }
					}
					
					System.out.println("RBP(" + Double.toString(RBPUserPatience) + ")\t" + "all      " + "\t" + file.getName()+ "\t" + String.format( "%.4f", averageRbp/(double)queryQrels.size() ));
					System.out.println("uRBP(" + Double.toString(RBPUserPatience) + ")\t" + "all      " + "\t" + file.getName()+ "\t" + String.format( "%.4f", averageRbpDc/(double)queryQrels.size() )); 
					System.out.println("uRBPgr(" + Double.toString(RBPUserPatience) + ")\t" + "all      " + "\t" + file.getName()+ "\t" + String.format( "%.4f", averageRbpDcGraded/(double)queryQrels.size() ));
					
				}
			}
		}else{
			
			QueryRanking rankings = new QueryRanking();
			rankings.readRankingFile(rankingFile, " ");
			File file = new File(rankingFile);
			String runname = file.getName();
			
			double average=0.0,averageRbp =0.0, averageRbpDc=0.0, averageRbpDcGraded=0.0; 
			//double averageNdcg=0.0; double averageNdcgDc=0.0;
			for (Entry<String, Qrels> entry : queryQrels.entrySet()) {
			    String queryid = entry.getKey();
			    Qrels qrel = entry.getValue();
			    Ranking ranking = rankings.getQueryRanking().get(queryid);
			    IRmeasures measures = new IRmeasures(qrel, docChar, ranking, "linear", "readability", "model5");
			    IRmeasures measuresGraded = new IRmeasures(qrel, docChar, ranking, "linear", "readability", "model4");
			    measuresGraded.setDocCharacteristicsGradeMapping(readabilityGradeMapping);
			    //System.out.println("Query " + queryid + "\tndcg: " + measures.ndcgDocCharacteristics());// .ndcgJarvelin());
			    //average = average +  measures.ndcgDocCharacteristics(); //measures.ndcgJarvelin();
			    
			    //System.out.println("Query " + queryid + "\trbp(0.95): " + measures.rbp(0.95));// .ndcgJarvelin());
			    double thisRbp = measures.rbp(RBPUserPatience, stoprank);
			    averageRbp = averageRbp + thisRbp; //measures.ndcgJarvelin();
			    
			    //System.out.println("Query " + queryid + "\trbpDc(0.95): " + measures.rbpDocCharacteristics(0.95));// .ndcgJarvelin());
			   //System.out.println("computing RBP with DocCharact. "); 
			   double thisRbpDc = measures.rbpDocCharacteristics(RBPUserPatience, stoprank);
			   averageRbpDc = averageRbpDc +  thisRbpDc; //measures.ndcgJarvelin();
			   double thisRbpDcGraded = measuresGraded.rbpDocCharacteristics(RBPUserPatience, stoprank);
			   averageRbpDcGraded = averageRbpDcGraded +  thisRbpDcGraded;
			   if(allqueries) {
				   System.out.println("RBP(" + Double.toString(RBPUserPatience) + ")\t" + queryid + "\t" + runname + "\t" + String.format( "%.4f", thisRbp ));
				   System.out.println("uRBP(" + Double.toString(RBPUserPatience) + ")\t" + queryid + "\t" + runname + "\t" + String.format( "%.4f", thisRbpDc )); 
				   System.out.println("uRBPgr(" + Double.toString(RBPUserPatience) + ")\t" + queryid + "\t" + runname + "\t" + String.format( "%.4f", thisRbpDcGraded ));
			   }
			}
			
			System.out.println("RBP(" + Double.toString(RBPUserPatience) + ")\t" + "all      " + "\t" + String.format( "%.4f", averageRbp/(double)queryQrels.size() ));
			System.out.println("uRBP(" + Double.toString(RBPUserPatience) + ")\t" + "all      " + "\t" + String.format( "%.4f", averageRbpDc/(double)queryQrels.size() )); 
			System.out.println("uRBPgr(" + Double.toString(RBPUserPatience) + ")\t" + "all      " + "\t" + String.format( "%.4f", averageRbpDcGraded/(double)queryQrels.size() ));
			
			
		}
		
	}


}
