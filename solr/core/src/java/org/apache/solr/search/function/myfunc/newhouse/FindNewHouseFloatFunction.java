package org.apache.solr.search.function.myfunc.newhouse;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.search.function.myfunc.common.FindHouseUtil;
import org.apache.solr.search.function.myfunc.common.SortUtil;

import java.util.Map;

public class FindNewHouseFloatFunction extends MultiFloatFunction {
	private final FindHouseUtil findHouseUtil;
	private final boolean printLog;

	public FindNewHouseFloatFunction(SolrParams solrParams, ValueSource[] sources) {
		super(sources);
		this.findHouseUtil = new FindHouseUtil(solrParams);
		this.printLog = solrParams.getBool("printLog",false);
	}

	@Override
	protected float func(int doc, FunctionValues[] vals) {
		float score = 0;
		try {
			Map<String, String> map = SortUtil.getMap(vals[0].strVal(doc), this.printLog);
			//区域结果打分
			score += findHouseUtil.computerIdsScore(map,"regionIds");
			//商圈结果打分
			score += findHouseUtil.computerIdsScore(map,"businessIds");
			//地铁线结果打分
			score += findHouseUtil.computerIdsScore(map,"lineIds");
			//地铁站结果打分
			score += findHouseUtil.computerIdsScore(map,"stationIds");
			//户型结果打分
			score += findHouseUtil.computerCustomScore(map,"houseType",(check,params)->
					(params.equals("0") && Integer.parseInt(check) > 5) || check.equals(params));
			//面积结果打分
			score += findHouseUtil.computerCustomScore(map,"layoutArea",(check,params)->{
				String[] split = params.split("-");
				double aDouble = Double.parseDouble(check);
				return Integer.parseInt(split[0]) <= aDouble && aDouble <= Integer.parseInt(split[1]);
			});
			//买房目的结果打分
			score += findHouseUtil.computerBuyPurpose(map,"buyPurpose");
			//关注信息结果打分
			score += findHouseUtil.computerFocusHouse(map,"focusHouse");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return score;
	}

	@Override
	protected String name() {
		return "findnewhouse";
	}
}