from flask import Flask, Response, send_file
from flask_cors import CORS
import os
from py4j.java_gateway import JavaGateway, GatewayParameters
import json
from flask import request
import time
import sys

app = Flask(__name__)
CORS(app)


@app.route("/search/", methods=['GET', 'POST'])
def search():
    # Issuing a search using a query from the client.

    global sei

    # parse the request from the client
    json_data = request.get_data(as_text=True)
    data = json.loads(json_data)
    query = data['query']
    ip = data['ip']
    page_id = int(data['page'])

    # log the search event
    time_stamp = str(time.time())
    print("search_event" + "," + ip + "," + time_stamp + "," + query.replace(',',"commasign") + "," + str(page_id))
    sys.stdout.flush()

    # issue a search and prepare a result list
    results = sei.search(query)
    titles = []
    abstracts = []
    urls = []
    ids = []
    docs_content = sei.search_engine.getContent(results)
    for single_dict in docs_content[page_id*10:page_id*10+10]:
        abstracts += [sei.boldface_query(single_dict['abstract'], query)]
        titles += [single_dict['title']]
        urls += [sei.get_pdf_url(single_dict['paper_id'])]
        ids += ['paperid' + str(single_dict['paper_id'].encode('utf-8')).replace(" ", "")]

    tore_turn = {'titles': titles, 'abstracts': abstracts, 'urls': urls, 'ids': ids}

    # send the result list to the client
    response = Response(response=json.dumps(tore_turn), status=200, mimetype='application/json')
    return response


@app.route("/buttonlog/", methods=['GET', 'POST'])
def button_log():
    # Logging an event of relevant/non-relevant judgement

    json_data = request.get_data(as_text=True)
    data = json.loads(json_data)
    query = data['query']
    args = data['value'].split("_")
    val = "_".join(args[0:len(args)-1])
    rank = args[len(args)-1]
    ip = data['ip']
    time_stamp = str(time.time())

    print("button_event" + "," + ip + "," + time_stamp + "," + query.replace(',',"commasign") + "," + val + "," + rank)
    sys.stdout.flush()
    return ''


@app.route("/urllog/", methods=['GET', 'POST'])
def url_log():
    # Logging an event of clicking on a URL

    json_data = request.get_data(as_text=True)
    data = json.loads(json_data)
    query = data['query']
    url = data['value']
    ip = data['ip']
    rank = data['rank']
    time_stamp = str(time.time())
    print("url_event" + "," + ip + "," + time_stamp + "," + query.replace(',',"commasign") + "," + url + "," + rank)
    sys.stdout.flush()
    return ''


@app.route('/')
def index():
    # Returns the main page when using the app URL

    return send_file('index.html')


class SearchEngineInterface():
    # A class that interacts with the Lucene search engine server.

    def __init__(self):
        self.search_engine = server.getSearchEngine()

    def search(self, query):
        # Simple search with a keyword query.

        results = self.search_engine.search(query, 1000)
        return results

    @staticmethod
    def get_pdf_url(file_name):
        # returns the URL of a result document in the ACL Anthology.

        name = file_name[0:3] + '-' + file_name[3:7]
        return "http://www.aclweb.org/anthology/" + name + ".pdf"

    @staticmethod
    def boldface_query(text, query):
        # Generate a snippet by bold-facing the query terms in the text.

        keywords = [w.lower() for w in query.split()]
        words = text.split()

        for i, word in enumerate(words):
            for keyword in keywords:
                if keyword in word.lower():
                    words[i] = '<b>' + word + '</b>'
                    break
        return ' '.join(words).rstrip("\"").rstrip(".") + "."


if __name__ == '__main__':

    search_engine_port = 25000  # should be the same as in 'SearchEngineServer.java'
    flask_port = 1200  # should be the same as in 'index.html'

    server = JavaGateway(gateway_parameters=GatewayParameters(port=25000))
    sei = SearchEngineInterface()
    if not os.path.exists("logs"): os.mkdir("logs")
    sys.stdout = open("logs/" + str(time.time()) + ".log", "w")
    app.run(host='0.0.0.0', port=1200)