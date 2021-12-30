package org.apache.solr.search.function.myfunc.common;

public enum FocusHouseEnum {
	DECORATION("精装"),
	HOSPITAL("医院配套"),
	SCHOOL("学校配套"),
	FOOD_SHOP("餐饮购物"),
	SUBWAY("交通便利"),
	PARKING_CHARGE("车位充足"),
	NORTH_SOUTH("南北通透"),
	;
	private final String desc;

	FocusHouseEnum(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}
}