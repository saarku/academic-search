# Academic Search Toolkit

A search engine toolkit (back-end and interface) for searching over research atricles.

## General architecture

The back-end search engine is written in Java using the open-sourced Lucene toolkit. 
A seperate Python server recieves requests from the client, issue them to the Java back-end, and returns the result to the client.

## How to run the demo?

1. Before starting the demo, you need to make sure that the port numbers are set correctly. 
The port number in 'SearchEngineServer.java' and the corresponding port number in 'server.py' should be the same (Python-Java communication). Also, the port number in 'index.html' and the corresponding port in 'server.py' should be the same (Client-Server communication). The code is currently set to work locally. If you want to depoly this on a server, please modify the URL/IP in both 'index.html' and 'server.py'. (If you just downloaded this repository, probably everything is set correctly, unless one of the ports is occupied.)

2. Build and run the java server.
- Build: javac -cp "./:src/:jar/*" src/SearchEngineServer.java
- Run: java -cp "./:src/:jar/*" SearchEngineServer

If successful, you should see a message "Gateway Server Started".

3. Run the python server (server.py).

4. Access the demo through index.html by either clicking on the file if built locally, or by accessing the URL if on a server.

