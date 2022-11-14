package org.apache.solr.search.function.myfunc.match;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.function.myfunc.OnlineMultiFloatFunction;

import java.util.List;
import java.util.Map;

public class FieldMatchSortFloatFunction extends OnlineMultiFloatFunction {
	private final Map<ValueSource,List<FieldMatchModel>> fieldMap;

	public FieldMatchSortFloatFunction(ValueSource[] sources, Map<ValueSource,List<FieldMatchModel>> fieldMap) {
		super(sources);
		this.fieldMap = fieldMap;
	}

	@Override
	protected float func(int doc, FunctionValues[] valsArr) {
		if (!fieldMap.isEmpty()) {
			float score = 0;
			for (int i = 0, valsArrLength = valsArr.length; i < valsArrLength; i++) {
				try {
					Object value = valsArr[i].objectVal(doc);
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