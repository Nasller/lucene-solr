package org.apache.solr.search.function.myfunc.room;

import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.ConstNumberSource;
import org.apache.lucene.queries.function.valuesource.LiteralValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.ValueSourceParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 房源权重排序
 * 
 * @author ChenYong
 */
public class RoomSortValueSourceParser extends ValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fp) {
		List<ValueSource> sources = null;
		int key = 0;
		List<RoomSortScore> scores = new ArrayList<>();
		Map<String, Map<String, Float>> extraSortMap = new HashMap<>();

		try {
			sources = fp.parseValueSourceList();


			ConstNumberSource keyParam = (ConstNumberSource) sources.get(10);
			LiteralValueSource scoreParam = (LiteralValueSource) sources.get(11);
			LiteralValueSource extraSortParam = (LiteralValueSource) sources.get(12);

			key = keyParam.getInt();
			String scoreStr = scoreParam.getValue();

			String[] scoreArr = scoreStr.split(",");
			for (String str : scoreArr) {
				String[] score = str.split("-");
				int startValue = Integer.parseInt(score[0]);
				int endValue = Integer.parseInt(score[1]);
				float scoreValue = Float.parseFloat(score[2]);
				scores.add(new RoomSortScore(startValue, endValue, scoreValue));
			}

			if (extraSortParam != null) {
				String extraSort = extraSortParam.getValue();
				String[] extraList = extraSort.split(",");
				if (extraList.length > 1) {
					extraSort = extraList[0];
				}

				Map<String, Float> gardenSortMap = fieldMatcher(extraSort, "gardenId");
				Map<String, Float> regionIdsSortMap = fieldMatcher(extraSort, "regionIds");
				Map<String, Float> businessIdsSortMap = fieldMatcher(extraSort, "businessIds");
				Map<String, Float> lineIdsSortMap = fieldMatcher(extraSort, "lineIds");
				Map<String, Float> stationIdsSortMap = fieldMatcher(extraSort, "stationIds");
				Map<String, Float> schoolNamesSortMap = fieldMatcher(extraSort, "schoolNames");
				Map<String, Float> layoutSortMap = fieldMatcher(extraSort, "layout");
				Map<String, Float> bedRoomSortMap = fieldMatcher(extraSort, "bedRoom");
				Map<String, Float> priceSortMap = fieldMatcher(extraSort, "price");
				Map<String, Float> areaSortMap = fieldMatcher(extraSort, "area");
				Map<String, Float> decorationSortMap = fieldMatcher(extraSort, "decoration");
				Map<String, Float> directionSortMap = fieldMatcher(extraSort, "direction");
				Map<String, Float> floorSortMap = fieldMatcher(extraSort, "floor");
				Map<String, Float> labelSortMap = fieldMatcher(extraSort, "label");
				Map<String, Float> titleSortMap = fieldMatcher(extraSort, "title");
				if (gardenSortMap != null && !gardenSortMap.isEmpty()) {
					extraSortMap.put("gardenId", gardenSortMap);
				}
				if (regionIdsSortMap != null && !regionIdsSortMap.isEmpty()) {
					extraSortMap.put("regionIds", regionIdsSortMap);
				}
				if (businessIdsSortMap != null && !businessIdsSortMap.isEmpty()) {
					extraSortMap.put("businessIds", businessIdsSortMap);
				}
				if (lineIdsSortMap != null && !lineIdsSortMap.isEmpty()) {
					extraSortMap.put("lineIds", lineIdsSortMap);
				}
				if (stationIdsSortMap != null && !stationIdsSortMap.isEmpty()) {
					extraSortMap.put("stationIds", stationIdsSortMap);
				}
				if (schoolNamesSortMap != null && !schoolNamesSortMap.isEmpty()) {
					extraSortMap.put("schoolNames", schoolNamesSortMap);
				}
				if (layoutSortMap != null && !layoutSortMap.isEmpty()) {
					extraSortMap.put("layout", layoutSortMap);
				}

				if (bedRoomSortMap != null && !bedRoomSortMap.isEmpty()) {
					extraSortMap.put("bedRoom", bedRoomSortMap);
				}
				if (priceSortMap != null && !priceSortMap.isEmpty()) {
					extraSortMap.put("price", priceSortMap);
				}
				if (areaSortMap != null && !areaSortMap.isEmpty()) {
					extraSortMap.put("area", areaSortMap);
				}

				if (decorationSortMap != null && !decorationSortMap.isEmpty()) {
					extraSortMap.put("decoration", decorationSortMap);
				}
				if (directionSortMap != null && !directionSortMap.isEmpty()) {
					extraSortMap.put("direction", directionSortMap);
				}
				if (floorSortMap != null && !floorSortMap.isEmpty()) {
					extraSortMap.put("floor", floorSortMap);
				}
				if (labelSortMap != null && !labelSortMap.isEmpty()) {
					extraSortMap.put("label", labelSortMap);
				}
				if (titleSortMap != null && !titleSortMap.isEmpty()) {
					extraSortMap.put("title", titleSortMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		ValueSource[] vs = sources.toArray(new ValueSource[sources.size()]);

		return new RoomSortFloatFunction(vs, key, scores, extraSortMap);
	}

	private Map<String, Float> fieldMatcher(String extraSort, String field) {
		if (extraSort == null  ||  field == null) {
			return null;
		}

		Pattern pattern = Pattern.compile(field + "\\s*:\\s*([A-Za-z0-9%\u4e00-\u9fa5]|-)+\\^[A-Za-z0-9]+");
		Matcher matcher = pattern.matcher(extraSort);
		Map<String, Float> resultMap = new HashMap<>();
		while (matcher.find()) {
			try {
				String text = matcher.group(0);
				String[] arr = text.split(":")[1].split("\\^");

				double d = Math.pow(2.0D, Double.parseDouble(arr[1].trim()));

				resultMap.put(arr[0].trim(), (float) d);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}

		return resultMap;
	}

}
