package org.apache.solr.search.function.myfunc.newhouse;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;
import org.apache.solr.search.SolrIndexSearcher;

import java.util.Set;

public class NewHouseSortFloatFunction extends MultiFloatFunction {
	private final SolrIndexSearcher searcher;
	public NewHouseSortFloatFunction(SolrIndexSearcher searcher,ValueSource[] sources) {
		super(sources);
		this.searcher = searcher;
	}

	@Override
	protected float func(int doc, FunctionValues[] vals) {
		try {
			System.out.println(searcher.getDocFetcher().doc(doc, Set.of()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	protected String name() {
		return "newhousesort";
	}

}