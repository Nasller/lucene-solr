package org.apache.solr.search.function.myfunc.match;

import org.apache.solr.search.function.myfunc.common.SortUtil;

import java.util.Map;
import java.util.function.BiPredicate;

public class FieldMatchModel {
	private String field;
	private String sortFieldKey;
	private Boolean multiValue;
	private String param;
	private String rangeStart;
	private Boolean rangeStartClosing;
	private String rangeEnd;
	private Boolean rangeEndClosing;
	private boolean notEqual;
	private MatchRuleType matchRuleType;
	private int boost = 1;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		if(param.startsWith("\"") && param.endsWith("\"")){
			param = param.replaceAll("\"","");
			matchRuleType = MatchRuleType.CONTAINS;
		}else if(param.contains("TO")){
			if(param.startsWith("[")) this.rangeStartClosing = true;
			else this.rangeStartClosing = false;
			if(param.startsWith("]")) this.rangeEndClosing = true;
			else this.rangeEndClosing = false;
			String[] replace = param.replaceAll("[\\[\\]]","").split(" TO ");
			this.rangeStart = replace[0];
			this.rangeEnd = replace[1];
			matchRuleType = MatchRuleType.RANGE;
		}else matchRuleType = MatchRuleType.EQUAL;
		this.param = param;
	}

	public int getBoost() {
		return boost;
	}

	public void setBoost(int boost) {
		this.boost = boost;
	}

	public String getRangeStart() {
		return rangeStart;
	}

	public String getRangeEnd() {
		return rangeEnd;
	}

	public boolean isNotEqual() {
		return notEqual;
	}

	public void setNotEqual(boolean notEqual) {
		this.notEqual = notEqual;
	}

	public Boolean getRangeStartClosing() {
		return rangeStartClosing;
	}

	public Boolean getRangeEndClosing() {
		return rangeEndClosing;
	}

	public MatchRuleType getMatchRuleType() {
		return matchRuleType;
	}

	public void setSortFieldKey(String sortFieldKey) {
		if(sortFieldKey.endsWith(FieldMatchValueSourceParser.SORT_FIELD_SUFFIX)) {
			multiValue = true;
			sortFieldKey = sortFieldKey.replace(FieldMatchValueSourceParser.SORT_FIELD_SUFFIX,"");
		}else multiValue = false;
		this.sortFieldKey = sortFieldKey;
	}

	public String getSortFieldKey() {
		return sortFieldKey;
	}

	public Boolean getMultiValue() {
		return multiValue;
	}

	public enum MatchRuleType {
		CONTAINS((matchModel,value)->{
			String param = matchModel.getParam();
			boolean check = false;
			if(value instanceof Map){
				String sortValue = ((Map<String,String>) value).get(matchModel.getSortFieldKey());
				if(SortUtil.isNotBlank(sortValue)){
					if(matchModel.getMultiValue()){
						for (String match : sortValue.split("\\|\\|\\|")) {
							check = match.contains(param);
							if(check) break;
						}
					}else check = sortValue.contains(param);
				}
			} else check = value instanceof String && ((String) value).contains(param);
			return matchModel.isNotEqual() != check;
		}),
		EQUAL((matchModel,value)->{
			String param = matchModel.getParam();
			boolean check = false;
			if(value instanceof Map){
				String sortValue = ((Map<String,String>) value).get(matchModel.getSortFieldKey());
				if(SortUtil.isNotBlank(sortValue)){
					if(matchModel.getMultiValue()){
						for (String match : sortValue.split("\\|\\|\\|")) {
							check = param.equals(match);
							if(check) break;
						}
					} else check = param.equals(sortValue);
				}
			}else check = param.equals(value.toString());
			return matchModel.isNotEqual() != check;
		}),
		RANGE((matchModel,value)->{
			boolean check = false;
			if(value instanceof Map){
				String sortValue = ((Map<String,String>) value).get(matchModel.getSortFieldKey());
				if(SortUtil.isNotBlank(sortValue)){
					if(matchModel.getMultiValue()){
						for (String match : sortValue.split("\\|\\|\\|")) {
							check = matchRangeNumber(matchModel, Integer.parseInt(match));
							if(check) break;
						}
					} else check = matchRangeNumber(matchModel, Integer.parseInt(sortValue));
				}
			}else{
				check = matchRangeNumber(matchModel, value);
			}
			return matchModel.isNotEqual() != check;
		});

		private static boolean matchRangeNumber(FieldMatchModel matchModel, Object value) {
			if(value instanceof Number){
				if(value instanceof Integer){
					int param = (Integer) value;
					int start = Integer.parseInt(matchModel.getRangeStart());
					int end = Integer.parseInt(matchModel.getRangeEnd());
					if((start == param && matchModel.getRangeStartClosing()) || (end == param && matchModel.getRangeEndClosing())){
						return true;
					}else return start < param && param < end;
				}else if(value instanceof Float){
					float param = (Float) value;
					float start = Float.parseFloat(matchModel.getRangeStart());
					float end = Float.parseFloat(matchModel.getRangeEnd());
					if((start == param && matchModel.getRangeStartClosing()) || (end == param && matchModel.getRangeEndClosing())){
						return true;
					}else return start < param && param < end;
				}else if(value instanceof Double){
					double param = (Double) value;
					double start = Double.parseDouble(matchModel.getRangeStart());
					double end = Double.parseDouble(matchModel.getRangeEnd());
					if((start == param && matchModel.getRangeStartClosing()) || (end == param && matchModel.getRangeEndClosing())){
						return true;
					}else return start < param && param < end;
				}
			}
			return false;
		}

		private final BiPredicate<FieldMatchModel,Object> predicate;

		MatchRuleType(BiPredicate<FieldMatchModel, Object> predicate) {
			this.predicate = predicate;
		}

		public BiPredicate<FieldMatchModel, Object> getPredicate() {
			return predicate;
		}
	}
}