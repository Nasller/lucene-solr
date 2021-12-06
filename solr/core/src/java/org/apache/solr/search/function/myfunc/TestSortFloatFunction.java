package org.apache.solr.search.function.myfunc;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSortFloatFunction extends MultiFloatFunction {
	private static final Logger logger = LoggerFactory.getLogger(TestSortFloatFunction.class);

	public TestSortFloatFunction(ValueSource[] sources) {
		super(sources);
	}

	@Override
	protected float func(int doc, FunctionValues[] vals) {
		try {
			String id = vals[0].strVal(doc);
			String param = vals[1].strVal(doc);
			if(id.equals(param)){
				logger.info("success");
				return 100;
			}else{
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	protected String name() {
		return "testMyFunc";
	}
}