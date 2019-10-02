import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Build an index from a collection of documents, where each document is in a text file.
 * Each document field is in a single line and is surrounded by tags which idicate the field name.
 * For example:
 * <title>this is the title</title>
 * <abstract>this is the abstract</abstract>
 * <introduction>this is the introduction</introduction>
 */
public class IndexBuilder {
	
	private String dataDir; // A directory with the document text files.
	private String indexDir; // A directory for the index (make sure it does not already exist!).
	private CharArraySet stopwords; // A text file with stop-words.
	
 	public IndexBuilder(String configDir) throws IOException
	{
		dataDir = Utils.paramsMap.get("dataDir");
		indexDir = Utils.paramsMap.get("indexDir");
		stopwords = Utils.loadStopwords(Utils.paramsMap.get("stopwordDir"));
	}
	
 	/*
 	 * Build the index from the collection of documents.
 	 */
 	public void build() throws IOException
	{
 		int numDocuments = 0;
		Path p = Paths.get(indexDir);
		Directory indexDirectory = FSDirectory.open(p);
		Analyzer analyzer = new EnglishKrovetzAnalyzer(stopwords);
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		Similarity sim = new BM25Similarity();
		conf.setSimilarity(sim);
		IndexWriter writer = new IndexWriter(indexDirectory, conf);
		
		try {
				File folder = new File(dataDir);
				File[] listOfFiles = folder.listFiles();
			    for (int i = 0; i < listOfFiles.length; i++){
			    		File singleFile = listOfFiles[i];
			    		String paperId = singleFile.getName().split("\\.")[0];
			    		
			    		if(paperId.split("\\-").length == 2) 
			    			paperId = paperId.split("\\-")[0] + paperId.split("\\-")[1];
			    		
					FileReader fileReader = new FileReader(singleFile);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					String line;
					
					String abstractField = "";
					String titleField = "";
					String introField = ""; 
					
					while ((line = bufferedReader.readLine()) != null) {
						line = line.replaceAll("\n", "");

						if(line.contains(Utils.INTRO_TAG)){
							introField = Utils.getContent(Utils.INTRO_TAG, line);
						}
						else if(line.contains(Utils.ABSTRACT_TAG)){
							abstractField = Utils.getContent(Utils.ABSTRACT_TAG, line);
						}
						else if(line.contains(Utils.TITLE_TAG)){
							titleField = Utils.getContent(Utils.TITLE_TAG, line);
						}
					}
					
					if(!introField.equals("") || !abstractField.equals("") || !titleField.equals("")) {
						Document doc = createDocument(paperId, titleField, abstractField, introField);
						writer.addDocument(doc);
						numDocuments++;
					}
					fileReader.close();
			    }
				writer.close();
				System.out.println("number of indexed articles: " + numDocuments);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 	
 	/* 
 	 * Create a single document from a set of given textual fields.
 	 */
	public Document createDocument(String paperId, String title, String abs, String intro)
	{
		Document document = new Document();
		StringField paperIdField = new StringField(Utils.PAPER_ID_FIELD, paperId, Field.Store.YES);
		TextField titleField = new TextField(Utils.TITLE_FIELD, title, Field.Store.YES);
		TextField absField = new TextField(Utils.ABSTRACT_FIELD, abs, Field.Store.YES);
		TextField introField = new TextField(Utils.INTRO_FIELD, intro, Field.Store.YES);
		
		document.add(paperIdField);
		document.add(titleField);
		document.add(absField);
		document.add(introField);
		return document;
	}
	
	/*
	 * Use this code to debug an existing index.
	 */
 	public void checkIndex() throws IOException
 	{
		Path p = Paths.get(Utils.paramsMap.get("indexDir"));
		Directory indexDirectory = FSDirectory.open(p);
		IndexReader reader = DirectoryReader.open(indexDirectory);
		System.out.println("number of indexed documents:" + reader.maxDoc());
		IndexSearcher searcher = new IndexSearcher(reader);
		
		int numberOfExamples = 100;
		for (int i = 0; i < numberOfExamples; i++) {
			Document HitDoc = searcher.doc(i);
			System.out.println("----------- " + i + " -----------");
			List<IndexableField> fields = HitDoc.getFields();
			for(IndexableField f : fields)
			{
				String fieldName = f.name();
				String content = HitDoc.get(fieldName);
				System.out.println(fieldName + ": " + content);
			}
		}
 	}
 	
	public static void main(String[] args) throws IOException
	{
		IndexBuilder builder = new IndexBuilder("config.txt");
		builder.checkIndex();
	}
}