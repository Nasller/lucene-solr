package org.apache.solr.search.function.myfunc.match;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.LongDocValues;
import org.apache.solr.search.function.myfunc.OnlineMultiFloatFunction;
import org.apache.solr.search.function.myfunc.common.SortUtil;

import java.util.List;
import java.util.Map;

public class FieldMatchSortFloatFunction extends OnlineMultiFloatFunction {
	private final Map<ValueSource,List<FieldMatchModel>> fieldMap;
	private final int sortFieldIndex;

	public FieldMatchSortFloatFunction(ValueSource[] sources,int sortFieldIndex, Map<ValueSource,List<FieldMatchModel>> fieldMap) {
		super(sources);
		this.fieldMap = fieldMap;
		this.sortFieldIndex = sortFieldIndex;
	}

	@Override
	protected float func(int doc, FunctionValues[] valsArr) {
		if (!fieldMap.isEmpty()) {
			float score = 0;
			for (int i = 0, valsArrLength = valsArr.length; i < valsArrLength; i++) {
				try {
					Object value;
					FunctionValues values = valsArr[i];
					if(i == sortFieldIndex){
						value = SortUtil.getMap(valsArr[i].strVal(doc),false);
					}else if(values instanceof LongDocValues){
						value =  values.longVal(doc);
					}else value = values.objectVal(doc);
					if(value == null) continue;
					for (FieldMatchModel model : fieldMap.get(sources[i])) {
						if(model.getMatchRuleType().getPredicate().test(model,value)) score += model.getBoost();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return score;
		}
		return 0;
	}

	@Override
	protected String name() {
		return "fieldMatch";
	}
}