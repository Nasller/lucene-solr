package org.apache.solr.search.function.myfunc.room;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.search.function.myfunc.SortUtil;

import java.text.SimpleDateFormat;
import java.util.*;

public class RoomSortFloatFunction extends MultiFloatFunction {
	private int key;
	private List<RoomSortScore> scores = null;

	private Map<String, Map<String, Float>> extraSortMap;
	private static List<Map<String,String>> floats = new LinkedList<>();

	private boolean printLog;
	private boolean isTopSellRoom;
	private static final float roomRankMaxScore = 2000000.0F;
	private Date now;
	private Map<String, Object> userLabelMap;

	public RoomSortFloatFunction(ValueSource[] sources, int key, List<RoomSortScore> scores,
                                 Map<String, Map<String, Float>> extraSortMap, SolrIndexSearcher searcher, boolean isTopSellRoom,
                                 Map<String, Object> userLabelMap) {
		super(sources);
		this.key = key;
		this.scores = scores;
		this.extraSortMap = extraSortMap;
		this.printLog = true;
		this.isTopSellRoom = isTopSellRoom;
		this.now = new Date();
		this.userLabelMap = userLabelMap;
	}

	/**
	 * 1、实勘排序 实勘/VR 1000000 
	 * 2、图片排序 有图 500000
	 * 3、房评排序 有房评 250000 
	 * 4、用户标签 大于60-100 180000 大于40-59 120000 小于0-40 60000
	 * 5、权重区间排序 权重大于60 50000 权重50--60 40000  权重40--50 30000  权重30--40 20000  权重小于30 10000   
	 * 6、随机排序权重 小于10000
	 * 7、搜索权重排序
	 *
	 * @return 1+2+3+4+5+6+7
	 */
	@SuppressWarnings("rawtypes")
	protected float func(int doc, FunctionValues[] vals) {
		try {
			if (vals.length <= 10) {
				return 0.0F;
			}
			//SHENZHEN100005094
			String idStr = vals[0].strVal(doc);
			int id = SortUtil.getId(idStr);

			int ranking = vals[2].intVal(doc);

			String sortField = vals[9].strVal(doc);
			
			Map<String, String> map = null;

			// 置顶房源的权重
			float topRanking = 0.0F;
			if (this.isTopSellRoom && sortField != null && sortField.contains("createTime")) {

				map = SortUtil.getMap(sortField, this.printLog);
				if (map != null) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

						Date sellTime = sdf.parse(map.get("createTime"));
						float dateRanking = (float) (this.now.getTime() - sellTime.getTime());
						topRanking = 5.12E8F + dateRanking;
						if (this.printLog) {
							System.out.println("id:" + id + "-->topRanking:" + (new Float(topRanking)).longValue());
						}
						//return topRanking;
					} catch (Exception e) {
						if (this.printLog) {
							e.printStackTrace();
						}
					}
				}
			}

			// 实勘/VR权重
			float cameramanSurveyOrVrRanking = 0;
			String vrCoverUrl= vals[14].strVal(doc);
			if ((StringUtils.isNotBlank(sortField) && sortField.contains("cameramanSurvey")) || StringUtils.isNotBlank(vrCoverUrl)) {
				cameramanSurveyOrVrRanking = 1000000;
			}

			//图片权重
			float pictureRanking=0;
			int pictureCount = vals[1].intVal(doc);
			if(pictureCount > 0){
				pictureRanking=500000;
			}
		
			// 房评权重
			float commentRanking = 0;
			if (StringUtils.isNotBlank(sortField) && sortField.contains("roomCommentCount")) {
				commentRanking = 250000;
			}
			
