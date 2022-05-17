package org.apache.solr.search.function.myfunc.newhouse;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.solr.search.function.myfunc.OnlineMultiFloatFunction;
import org.apache.solr.search.function.myfunc.common.SortUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class NewHouseSortFloatFunction extends OnlineMultiFloatFunction {
    private final ThreadLocalRandom random;
    public NewHouseSortFloatFunction(ValueSource[] sources) {
        super(sources);
        random = ThreadLocalRandom.current();
    }

    @Override
    protected float func(int doc, FunctionValues[] vals) {
        try {
            long id = vals[0].longVal(doc);
            int ranking = vals[1].intVal(doc);
            String city = vals[2].strVal(doc);
            String ids = vals[3].strVal(doc);
            String requestCity = vals[4].strVal(doc);
            boolean isOriginCity = SortUtil.isNotBlank(requestCity) && city.equals(requestCity);
            int kfangCount = vals[5].intVal(doc);
            int spiderCount = vals[6].intVal(doc);
            int gap = 1000;
            if(SortUtil.isNotBlank(ids)){
                List<Long> newhouseIds = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
                if (newhouseIds.contains(id)) {
                    return (200-newhouseIds.indexOf(id))*100;
                }
            }
            if(isOriginCity) return ranking + gap;
            else if(ranking >= kfangCount) return random.nextInt(kfangCount,kfangCount + gap);
            else if(ranking >= spiderCount) return random.nextInt(spiderCount,spiderCount + gap);
            else return random.nextInt(0,gap);
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