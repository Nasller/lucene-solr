package org.apache.solr.search.function.myfunc.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.SolrParams;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;

public class FindHouseUtil {
	private final SolrParams solrParams;

	public FindHouseUtil(SolrParams solrParams) {
		this.solrParams = solrParams;
	}

	public int computerIdsScore(Map<String, String> map, String paramName){
		return computerCustomScore(map,paramName, String::equals);
	}

	public int computerCustomScore(Map<String, String> map, String paramName, BiPredicate<String,String> predicate){
		int score = 0;
		String params = solrParams.get(paramName);
		if(StringUtils.isNotBlank(params)){
			String params1 = map.get(paramName);
			if(StringUtils.isNotBlank(params1)){
				String[] list = params1.split(",");
				for (String thing : params.split(",")) {
					for (String check : list) {
						if(predicate.test(check,thing)){
							score += 1;
							break;
						}
					}
				}
			}
		}
		return score;
	}

	/**
	 * 买房目的
	 */
	public int computerBuyPurpose(Map<String, String> map,String purposeString){
		int score = 0;
		String params = solrParams.get(purposeString);
		if(StringUtils.isNotBlank(params)){
			String features = map.get("features");
			String surroundType = map.get("surroundType");
			String propertyTypeUses = map.get("propertyTypeUses");
			String houseType = map.get("houseType");
			String elevator = map.get("elevator");
			switch (BuyHousePurposeEnum.valueOf(params)) {
				case NEED:
					//房屋用途为住宅
					//二房以上（含）
					if(StringUtils.isNotBlank(propertyTypeUses) && propertyTypeUses.contains("RESIDENCE") &&
							StringUtils.isNotBlank(houseType) && Arrays.stream(houseType.split(",")).anyMatch(o->Integer.parseInt(o) >= 2))
						score += 1;
					break;
				case MARRY:
					//房屋用途为住宅
					//二房以上（含）
					//周边配套满足：地铁（或公交）、医院、学校
					if(StringUtils.isNotBlank(propertyTypeUses) && propertyTypeUses.contains("RESIDENCE") &&
							StringUtils.isNotBlank(houseType) && Arrays.stream(houseType.split(",")).anyMatch(o->Integer.parseInt(o) >= 2) &&
							StringUtils.isNotBlank(surroundType) && surroundType.contains("HOSPITAL") && surroundType.contains("SCHOOL") &&
							(surroundType.contains("SUBWAY") || surroundType.contains("BUS")))
						score += 1;
					break;
				case OLD:
					//房屋用途为住宅
					//二房以上（含）
					//周边配套满足：地铁（或公交）、医院
					if(StringUtils.isNotBlank(propertyTypeUses) && propertyTypeUses.contains("RESIDENCE") &&
							StringUtils.isNotBlank(houseType) && Arrays.stream(houseType.split(",")).anyMatch(o->Integer.parseInt(o) >= 2) &&
							StringUtils.isNotBlank(surroundType) && surroundType.contains("HOSPITAL") &&
							(surroundType.contains("SUBWAY") || surroundType.contains("BUS")))
						score += 1;
					break;
				case IMPROVE:
					//近地铁
					//有电梯
					//户型大于等于三房的住宅
					//周边配套满足：医院、学校
					if(StringUtils.isNotBlank(features) && features.contains("ALONG_LINE") &&
							StringUtils.isNotBlank(elevator) && ("YES".equals(elevator) || "PART".equals(elevator)) &&
							StringUtils.isNotBlank(houseType) && Arrays.stream(houseType.split(",")).anyMatch(o->Integer.parseInt(o) >= 3) &&
							StringUtils.isNotBlank(surroundType) && surroundType.contains("HOSPITAL") && surroundType.contains("SCHOOL"))
						score += 1;
					break;
				case EDUCATION:
					//周边配套满足：学校
					if(StringUtils.isNotBlank(surroundType) && surroundType.contains("SCHOOL"))
						score += 1;
					break;
				case INVESTMENT:
					//周边配套满足：地铁（或公交）、医院、学校
					if(StringUtils.isNotBlank(surroundType) && surroundType.contains("HOSPITAL") && surroundType.contains("SCHOOL") &&
							(surroundType.contains("SUBWAY") || surroundType.contains("BUS")))
						score += 1;
					break;
			}
		}
		return score;
	}

	/**
	 * 关注信息
	 */
	public int computerFocusHouse(Map<String, String> map,String focusString){
		int score = 0;
		String params = solrParams.get(focusString);
		if(StringUtils.isNotBlank(params)){
			String features = map.get("features");
			String surroundType = map.get("surroundType");
			String direction = map.get("direction");
			for (String string : params.split(",")) {
				switch (FocusHouseEnum.valueOf(string)){
					case DECORATION:
						if(StringUtils.isNotBlank(features) && features.contains("DECORATION")) score += 1;
						break;
					case HOSPITAL:
						if(StringUtils.isNotBlank(surroundType) && surroundType.contains("HOSPITAL")) score += 1;
						break;
					case SCHOOL:
						if(StringUtils.isNotBlank(surroundType) && surroundType.contains("SCHOOL")) score += 1;
						break;
					case FOOD_SHOP:
						if(StringUtils.isNotBlank(surroundType) && surroundType.contains("FOOD") && surroundType.contains("SHOP")) score += 1;
						break;
					case SUBWAY:
						if(StringUtils.isNotBlank(features) && features.contains("ALONG_LINE")) score += 1;
						break;
					case PARKING_CHARGE:
						if(StringUtils.isNotBlank(features) && features.contains("PARKING_CHARGE")) score += 1;
						break;
					case NORTH_SOUTH:
						if(StringUtils.isNotBlank(direction) && direction.contains("NORTHSOUTH")) score += 1;
						break;
				}
			}
		}
		return score;
	}
}