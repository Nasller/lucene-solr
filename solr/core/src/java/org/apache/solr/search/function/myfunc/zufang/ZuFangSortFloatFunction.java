package org.apache.solr.search.function.myfunc.zufang;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZuFangSortFloatFunction extends MultiFloatFunction {
	public ZuFangSortFloatFunction(ValueSource[] sources) {
		super(sources);
	}

	@Override
	protected float func(int doc, FunctionValues[] vals) {
		try {
			String keyword = Stream.of(vals[0].strVal(doc),vals[1].strVal(doc),vals[2].strVal(doc),vals[3].strVal(doc))
					.filter(StringUtils::isNotBlank).collect(Collectors.joining());
			List<String> keywords = Stream.of(vals).skip(4).map(o-> {
				try {
					return o.strVal(doc);
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}).filter(StringUtils::isNotBlank).collect(Collectors.toList());
			if(CollectionUtils.isNotEmpty(keywords)){
				String allKeyword = keywords.remove(0);
				if(keyword.contains(allKeyword)){
					return 100;
				}
				for (String text : keywords) {
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
