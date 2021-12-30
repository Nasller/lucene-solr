package org.apache.solr.search.function.myfunc.common;

public enum BuyHousePurposeEnum {
	NEED("刚需"),
	MARRY("结婚"),
	OLD("养老"),
	IMPROVE("改善"),
	EDUCATION("教育"),
	INVESTMENT("投资"),
	;
	private final String desc;

	BuyHousePurposeEnum(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}