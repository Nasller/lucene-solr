package org.apache.solr.search.function.myfunc.room;

import java.util.List;

public class RoomSortScore {
	private int startValue;
	private int endValue;
	private float scoreValue;
	public RoomSortScore() {
	}
	
	public RoomSortScore(int startValue,int endValue,float scoreValue) {
		this.startValue=startValue;
		this.endValue=endValue;
		this.scoreValue=scoreValue;
	}
	
	public static float getScore(List<RoomSortScore> scores,int ranking){
		try {
			if(scores==null || ranking<=0){
				return 0;
			}
			
			for(RoomSortScore score:scores){
				if(ranking>score.startValue && ranking<=score.endValue){
					return  score.scoreValue;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
}
