package org.apache.solr.search.function.myfunc.zufang;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.function.myfunc.OnlineValueSourceParser;

import java.util.ArrayList;
import java.util.List;

public class ZuFangPriceValueSourceParser extends OnlineValueSourceParser {
	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		List<ValueSource> list = new ArrayList<>(fp.parseValueSourceList());
		list.add(getValueSource(fp, "monthPrice"));
		list.add(getValueSource(fp, "solicitBudgetStart"));
		list.add(getValueSource(fp, "solicitBudgetEnd"));
		return new ZuFangPriceFloatFunction(list.toArray(new ValueSource[0]));
	}
}