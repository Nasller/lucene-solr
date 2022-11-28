package org.apache.solr.search.function.myfunc.match;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FieldMatchValueSourceParser extends ValueSourceParser {
	public static final String DEFAULT_SORT_FIELD = "sortField";
	public static final String SORT_FIELD_SUFFIX = ".multiple";
	public static final String SORT_FIELD_SPLIT = "\\|\\|\\|";

	/**
	 * 字符串contains equals 数字equals range
	 * title:"南山" contains title:南山 equals
	 * 查询方法传参 field.match=title:"南山"^10&field.match=sortField.regionIds:28^1
	 */
	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		Map<ValueSource,List<FieldMatchModel>> fieldMap = new LinkedHashMap<>();
		SolrParams solrParams = fp.getReq().getParams();
		String[] params = solrParams.getParams("field.match");
		int sortFieldIndex = -1;
		if(params != null){
			String sortField = solrParams.get("field.sortField",DEFAULT_SORT_FIELD);
			for (String param : params) {
				String[] split = param.split(":");
				if(split.length > 1){
					FieldMatchModel matchModel = new FieldMatchModel();
					matchModel.setNotEqual(split[0].startsWith("-"));
					String field = matchModel.isNotEqual() ? split[0].replaceFirst("-", "") : split[0];
					if (field.startsWith(sortField + ".")) {
						matchModel.setSortFieldKey(field.replaceFirst(sortField + "\\.",""));
						matchModel.setField(sortField);
						if(sortFieldIndex < 0) sortFieldIndex = fieldMap.size();
					}else matchModel.setField(field);
					String splitParam = split[1];
					if(splitParam.contains("^")){
						split = splitParam.split("\\^");
						matchModel.setParam(split[0]);
						matchModel.setBoost(Integer.parseInt(split[1]));
					}else matchModel.setParam(splitParam);
					fieldMap.computeIfAbsent(getValueSource(fp, matchModel.getField()),o->new ArrayList<>()).add(matchModel);
				}
			}
		}
		return new FieldMatchSortFloatFunction(fieldMap.keySet().toArray(new ValueSource[0]),sortFieldIndex,fieldMap);
	}

	public ValueSource getValueSource(FunctionQParser fp, String arg) {
		SchemaField f = fp.getReq().getSchema().getField(arg);
		return f.getType().getValueSource(f, fp);
	}
}