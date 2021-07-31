package org.apache.solr.search.myParser;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import java.util.ArrayList;
import java.util.List;

public class IndexPrefixBoostQParser {
	private static final String SOLRPEDIA_INSTANT_CORE = "http://localhost:8983/solr/prefixboost";

	private static final String[] names = {
			"坪洲地铁展示坪洲",
			"我是坪洲地铁站的一名",
			"坪洲地铁站",
			"坪洲地铁站好啊",
			"坪洲",
			"坪洲没有必要之坪洲",
			"地铁站坪洲坪洲地铁",
			"站内换乘地铁坪洲"
	};
	public static void main(String[] args) throws Exception {
		System.setProperty("solr.log.dir", "D:/Project/lucene-solr/solr/server/logs/http");
		HttpSolrClient client = new HttpSolrClient.Builder().withBaseSolrUrl(SOLRPEDIA_INSTANT_CORE).build();
		client.setRequestWriter(new BinaryRequestWriter());
		UpdateRequest request = new UpdateRequest();
		//这个用于设置硬提交
		request.setAction(UpdateRequest.ACTION.COMMIT, true, true);

		List<SolrInputDocument> docList = new ArrayList<>();

		int id = 0;
		for(String name : names) {
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id",++id + "");
			doc.addField("name",name);
			docList.add(doc);
		}
		request.add(docList);
		NamedList<Object> result = client.request(request);
		System.out.println("Result: " + result);

		SolrQuery query = new SolrQuery();
		//select查询测试
		query.setRequestHandler("/select");
		query.set("q", "{!prefixBoost qf=\"name exactname^10\"}\"^坪洲\"^10 \"^坪洲地铁站\"^20");
		query.set("wt", "json");
		query.set("indent", "true");
		query.set("fl", "id,name,score");
		QueryResponse response = client.query(query);
		System.out.println("以下是select查询的响应信息：\n");
		System.out.println(response.toString());
	}
}
