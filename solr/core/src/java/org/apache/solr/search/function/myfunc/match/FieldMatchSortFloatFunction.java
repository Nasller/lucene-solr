package org.apache.solr.search.function.myfunc.match;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.function.myfunc.OnlineMultiFloatFunction;

import java.util.List;

public class FieldMatchSortFloatFunction extends OnlineMultiFloatFunction {
	private final List<FieldMatchModel> list;

	public FieldMatchSortFloatFunction(ValueSource[] sources, List<FieldMatchModel> list) {
		super(sources);
		this.list = list;
	}

	@Override
	protected float func(int doc, FunctionValues[] valsArr) {
		if (!list.isEmpty()) {
			float score = 0;
			for (int i = 0, valsArrLength = valsArr.length; i < valsArrLength; i++) {
				try {
					FieldMatchModel model = list.get(i);
					Object value = valsArr[i].objectVal(doc);
					if(model.getMatchRuleType().getPredicate().test(model,value)) score += model.getBoost();
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