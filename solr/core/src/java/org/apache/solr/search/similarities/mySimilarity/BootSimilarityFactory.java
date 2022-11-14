package org.apache.solr.search.similarities.mySimilarity;

import org.apache.lucene.search.similarities.Similarity;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.schema.SimilarityFactory;

public class BootSimilarityFactory extends SimilarityFactory {
	@Override
	public void init(SolrParams params) {
		System.out.println(params.get("paramKey"));
	}

	@Override
	public Similarity getSimilarity(){
		return new  BootSimilarity();
	}
}