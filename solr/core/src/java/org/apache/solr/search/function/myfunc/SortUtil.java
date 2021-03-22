package org.apache.solr.search.function.myfunc;


import org.apache.commons.lang.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SortUtil {
	
	public static float getRandomRanking(int key,int id){
		try {
			if(key<=0 || id<=0){
				return 0;
			}
			return hash(String.valueOf(key+id));
		} catch (Exception e) {
		}
		return 0;
	}
	
	private static long hash(String key) {
		ByteBuffer buf = ByteBuffer.wrap(key.getBytes(StandardCharsets.UTF_8));
        int seed = 0x1234ABCD;
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        long m = 0xc6a4a7935bd1e995L;
        int r = 47;
        long h = seed ^ (buf.remaining() * m);
        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();
            k *= m;
            k ^= k >>> r;
            k *= m;
            h ^= k;
            h *= m;
        }
        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }
        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;
        buf.order(byteOrder);
        long value=Math.abs(h/100000000000000l);
        if(value>=100000){
        	return 99999;
        }
        return value;
    }
	
	
	public static Map<String,String> getMap(String sortField,boolean printLog){
		Map<String, String> map  = new HashMap<String, String>(); 
		try {
			if(StringUtils.isNotBlank(sortField)){
				
				sortField = sortField.replace("{", "").replace("}", "").replace("[", "").replace("]", "");
				for(String str:sortField.split("','")){
					str = str.replace("'", "");
					if(StringUtils.isBlank(str)){
						continue;
					}
					String[] array = str.split(":");
					if(array.length == 2){
						String key = str.split(":")[0];
						String value = str.split(":")[1];
						if(printLog){
							System.out.println(key+"-->"+value);
						}
						map.put(key, value);
					}
				}
				return map;
			}
			
		} catch (Exception e) {
			if(printLog){
				e.printStackTrace();
			}
		}
		
		return map;
	}
	
	
	public static int getId(String idStr) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(idStr);
		
		while (matcher.find()) {
			return Integer.parseInt(matcher.group(0));
		}
		
		throw new RuntimeException("id not contains number");
	}
}
