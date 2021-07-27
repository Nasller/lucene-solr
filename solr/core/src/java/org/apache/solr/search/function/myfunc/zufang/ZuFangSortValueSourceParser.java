package org.apache.solr.search.function.myfunc.zufang;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.ArrayList;
import java.util.List;

public class ZuFangSortValueSourceParser extends ValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		List<ValueSource> list = new ArrayList<>();
		list.add(getValueSource(fp, "title"));
		list.add(getValueSource(fp, "desc"));
		list.add(getValueSource(fp, "address"));
		list.add(getValueSource(fp, "sortField"));
		list.addAll(fp.parseValueSourceList());
		List<String> keywords = new ArrayList<>();
		try {
			while (fp.hasMoreArguments()){
				keywords.add(fp.parseArg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ZuFangSortFloatFunction(list.toArray(new ValueSource[0]),keywords);
	}

	public ValueSource getValueSource(FunctionQParser fp, String arg) {
		SchemaField f = fp.getReq().getSchema().getField(arg);
		return f.getType().getValueSource(f, fp);
	}
}
