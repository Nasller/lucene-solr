package org.apache.solr.search.function.myfunc.room;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;
import org.apache.solr.search.function.myfunc.common.SortUtil;

import java.util.List;
import java.util.Map;

/**
 * 房源权重排序
 * 
 * @author ChenYong
 */
public class RoomSortFloatFunction extends MultiFloatFunction {
	private final int key;
	private final List<RoomSortScore> scores;
	private final boolean printLog;

	private final Map<String, Map<String, Float>> extraSortMap;

	private static final float roomRankMaxScore = 1000000.0F;

	public RoomSortFloatFunction(ValueSource[] sources, int key, List<RoomSortScore> scores,
			Map<String, Map<String, Float>> extraSortMap) {
		super(sources);
		this.key = key;
		this.scores = scores;
		this.extraSortMap = extraSortMap;
		this.printLog = false;
	}

	/**
	 * 1、图片排序 有图 500000
	 * 3、权重区间排序 权重大于60 50000 权重50--60 40000  权重40--50 30000  权重30--40 20000  权重小于30 10000
	 * 4、随机排序权重 小于10000
	 * 5、搜索权重排序
	 * 
	 * @return 1+2+3+4+5+6+7
	 */
	protected float func(int doc, FunctionValues[] vals) {
		try {
			String idStr = vals[0].strVal(doc);
			int id = SortUtil.getId(idStr);

			int ranking = vals[1].intVal(doc);

			String sortField = vals[9].strVal(doc);

			//图片权重
			float pictureRanking=0;
			if (vals.length > 13) {
				String pictureUrl = vals[13].strVal(doc);
				if(pictureUrl != null && pictureUrl.trim().length() > 0){
					pictureRanking=500000;
				}
			}

			// 房源权重
			float scoreRanking = 0;
			if (this.scores != null && this.scores.size() > 0) {
				scoreRanking = RoomSortScore.getScore(this.scores, ranking);
			}

			// 随机权重
			float randomRanking = 0;

			// 额外排序条件权重
			float extraSortRanking = 0;

			if (this.extraSortMap != null && !this.extraSortMap.isEmpty()) {
				Map<String, Float> gardenSortMap = this.extraSortMap.get("gardenId");
				// 位置词:区域/商圈/地铁线路/地铁站点/学校 只需满足一个，计算一次分数
				Map<String, Float> regionIdsSortMap = this.extraSortMap.get("regionIds");
				Map<String, Float> businessIdsSortMap = this.extraSortMap.get("businessIds");
				Map<String, Float> lineIdsSortMap = this.extraSortMap.get("lineIds");
				Map<String, Float> stationIdsSortMap = this.extraSortMap.get("stationIds");
				Map<String, Float> schoolSortMap = this.extraSortMap.get("schoolNames");

				Map<String, Float> bedRoomSortMap = this.extraSortMap.get("bedRoom");
				Map<String, Float> priceSortMap = this.extraSortMap.get("price");
				Map<String, Float> areaSortMap = this.extraSortMap.get("area");
				Map<String, Float> decorationSortMap = this.extraSortMap.get("decoration");
				Map<String, Float> directionSortMap = this.extraSortMap.get("direction");
				Map<String, Float> floorSortMap = this.extraSortMap.get("floor");
				Map<String, Float> labelSortMap = this.extraSortMap.get("label");
				Map<String, Float> titleSortMap = this.extraSortMap.get("title");
				// 精准小区优先排序（有社区）
				Map<String, String> map = SortUtil.getMap(sortField, this.printLog);


				if (gardenSortMap != null && !gardenSortMap.isEmpty()) {
					int gardenId = vals[3].intVal(doc);

					String gardenIdStr = String.valueOf(gardenId);
					if (gardenSortMap.containsKey(gardenIdStr)) {
						Float f = gardenSortMap.get(gardenIdStr);
						if (f != null && f > 0.0F) {
							extraSortRanking = extraSortRanking + roomRankMaxScore * f + 5.12E10F;
							if (this.printLog) {
								System.out.println("gardenId:" + extraSortRanking);
							}
						}
					}
				}

				boolean dist = false;

				if (regionIdsSortMap != null && !regionIdsSortMap.isEmpty()) {
					if (map != null && !map.isEmpty()) {
						String regionIds = map.get("regionIds");
						if (StringUtils.isNotBlank(regionIds)) {
							for (String regionId : regionIds.split(",")) {
								if (regionIdsSortMap.containsKey(regionId)) {
									Float f = regionIdsSortMap.get(regionId);
									if (f != null && f > 0.0F) {
										extraSortRanking += roomRankMaxScore * f;
										dist = true;
										if (this.printLog) {
											System.out.println("regionIds:" + extraSortRanking);
										}

										break;
									}
								}
							}
						}
					}
				}

				if (businessIdsSortMap != null && !businessIdsSortMap.isEmpty() && !dist && map != null && !map.isEmpty()) {
					String businessIds = map.get("businessIds");
					if (StringUtils.isNotBlank(businessIds)) {
						for (String businessId : businessIds.split(",")) {
							if (businessIdsSortMap.containsKey(businessId)) {
								Float f = businessIdsSortMap.get(businessId);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									dist = true;
									if (this.printLog) {
										System.out.println("businessIds:" + extraSortRanking);
									}

									break;
								}
							}
						}
					}
				}


				if (lineIdsSortMap != null && !lineIdsSortMap.isEmpty() && !dist) {
					if (map != null && !map.isEmpty()) {
						String lineIds = map.get("lineIds");
						if (StringUtils.isNotBlank(lineIds)) {
							for (String lineId : lineIds.split(",")) {
								if (lineIdsSortMap.containsKey(lineId)) {
									Float f = lineIdsSortMap.get(lineId);
									if (f != null && f > 0.0F) {
										extraSortRanking += roomRankMaxScore * f;
										dist = true;
										if (this.printLog) {
											System.out.println("lineIds:" + extraSortRanking);
										}

										break;
									}
								}
							}
						}
					}
				}

				if (stationIdsSortMap != null && !stationIdsSortMap.isEmpty() && !dist && map != null && !map.isEmpty()) {
					String stationIds = map.get("stationIds");
					if (StringUtils.isNotBlank(stationIds)) {
						for (String stationId : stationIds.split(",")) {
							if (stationIdsSortMap.containsKey(stationId)) {
								Float f = stationIdsSortMap.get(stationId);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									dist = true;
									if (this.printLog) {
										System.out.println("stationIds:" +extraSortRanking);
									}

									break;
								}
							}
						}
					}
				}

				if (schoolSortMap != null && !schoolSortMap.isEmpty() && !dist && map != null && !map.isEmpty()) {
					String schoolNames = map.get("schoolNames");
					if (StringUtils.isNotBlank(schoolNames)) {
						for (String schoolName : schoolNames.split(",")) {
							if (schoolSortMap.containsKey(schoolName)) {
								Float f = schoolSortMap.get(schoolName);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("schoolNames:" +extraSortRanking);
									}

									break;
								}
							}
						}
					}
				}

				if (bedRoomSortMap != null && !bedRoomSortMap.isEmpty()) {
					int bedRoom  = vals[4].intVal(doc);

					for (String key : bedRoomSortMap.keySet()) {
						try {
							String[] keys = key.split("-");
							int start = Integer.parseInt(keys[0]);
							int end = Integer.parseInt(keys[1]);
							if (bedRoom >= start && bedRoom <= end) {
								Float f = bedRoomSortMap.get(key);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("bedRoom:" + extraSortRanking);
									}
									break;
								}
							}
						} catch (Exception exception) {
							//
						}
					}
				}

				if (priceSortMap != null && !priceSortMap.isEmpty()) {
					double price = vals[5].doubleVal(doc);

					for (String key : priceSortMap.keySet()) {
						try {
							String[] keys = key.split("-");
							int start = Integer.parseInt(keys[0]);
							int end = Integer.parseInt(keys[1]);
							if (price >= start && price <= end) {
								Float f = priceSortMap.get(key);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("price:" + extraSortRanking);
									}
									break;
								}
							}
						} catch (Exception exception) {
							//
						}
					}
				}

				if (areaSortMap != null && !areaSortMap.isEmpty()) {
					double area = vals[5].doubleVal(doc);

					for (String key : areaSortMap.keySet()) {
						try {
							String[] keys = key.split("-");
							int start = Integer.parseInt(keys[0]);
							int end = Integer.parseInt(keys[1]);
							if (area >= start && area <= end) {
								Float f = areaSortMap.get(key);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("area:" + extraSortRanking);
									}
									break;
								}
							}
						} catch (Exception exception) {
							//
						}
					}
				}

				if (decorationSortMap != null && !decorationSortMap.isEmpty()) {
					String decoration = vals[6].strVal(doc);

					if (decorationSortMap.containsKey(decoration)) {
						Float f = decorationSortMap.get(decoration);
						if (f != null && f > 0.0F) {
							extraSortRanking += roomRankMaxScore * f;
							if (this.printLog) {
								System.out.println("decoration:" + extraSortRanking);
							}
						}
					}
				}

				if (directionSortMap != null && !directionSortMap.isEmpty() && map != null && !map.isEmpty()) {
					String direction = map.get("direction");
					if (directionSortMap.containsKey(direction)) {
						Float f = directionSortMap.get(direction);
						if (f != null && f > 0.0F) {
							extraSortRanking += roomRankMaxScore * f;
							if (this.printLog) {
								System.out.println("direction:" + extraSortRanking);
							}
						}
					}
				}

				if (floorSortMap != null && !floorSortMap.isEmpty()) {
					int floor = vals[8].intVal(doc);

					for (String key : floorSortMap.keySet()) {
						try {
							String[] keys = key.split("-");
							int start = Integer.parseInt(keys[0]);
							int end = Integer.parseInt(keys[1]);
							if (floor >= start && floor <= end) {
								Float f = floorSortMap.get(key);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("floor:" + extraSortRanking);
									}
									break;
								}
							}
						} catch (Exception exception) {
							//
						}
					}
				}

				if (labelSortMap != null && !labelSortMap.isEmpty() && map != null && !map.isEmpty()) {
					String labels = map.get("label");
					if (StringUtils.isNotBlank(labels) && StringUtils.isNotBlank(labels)) {
						for (String label : labels.split("\\|")) {
							if (labelSortMap.containsKey(label)) {
								Float f = labelSortMap.get(label);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("label:" + extraSortRanking);
									}

									break;
								}
							}
						}
					}
				}

				if (titleSortMap != null && !titleSortMap.isEmpty() && map != null && !map.isEmpty()) {
					String title = map.get("title");
					if (StringUtils.isNotBlank(title)) {
						for (String key : titleSortMap.keySet()) {
							if (title.contains(key)) {
								Float f = titleSortMap.get(key);
								if (f != null && f > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("title:" + extraSortRanking);
									}

									break;
								}
							}
						}
					}
				}

			}

			if (id > 0) {
				randomRanking = SortUtil.getRandomRanking(this.key, id);
			}
			if (this.printLog) {
				System.out.println("id:" + id
						+ ",pictureRanking:" + pictureRanking
						+ ",scoreRanking:"+ scoreRanking
						+ ",extraSortRanking:"+ extraSortRanking
						+ ",randomRanking:"+ randomRanking);
			}
			
			return pictureRanking + scoreRanking + randomRanking + extraSortRanking;
		} catch (Exception e) {
			if (this.printLog) {
				e.printStackTrace();
			}

			return 0.0F;
		}
	}

	protected String name() {
		return "roomsort";
	}

}