import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

/*
 * This class is used to build a Lucene query from a textual (String) query.
 */
public class QueryCreator {
	
	private Analyzer analyzer;
	
	// The document fields that we use for search and their weights
	private HashMap<String,Float> searchFields = new HashMap<>(); 
	
	public QueryCreator(HashMap<String,Float> searchFields)
	{
		CharArraySet stopwords = Utils.loadStopwords(Utils.paramsMap.get("stopwordDir"));
		analyzer = new EnglishKrovetzAnalyzer(stopwords);
		this.searchFields = searchFields;
	}
	
	/*
	 * Build a Lucene query from a String.
	 */
	public Query create(String query) throws IOException{
		ArrayList<String> terms = new ArrayList<>(Arrays.asList(query.split(" ")));
		ArrayList<String> analyzedTerms = analyzeTerms(terms);
		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		for(String term : analyzedTerms) {
			BooleanQuery.Builder termQueryBuilder = new BooleanQuery.Builder();
			for(String searchField : searchFields.keySet()){
				Term t = new Term(searchField, term);
				BoostQuery boostQuery = new BoostQuery(new TermQuery(t), searchFields.get(searchField));
				BooleanClause booleanClause;
				booleanClause = new BooleanClause(boostQuery, Occur.SHOULD);
				termQueryBuilder.add(booleanClause);
			}
			booleanQueryBuilder.add(termQueryBuilder.build(), Occur.SHOULD);
		}
		return booleanQueryBuilder.build();
	}
	
	/*
	 * Pre-processing (tokenization, stemming, etc..) of set of query terms.
	 */
	public ArrayList<String> analyzeTerms(ArrayList<String> terms) throws IOException
	{
		ArrayList<String> analyzedTerms = new ArrayList<>();
		for(String term : terms)
		{
			String analyzedTerm = null;
			TokenStream tokenStream = analyzer.tokenStream("temp", term);
			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

			tokenStream.reset();
			while (tokenStream.incrementToken()) {
			    analyzedTerm = charTermAttribute.toString();
			}
			tokenStream.close();
			
			if(analyzedTerm != null)
				analyzedTerms.add(analyzedTerm);
		}
		return analyzedTerms;
	}
		
}
