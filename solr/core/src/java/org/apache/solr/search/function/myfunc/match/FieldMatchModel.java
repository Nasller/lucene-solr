package org.apache.solr.search.function.myfunc.match;

import java.util.function.BiPredicate;

public class FieldMatchModel {
	private String field;
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

	public enum MatchRuleType{
		CONTAINS((matchModel,value)->{
			String param = matchModel.getParam();
			return matchModel.isNotEqual() != (value instanceof String && ((String) value).contains(param));
		}),
		EQUAL((matchModel,value)->{
			String param = matchModel.getParam();
			return matchModel.isNotEqual() != (param.equals(value.toString()));
		}),
		RANGE((matchModel,value)->{
			boolean check = false;
			if(value instanceof Number){
				if(value instanceof Integer){
					int param = Integer.parseInt(matchModel.getParam());
					int start = Integer.parseInt(matchModel.getRangeStart());
					int end = Integer.parseInt(matchModel.getRangeEnd());
					if((start == param && matchModel.getRangeStartClosing()) || (end == param && matchModel.getRangeEndClosing())){
						check = true;
					}else if(start < param && param < end){
						check = true;
					}
				}else if(value instanceof Float){
					float param = Float.parseFloat(matchModel.getParam());
					float start = Float.parseFloat(matchModel.getRangeStart());
					float end = Float.parseFloat(matchModel.getRangeEnd());
					if((start == param && matchModel.getRangeStartClosing()) || (end == param && matchModel.getRangeEndClosing())){
						check = true;
					}else if(start < param && param < end){
						check = true;
					}
				}else if(value instanceof Double){
					double param = Double.parseDouble(matchModel.getParam());
					double start = Double.parseDouble(matchModel.getRangeStart());
					double end = Double.parseDouble(matchModel.getRangeEnd());
					if((start == param && matchModel.getRangeStartClosing()) || (end == param && matchModel.getRangeEndClosing())){
						check = true;
					}else if(start < param && param < end){
						check = true;
					}
				}
			}
			return matchModel.isNotEqual() != check;
		});

		private final BiPredicate<FieldMatchModel,Object> predicate;

		MatchRuleType(BiPredicate<FieldMatchModel, Object> predicate) {
			this.predicate = predicate;
		}

		public BiPredicate<FieldMatchModel, Object> getPredicate() {
			return predicate;
		}
	}
}