			//用户标签权重
			float userLabelRanking = 0;
			if (this.userLabelMap != null && !this.userLabelMap.isEmpty()) {

				double price = vals[5].doubleVal(doc);
				String gardenId = vals[3].strVal(doc);
				int bedRoom = vals[4].intVal(doc);
				int livingRoom = vals[16].intVal(doc);
				String businessIdsStr = vals[17].strVal(doc);
				Object businessIds = this.userLabelMap.get("businessIds");
				Object garenIds = this.userLabelMap.get("garenIds");
				Object prices = this.userLabelMap.get("prices");
				Object layouts = this.userLabelMap.get("layouts");
				Object closeMetro = this.userLabelMap.get("closeMetro");
			
				if (this.printLog)
					System.out.println("businessIds:" + businessIds + "garenIds:" + garenIds + ",prices:" + prices+ ",layouts:" + layouts + ",closeMetro:" + closeMetro);
				
				//用户标签得分
				int userLabelscores =0;
				//商圈
				if (StringUtils.isNotBlank(businessIdsStr) && businessIds != null) {
					for (String IdStr : businessIdsStr.split(",")) {
						if (((List) businessIds).contains(IdStr)) {
							userLabelscores+=20;
							break;
						}
					}
				}
				//小区
				if (garenIds != null && ((List) garenIds).contains(gardenId)) {
					userLabelscores += 10;
				}
				//价格
				if (prices != null) {
					String[] split = prices.toString().split(",");
					double start = Double.parseDouble(split[0].split("-")[0]);
					double end = Double.parseDouble(split[0].split("-")[1]);
					if ((price >= start && price <= end)) {
						userLabelscores+=40;
					}else if(split.length >1){
						double start1 = Double.parseDouble(split[1].split("-")[0]);
						double end1 = Double.parseDouble(split[1].split("-")[1]);
						if ((price >= start1 && price <= end1)) {
							userLabelscores+=40;
						}
					}
				}
				//户型
				if (layouts != null && ((List) layouts).contains(bedRoom + "-" + livingRoom + "".trim())) {
					userLabelscores+=20;
				}
				//地铁
				if (closeMetro != null && Boolean.parseBoolean(closeMetro.toString())
						&& sortField.contains("subwayStation")) {
					userLabelscores+=10;
				}
				//分数范围得分
				if(userLabelscores > 59){
					userLabelRanking=180000;
				}else if(userLabelscores > 39){
					userLabelRanking=120000;
				}else{
					userLabelRanking=60000;
				}
				
				if (this.printLog) {
					System.out.println("id:" + id + ",price:" + price + ",gardenId:" + gardenId + ",bedRoom:" + bedRoom
							+ ",livingRoom:" + livingRoom + ",businessIdsStr:" + businessIdsStr
							+",userLabelscores"+userLabelscores
							+ ">>>>userLabelRanking:" + userLabelRanking);
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
				Map<String, Float> parentAreaIdsSortMap = this.extraSortMap.get("parentAreaIds");
				Map<String, Float> businessIdsSortMap = this.extraSortMap.get("businessIds");
				Map<String, Float> officeBusinessIdsSortMap = this.extraSortMap.get("officeBusinessIds");
				Map<String, Float> lineSortMap = this.extraSortMap.get("subwayLine");
				Map<String, Float> stationSortMap = this.extraSortMap.get("subwayStation");
				Map<String, Float> schoolSortMap = this.extraSortMap.get("schoolNames");

				Map<String, Float> bedRoomSortMap = this.extraSortMap.get("bedRoom");
				Map<String, Float> priceSortMap = this.extraSortMap.get("price");
				Map<String, Float> areaSortMap = this.extraSortMap.get("area");
				Map<String, Float> decorationSortMap = this.extraSortMap.get("decoration");
				Map<String, Float> directionSortMap = this.extraSortMap.get("direction");
				Map<String, Float> floorSortMap = this.extraSortMap.get("floor");
				Map<String, Float> labelDescSortMap = this.extraSortMap.get("labelDesc");
				Map<String, Float> titleSortMap = this.extraSortMap.get("title");
				// 精准小区优先排序（有社区）
				Map<String, Float> communitySortMap = this.extraSortMap.get("communitySort");
				if (map == null) {
					map = SortUtil.getMap(sortField, this.printLog);
				}

				if (gardenSortMap != null && !gardenSortMap.isEmpty()) {
					int gardenId = vals[3].intVal(doc);

					String gardenIdStr = String.valueOf(gardenId);
					if (gardenSortMap.containsKey(gardenIdStr)) {
						Float f = gardenSortMap.get(gardenIdStr);
						if (f != null && f.floatValue() > 0.0F) {
							extraSortRanking = extraSortRanking + roomRankMaxScore * f + 5.12E10F; //精准小区搜索分值增大，比售卖分值高
							if (this.printLog) {
								System.out.println("gardenId:" + (new Float(extraSortRanking)).longValue());
							}
						}
					}
				}

				boolean dist = false;

				if (parentAreaIdsSortMap != null && !parentAreaIdsSortMap.isEmpty() && !dist) {
					if (map != null && !map.isEmpty()) {
						String parentAreaIds = map.get("parentAreaIds");
						if (StringUtils.isNotBlank(parentAreaIds)) {
							for (String parentAreaId : parentAreaIds.split(",")) {
								if (parentAreaIdsSortMap.containsKey(parentAreaId)) {
									Float f = parentAreaIdsSortMap.get(parentAreaId);
									if (f != null && f.floatValue() > 0.0F) {
										extraSortRanking += roomRankMaxScore * f;
										dist = true;
										if (this.printLog) {
											System.out.println("parentAreaIds:" + (new Float(extraSortRanking)).longValue());
										}

										break;
									}
								}
							}
						}
					}
				}

				if (businessIdsSortMap != null && !businessIdsSortMap.isEmpty() && !dist && map != null
						&& !map.isEmpty()) {
					String businessIds = map.get("businessIds");
					if (StringUtils.isNotBlank(businessIds)) {
						for (String businessId : businessIds.split(",")) {
							if (businessIdsSortMap.containsKey(businessId)) {
								Float f = businessIdsSortMap.get(businessId);
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									dist = true;
									if (this.printLog) {
										System.out.println("businessIds:" + (new Float(extraSortRanking)).longValue());
									}

									break;
								}
							}
						}
					}
				}
				if (officeBusinessIdsSortMap != null && !officeBusinessIdsSortMap.isEmpty() && !dist && map != null
						&& !map.isEmpty()) {
					String officeBusinessIds = map.get("officeBusinessIds");
					if (StringUtils.isNotBlank(officeBusinessIds)) {
						for (String officeBusinessId : officeBusinessIds.split(",")) {
							if (officeBusinessIdsSortMap.containsKey(officeBusinessId)) {
								Float f = officeBusinessIdsSortMap.get(officeBusinessId);
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									dist = true;
									if (this.printLog) {
										System.out.println("officeBusinessIds:" + (new Float(extraSortRanking)).longValue());
									}

									break;
								}
							}
						}
					}
				}

