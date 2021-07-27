package org.apache.solr.search.function.myfunc.zufang;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZuFangSortFloatFunction extends MultiFloatFunction {
	private final List<String> list;
	public ZuFangSortFloatFunction(ValueSource[] sources, List<String> list) {
		super(sources);
		this.list = list;
	}

	@Override
	protected float func(int doc, FunctionValues[] vals) {
		try {
			if(CollectionUtils.isNotEmpty(list)){
				List<String> local = new ArrayList<>(list);
				String allKeyword = local.remove(0);
				String keyword = Stream.of(vals[0].strVal(doc),vals[1].strVal(doc),vals[2].strVal(doc),vals[3].strVal(doc))
						.filter(StringUtils::isNotBlank).collect(Collectors.joining());
				if(keyword.contains(allKeyword)){
					return 100;
				}
				for (String text : local) {
					if(keyword.contains(text)){
						return 50;
					}
				}
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	protected String name() {
		return "zufangsort";
	}

}
