package org.apache.solr.search.function.myfunc.room;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.ConstNumberSource;
import org.apache.lucene.queries.function.valuesource.LiteralValueSource;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoomSortValueSourceParser extends ValueSourceParser {
	
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		List<ValueSource> sources = null;
		SolrIndexSearcher searcher = null;
		int key = 0;
		List<RoomSortScore> scores = new ArrayList<RoomSortScore>();
		Map<String, Map<String, Float>> extraSortMap = new HashMap<String, Map<String, Float>>();
		boolean isTopSellRoom = false;
		Map<String, Object> userLabelMap = new HashMap<String, Object>();
		try {
			sources = fp.parseValueSourceList();
			searcher = fp.getReq().getSearcher();

			ConstNumberSource keyParam = null;
			LiteralValueSource scoreParam = null;
			LiteralValueSource extraSortParam = null;
			LiteralValueSource userLabel = null;

			keyParam = (ConstNumberSource) sources.get(10);
			scoreParam = (LiteralValueSource) sources.get(11);
			extraSortParam = (LiteralValueSource) sources.get(12);
			ConstNumberSource topParam = (ConstNumberSource) sources.get(13);
			isTopSellRoom = (topParam.getInt() == 1);
			if (sources.size() >= 16) {
				userLabel = (LiteralValueSource) sources.get(15);

				String userLabelParam = userLabel.getValue();
				getUserLabelMap(userLabelParam, userLabelMap);
			}
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
					if ("communitysort".equals(extraList[1])) {
						extraSortMap.put("communitySort", new HashMap<String, Float>());
					}
				}

				Map<String, Float> gardenSortMap = fieldMatcher(extraSort, "gardenId");
				Map<String, Float> parentAreaIdsSortMap = fieldMatcher(extraSort, "parentAreaIds");
				Map<String, Float> businessIdsSortMap = fieldMatcher(extraSort, "businessIds");
				Map<String, Float> officeBusinessIdsSortMap = fieldMatcher(extraSort, "officeBusinessIds");
				Map<String, Float> lineSortMap = fieldMatcher(extraSort, "subwayLine");
				Map<String, Float> stationSortMap = fieldMatcher(extraSort, "subwayStation");
				Map<String, Float> schoolSortMap = fieldMatcher(extraSort, "schoolNames");
				Map<String, Float> layoutSortMap = fieldMatcher(extraSort, "layout");
				Map<String, Float> bedRoomSortMap = fieldMatcher(extraSort, "bedRoom");
				Map<String, Float> priceSortMap = fieldMatcher(extraSort, "price");
				Map<String, Float> areaSortMap = fieldMatcher(extraSort, "area");
				Map<String, Float> decorationSortMap = fieldMatcher(extraSort, "decoration");
				Map<String, Float> directionSortMap = fieldMatcher(extraSort, "direction");
				Map<String, Float> floorSortMap = fieldMatcher(extraSort, "floor");
				Map<String, Float> labelDescSortMap = fieldMatcher(extraSort, "labelDesc");
				Map<String, Float> titleSortMap = fieldMatcher(extraSort, "title");
				if (gardenSortMap != null && !gardenSortMap.isEmpty()) {
					extraSortMap.put("gardenId", gardenSortMap);
				}
				if (parentAreaIdsSortMap != null && !parentAreaIdsSortMap.isEmpty()) {
					extraSortMap.put("parentAreaIds", parentAreaIdsSortMap);
				}
				if (businessIdsSortMap != null && !businessIdsSortMap.isEmpty()) {
					extraSortMap.put("businessIds", businessIdsSortMap);
				}
				if (officeBusinessIdsSortMap != null && !officeBusinessIdsSortMap.isEmpty()) {
					extraSortMap.put("officeBusinessIds", officeBusinessIdsSortMap);
				}
				if (lineSortMap != null && !lineSortMap.isEmpty()) {
					extraSortMap.put("subwayLine", lineSortMap);
				}
				if (stationSortMap != null && !stationSortMap.isEmpty()) {
					extraSortMap.put("subwayStation", stationSortMap);
				}
				if (schoolSortMap != null && !schoolSortMap.isEmpty()) {
					extraSortMap.put("schoolNames", schoolSortMap);
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
				if (labelDescSortMap != null && !labelDescSortMap.isEmpty()) {
					extraSortMap.put("labelDesc", labelDescSortMap);
				}
				if (titleSortMap != null && !titleSortMap.isEmpty()) {
					extraSortMap.put("title", titleSortMap);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		ValueSource[] vs = sources.toArray(new ValueSource[sources.size()]);
		return new RoomSortFloatFunction(vs, key, scores, extraSortMap, searcher, isTopSellRoom,
				userLabelMap);
	}

	private Map<String, Float> fieldMatcher(String extraSort, String field) {
		if (StringUtils.isBlank(extraSort) || StringUtils.isBlank(field)) {
			return null;
		}

		Pattern pattern = Pattern.compile(field + "\\s*:\\s*([A-Za-z0-9%\u4e00-\u9fa5]|-)+\\^[A-Za-z0-9]+");
		Matcher matcher = pattern.matcher(extraSort);
		Map<String, Float> resultMap = new HashMap<String, Float>();
		while (matcher.find()) {
			try {
				String text = matcher.group(0);
				String[] arr = text.split(":")[1].split("\\^");

				Double d = Double.valueOf(Math.pow(2.0D, Double.parseDouble(arr[1].trim())));

				resultMap.put(arr[0].trim(), Float.valueOf(d.floatValue()));
			} catch (Exception exception) {

			}
		}

		return resultMap;
	}

	private void getUserLabelMap(String userLabelStr, Map<String, Object> userLabelMap) {
		if (StringUtils.isBlank(userLabelStr))
			return;
		String[] item = userLabelStr.split("&");
		if (item == null || item.length != 5)
			return;
		if (StringUtils.isNotBlank(item[0]))
			userLabelMap.put("businessIds", Arrays.asList(item[0].split(",")));
		if (StringUtils.isNotBlank(item[1]))
			userLabelMap.put("garenIds", Arrays.asList(item[1].split(",")));
		if (StringUtils.isNotBlank(item[2]) )
			userLabelMap.put("prices", item[2]);
		if (StringUtils.isNotBlank(item[3]))
			userLabelMap.put("layouts", Arrays.asList(item[3].split(",")));
		if (StringUtils.isNotBlank(item[4]))
			userLabelMap.put("closeMetro", item[4]);
	}
}
