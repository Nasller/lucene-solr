package org.apache.solr.search.function.myfunc.newhouse;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.function.myfunc.OnlineMultiFloatFunction;
import org.apache.solr.search.function.myfunc.common.SortUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NewHouseSortFloatFunction extends OnlineMultiFloatFunction {
    public NewHouseSortFloatFunction(ValueSource[] sources) {
        super(sources);
    }

    @Override
    protected float func(int doc, FunctionValues[] vals) {
        try {
            long id = vals[0].longVal(doc);
            int ranking = vals[1].intVal(doc);
            String city = vals[2].strVal(doc);
            String requestCity = vals[4].strVal(doc);
            int cityCount = SortUtil.isNotBlank(requestCity) && requestCity.equals(city) ? 500 : 0;
            String ids = vals[3].strVal(doc);
            if(SortUtil.isNotBlank(ids)){
                List<Long> newhouseIds = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
                if (newhouseIds.contains(id)) {
                    return (200-newhouseIds.indexOf(id))*100;
                }
            }
            return ranking + cityCount;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected String name() {
        return "newHouseSort";
    }

}