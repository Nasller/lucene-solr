package org.apache.solr.search.function.myfunc.room;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.ArrayList;
import java.util.List;

public class FindSaleValueSourceParser extends ValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		List<ValueSource> list = new ArrayList<>();

		// 索引的值
		list.add(getValueSource(fp, "id"));
		list.add(getValueSource(fp, "direction"));
		list.add(getValueSource(fp, "bedRoom"));
		list.add(getValueSource(fp, "subwayDesc"));
		list.add(getValueSource(fp, "elevator"));
		list.add(getValueSource(fp, "decoration"));
		list.add(getValueSource(fp, "buildingArea"));
		list.add(getValueSource(fp,"sortField"));
		list.addAll(fp.parseValueSourceList());
		return new FindSaleFloatFunction(fp.getReq().getParams(),list.toArray(new ValueSource[0]));
	}

	public ValueSource getValueSource(FunctionQParser fp, String arg) {
		SchemaField f = fp.getReq().getSchema().getField(arg);
		return f.getType().getValueSource(f, fp);
	}
}