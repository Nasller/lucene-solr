package org.apache.solr.search.function.myfunc.garden;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.LiteralValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小区排序
 */
public class GardenSortValueSourceParser extends ValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		List<ValueSource> sources = fp.parseValueSourceList();
        Map<Integer,Integer> map = new HashMap<>();
		try {
            LiteralValueSource id = (LiteralValueSource) sources.get(0);
            String gardenId = id.getValue();
            map.put(Integer.parseInt(gardenId.split("\\^")[0]),Integer.parseInt(gardenId.split("\\^")[1]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new GardenSortFloatFunction(sources.toArray(new ValueSource[sources.size()]),map);
	}
}
