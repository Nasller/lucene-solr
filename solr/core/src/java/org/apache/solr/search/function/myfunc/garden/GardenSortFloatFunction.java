package org.apache.solr.search.function.myfunc.garden;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;
import org.apache.solr.search.function.myfunc.SortUtil;

import java.util.Map;

/**
 * 小区排序
 */
public class GardenSortFloatFunction extends MultiFloatFunction {
	private Map<Integer,Integer> idMap;
	public GardenSortFloatFunction(ValueSource[] sources, Map<Integer,Integer> idMap) {
		super(sources);
		this.idMap = idMap;
	}

	@Override
	protected float func(int doc, FunctionValues[] vals) {
		try {
			String idStr = vals[0].strVal(doc);
			int gardenId = SortUtil.getId(idStr);
            float ranking = 0;
            if(gardenId>0 && idMap.get(gardenId)!=null){
                ranking += 1000*idMap.get(gardenId);
            }
			return ranking;
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	protected String name() {
		return "gardensort";
	}

}
