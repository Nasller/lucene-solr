package org.apache.solr.search.function.myfunc.newhouse;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.function.myfunc.OnlineValueSourceParser;

import java.util.ArrayList;
import java.util.List;

public class NewHouseSortValueSourceParser extends OnlineValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		List<ValueSource> list = new ArrayList<>();
		list.add(getValueSource(fp, "newhouseId"));
		list.add(getValueSource(fp, "ranking"));
		list.add(getValueSource(fp, "city"));
		list.addAll(fp.parseValueSourceList());
		return new NewHouseSortFloatFunction(list.toArray(new ValueSource[0]));
	}
}