				if (lineSortMap != null && !lineSortMap.isEmpty() && !dist) {
					if (map != null && !map.isEmpty()) {
						String subwayLine = map.get("subwayLine");
						if (StringUtils.isNotBlank(subwayLine)) {
							for (String lineId : subwayLine.split(",")) {
								if (lineSortMap.containsKey(lineId)) {
									Float f = lineSortMap.get(lineId);
									if (f != null && f.floatValue() > 0.0F) {
										extraSortRanking += roomRankMaxScore * f;
										dist = true;
										if (this.printLog) {
											System.out.println("subwayLine:" + (new Float(extraSortRanking)).longValue());
										}

										break;
									}
								}
							}
						}
					}
				}

				if (stationSortMap != null && !stationSortMap.isEmpty() && !dist && map != null && !map.isEmpty()) {
					String subwayStation = map.get("subwayStation");
					if (StringUtils.isNotBlank(subwayStation)) {
						for (String stationId : subwayStation.split(",")) {
							if (stationSortMap.containsKey(stationId)) {
								Float f = stationSortMap.get(stationId);
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									dist = true;
									if (this.printLog) {
										System.out.println("subwayStation:" + (new Float(extraSortRanking)).longValue());
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
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									dist = true;
									if (this.printLog) {
										System.out.println("schoolNames:" + (new Float(extraSortRanking)).longValue());
									}

									break;
								}
							}
						}
					}
				}

