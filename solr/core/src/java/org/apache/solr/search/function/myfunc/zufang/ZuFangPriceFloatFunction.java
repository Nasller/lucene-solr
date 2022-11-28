package org.apache.solr.search.function.myfunc.zufang;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.function.myfunc.OnlineMultiFloatFunction;

public class ZuFangPriceFloatFunction extends OnlineMultiFloatFunction {
	public ZuFangPriceFloatFunction(ValueSource[] sources) {
		super(sources);
	}

	@Override
	protected float func(int doc, FunctionValues[] vals) {
		try {
			double priceStart = vals[0].doubleVal(doc);
			double priceEnd = vals[1].doubleVal(doc);
			float boost = vals[2].floatVal(doc);
			double monthPrice = vals[3].doubleVal(doc);
			if(priceStart <= monthPrice && monthPrice <= priceEnd){
				return boost;
			}
			double solicitBudgetStart = vals[4].doubleVal(doc);
			double solicitBudgetEnd = vals[5].doubleVal(doc);
			boolean checkStart = solicitBudgetStart >= priceStart || solicitBudgetEnd >= priceStart;
			boolean checkEnd = (0 < solicitBudgetEnd && solicitBudgetEnd <= priceEnd) || (0 < solicitBudgetStart && solicitBudgetStart <= priceEnd);
			if(checkStart && checkEnd){
				return boost;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	protected String name() {
		return "zufangPrice";
	}
}