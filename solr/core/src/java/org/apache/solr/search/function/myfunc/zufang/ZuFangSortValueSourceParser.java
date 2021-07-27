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
		ValueSource title = getValueSource(fp, "title");
		ValueSource desc = getValueSource(fp, "desc");
		ValueSource address = getValueSource(fp, "address");
		ValueSource sortField = getValueSource(fp, "sortField");
		List<String> list = new ArrayList<>();
		try {
			while (fp.hasMoreArguments()){
				list.add(fp.parseArg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ZuFangSortFloatFunction(new ValueSource[]{title,desc,address,sortField},list);
	}

	public ValueSource getValueSource(FunctionQParser fp, String arg) {
		SchemaField f = fp.getReq().getSchema().getField(arg);
		return f.getType().getValueSource(f, fp);
	}
}
