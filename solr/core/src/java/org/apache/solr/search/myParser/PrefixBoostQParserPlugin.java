package org.apache.solr.search.myParser;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

/**
 * PrefixBoostQParser插件
 */
public class PrefixBoostQParserPlugin extends QParserPlugin {
	public static final String NAME = "prefixBoost";

	@Override
	public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		return new PrefixBoostQParser(qstr, localParams, params, req);
	}

	public void init(NamedList namedList) {

	}
}
