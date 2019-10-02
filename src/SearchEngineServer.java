import java.io.IOException;
import py4j.GatewayServer;

/*
 * An interface to the search engine that can interact with Python code.\
 * Make sure that the port number is the same here and in the Python code.
 */
public class SearchEngineServer {
	
	public SearchEngine engine;
	
	public SearchEngineServer() throws IOException {		
		engine = new SearchEngine(Utils.paramsMap.get("indexDir"));
	}
	
	public SearchEngine getSearchEngine() {
		return engine;
	}
	
 	public static void main(String[] args) throws IOException {
 		int port_number = 26000;
		GatewayServer gatewayServer = new GatewayServer(new SearchEngineServer(), port_number);
        gatewayServer.start();
        System.out.println("Gateway Server Started");
 	}
}
