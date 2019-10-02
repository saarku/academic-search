import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import org.apache.lucene.analysis.CharArraySet;

/*
 * General search engine utilities, used for indexing and search.
 */
public class Utils {
	
	public static final String configDir = "index.config";
	public static final String ABSTRACT_TAG = "<abstract>";
	public static final String TITLE_TAG = "<title>";
	public static final String INTRO_TAG = "<introduction>";
	public static final String PAPER_ID_FIELD = "paper_id";
	public static final String TITLE_FIELD = "title";
	public static final String ABSTRACT_FIELD = "abstract";
	public static final String INTRO_FIELD = "intro";
	public static final HashMap<String, String> paramsMap = parseParams(configDir);
		
	public static HashMap<String, String> parseParams(String paramsDir)
	{
		HashMap<String,String> params = new HashMap<>();
		try {
			File file = new File(paramsDir);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] args = line.split("=");
				params.put(args[0], args[1]);
			}
				fileReader.close();	
			} catch (IOException e) {
				e.printStackTrace();
			}
		return params;
	}
	
	public static CharArraySet loadStopwords(String stopwordsFileDir)
	{
		ArrayList<String> stopwordsList = new ArrayList<>();
		try {
				File file = new File(stopwordsFileDir);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stopwordsList.add(line);
				}	
				fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		CharArraySet c = new CharArraySet(stopwordsList.size(), true);
		for(String word : stopwordsList)
			c.add(word);
		
		return c;
	}
	
	public static String getContent(String tag, String content)
	{
		String output = content.replaceAll(tag, "");
		output = output.replaceAll(tag.replaceAll("<", "</"), "");
		return output;
	}
    
}