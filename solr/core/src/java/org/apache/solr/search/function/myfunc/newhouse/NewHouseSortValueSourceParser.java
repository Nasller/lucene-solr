package org.apache.solr.search.function.myfunc.newhouse;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.ArrayList;
import java.util.List;

public class NewHouseSortValueSourceParser extends ValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		List<ValueSource> list = new ArrayList<>();
		list.add(getValueSource(fp,"gardenName"));
		list.addAll(fp.parseValueSourceList());
		return new NewHouseSortFloatFunction(fp.getReq().getSearcher(),list.toArray(new ValueSource[0]));
	}

	public ValueSource getValueSource(FunctionQParser fp, String arg) {
		SchemaField f = fp.getReq().getSchema().getField(arg);
		return f.getType().getValueSource(f, fp);
	}
}