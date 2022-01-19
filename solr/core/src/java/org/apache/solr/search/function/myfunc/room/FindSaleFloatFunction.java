package org.apache.solr.search.function.myfunc.room;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.search.function.myfunc.common.FindHouseUtil;
import org.apache.solr.search.function.myfunc.common.SortUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindSaleFloatFunction extends MultiFloatFunction {
    private final FindHouseUtil findHouseUtil;
    private final boolean printLog;

    public FindSaleFloatFunction(SolrParams solrParams, ValueSource[] sources) {
        super(sources);
        this.findHouseUtil = new FindHouseUtil(solrParams);
        this.printLog = solrParams.getBool("printLog", false);
    }

    @Override
    protected float func(int doc, FunctionValues[] vals) {
        float score = 0;
        try {
            String id = vals[0].strVal(doc);
            String direction = vals[1].strVal(doc);
            Integer bedRoom = vals[2].intVal(doc);
            String subwayDesc = vals[3].strVal(doc);
            String elevator = vals[4].strVal(doc);
            String decoration = vals[5].strVal(doc);
            Double buildingArea = vals[6].doubleVal(doc);
            Map<String, String> sortFieldMap = SortUtil.getMap(vals[7].strVal(doc), this.printLog);

            List<String> features = new ArrayList<>();
            if (StringUtils.isNotBlank(subwayDesc)) {
                features.add("ALONG_LINE");
            }
            if (StringUtils.isNotBlank(decoration)) {
                features.add(decoration);
            }
            Map<String, String> map = new HashMap<>();
            map.put("regionIds", sortFieldMap.get("regionIds"));
            map.put("businessIds", sortFieldMap.get("businessIds"));
            map.put("lineIds", sortFieldMap.get("lineIds"));
            map.put("stationIds", sortFieldMap.get("stationIds"));
            map.put("houseType", String.valueOf(bedRoom));
            map.put("layoutArea", String.valueOf(buildingArea));

            map.put("features", StringUtils.join(features, ","));
            map.put("surroundType", sortFieldMap.get("gardenSurrounds"));
            map.put("propertyTypeUses", sortFieldMap.get("propertyTypeUses"));
            map.put("elevator", "HAS_ELEVATOR".equals(elevator) ? "YES" : "NO");
            map.put("direction", StringUtils.upperCase(direction));

            //区域结果打分
            score += findHouseUtil.computerIdsScore(map, "regionIds");
            //商圈结果打分
            score += findHouseUtil.computerIdsScore(map, "businessIds");
            //地铁线结果打分
            score += findHouseUtil.computerIdsScore(map, "lineIds");
            //地铁站结果打分
            score += findHouseUtil.computerIdsScore(map, "stationIds");
            //户型结果打分
            score += findHouseUtil.computerCustomScore(map, "houseType", (check, params) ->
                    ("0".equals(params) && Integer.parseInt(check) > 5) || check.equals(params));
            //面积结果打分
            score += findHouseUtil.computerCustomScore(map, "layoutArea", (check, params) -> {
                String[] split = params.split("-");
                double aDouble = Double.parseDouble(check);
                return Integer.parseInt(split[0]) < aDouble && aDouble <= Integer.parseInt(split[1]);
            });
            //买房目的结果打分
            score += findHouseUtil.computerBuyPurpose(map, "buyPurpose");
            //关注信息结果打分
            score += findHouseUtil.computerFocusHouse(map, "focusHouse");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
    }

    @Override
    protected String name() {
        return "findSaleSort";
    }
}