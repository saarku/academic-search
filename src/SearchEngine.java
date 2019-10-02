import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/*
 * A search engine tool. Opens an existing queries and support search for textual queries.
 */
public class SearchEngine {
	
	public IndexSearcher searcher;
	public IndexReader reader;
	public QueryCreator queryCreator;
	public HashMap<String, Float> searchFields = new HashMap<>();

	public SearchEngine(String indexDir) throws IOException
	{
		searchFields.put(Utils.ABSTRACT_FIELD, 0.4f);
		searchFields.put(Utils.TITLE_FIELD, 0.4f);
		searchFields.put(Utils.INTRO_FIELD, 0.2f);
		Path p = Paths.get(indexDir);
		Directory indexDirectory = FSDirectory.open(p);
		reader = DirectoryReader.open(indexDirectory);
		searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity());
		queryCreator = new QueryCreator(searchFields);
	}
	
	/*
	 * Retrieve a result list with respect to a query.
	 */
	public ScoreDoc[] search(String query, int numResults) throws IOException
	{	
		Query q = queryCreator.create(query);
		TopDocs docs = searcher.search(q, numResults);	
		return docs.scoreDocs;
	}
	
	/*
	 * Get the content of documents (e.g., abstract and title) of a result list.
	 */
	public ArrayList<HashMap<String,String>> getContent(ScoreDoc[] result) throws IOException
	{	
		ArrayList<HashMap<String,String>> output = new ArrayList<>();
		for(int i = 0; i < result.length; i++)
		{
			HashMap<String,String> singleMap = new HashMap<>();
			Document HitDoc = searcher.doc(result[i].doc);
			List<IndexableField> fields = HitDoc.getFields();
			for(IndexableField f : fields)
			{	
				String fieldName = f.name();
				String content = HitDoc.get(fieldName);
				singleMap.put(fieldName, content);
			}
			output.add(singleMap);
		}
		return output;
	}
}