				if (bedRoomSortMap != null && !bedRoomSortMap.isEmpty()) {
					int bedRoom = 0;
					if (vals.length <= 10) {
						bedRoom = vals[7].intVal(doc);
					} else {
						bedRoom = vals[4].intVal(doc);
					}

					for (String key : bedRoomSortMap.keySet()) {
						try {
							String[] keys = key.split("-");
							int start = Integer.valueOf(keys[0]).intValue();
							int end = Integer.valueOf(keys[1]).intValue();
							if (bedRoom >= start && bedRoom <= end) {
								Float f = bedRoomSortMap.get(key);
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("bedRoom:" + (new Float(extraSortRanking)).longValue());
									}
									break;
								}
							}
						} catch (Exception exception) {
						}
					}
				}

				if (priceSortMap != null && !priceSortMap.isEmpty()) {
					double price = vals[5].doubleVal(doc);

					for (String key : priceSortMap.keySet()) {
						try {
							String[] keys = key.split("-");
							int start = Integer.valueOf(keys[0]).intValue();
							int end = Integer.valueOf(keys[1]).intValue();
							if (price >= start && price <= end) {
								Float f = priceSortMap.get(key);
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("price:" + (new Float(extraSortRanking)).longValue());
									}
									break;
								}
							}
						} catch (Exception exception) {
						}
					}
				}

				if (areaSortMap != null && !areaSortMap.isEmpty()) {
					double area = vals[5].doubleVal(doc);

					for (String key : areaSortMap.keySet()) {
						try {
							String[] keys = key.split("-");
							int start = Integer.valueOf(keys[0]).intValue();
							int end = Integer.valueOf(keys[1]).intValue();
							if (area >= start && area <= end) {
								Float f = areaSortMap.get(key);
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f;
									if (this.printLog) {
										System.out.println("area:" + (new Float(extraSortRanking)).longValue());
									}
									break;
								}
							}
						} catch (Exception exception) {
						}
					}
				}

				if (decorationSortMap != null && !decorationSortMap.isEmpty()) {
					String decoration = vals[6].strVal(doc);

					if (decorationSortMap.containsKey(decoration)) {
						Float f = decorationSortMap.get(decoration);
						if (f != null && f.floatValue() > 0.0F) {
							extraSortRanking += roomRankMaxScore * f;
							if (this.printLog) {
								System.out.println("decoration:" + (new Float(extraSortRanking)).longValue());
							}
						}
					}
				}

				if (directionSortMap != null && !directionSortMap.isEmpty() && map != null && !map.isEmpty()) {
					String direction = map.get("direction");
					if (directionSortMap.containsKey(direction)) {
						Float f = directionSortMap.get(direction);
						if (f != null && f.floatValue() > 0.0F) {
							extraSortRanking += roomRankMaxScore * f;
							if (this.printLog) {
								System.out.println("direction:" + (new Float(extraSortRanking)).longValue());
							}
						}
					}
				}

				if (floorSortMap != null && !floorSortMap.isEmpty()) {
					int floor = vals[8].intVal(doc);

					for (String key : floorSortMap.keySet()) {
						try {
							String[] keys = key.split("-");
							int start = Integer.valueOf(keys[0]).intValue();
							int end = Integer.valueOf(keys[1]).intValue();
							if (floor >= start && floor <= end) {
								Float f = floorSortMap.get(key);
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f.floatValue();
									if (this.printLog) {
										System.out.println("floor:" + (new Float(extraSortRanking)).longValue());
									}
									break;
								}
							}
						} catch (Exception exception) {
						}
					}
				}

				if (labelDescSortMap != null && !labelDescSortMap.isEmpty() && map != null && !map.isEmpty()) {
					String labelDesc = map.get("labelDesc");
					if (StringUtils.isNotBlank(labelDesc) && StringUtils.isNotBlank(labelDesc)) {
						for (String desc : labelDesc.split("\\|")) {
							if (labelDescSortMap.containsKey(desc)) {
								Float f = labelDescSortMap.get(desc);
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f.floatValue();
									if (this.printLog) {
										System.out.println("labelDesc:" + (new Float(extraSortRanking)).longValue());
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
								if (f != null && f.floatValue() > 0.0F) {
									extraSortRanking += roomRankMaxScore * f.floatValue();
									if (this.printLog) {
										System.out.println("title:" + (new Float(extraSortRanking)).longValue());
									}

									break;
								}
							}
						}
					}
				}

				if (communitySortMap != null) {
					return extraSortRanking;
				}
			}

			if (id > 0) {
				randomRanking = SortUtil.getRandomRanking(this.key, id);
			}
            float sum = cameramanSurveyOrVrRanking+pictureRanking+commentRanking+userLabelRanking + scoreRanking + randomRanking + extraSortRanking + topRanking;
			floats.add(ImmutableMap.of("id",idStr,"sum",String.valueOf(sum)));
			if (this.printLog) {
				System.out.println("id:" + id 
						+ "-->cameramanSurveyOrVRRanking:" + (new Float(cameramanSurveyOrVrRanking)).longValue()
						+ ",pictureRanking:" + (new Float(pictureRanking)).longValue()
						+ ",commentRanking:" + (new Float(commentRanking)).longValue() 
						+ ",userLabelRanking:" + (new Float(userLabelRanking)).longValue()
						+ ",scoreRanking:"+ (new Float(scoreRanking)).longValue()
						+ ",extraSortRanking:"+ (new Float(extraSortRanking)).longValue()
						+ ",randomRanking:"+ (new Float(randomRanking)).longValue());
				System.out.println("sum: "+sum);
			}
			
			return sum;
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

    public static List<Map<String, String>> getFloats() {
        return floats;
    }
}
