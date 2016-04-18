package com.cy.lenskit.lenscy.util;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LRUCache<K,V> extends LinkedHashMap<K, V> {
	
	private static Logger log=LoggerFactory.getLogger(LRUCache.class);
	
	private int cacheSize;

	private LinkedHashMap<K, V> cache=null;
	
 	public LRUCache(int cacheSize){
		this.cacheSize=cacheSize;
		
		int hashTableCapacity=(int)Math.ceil(cacheSize/0.75);
		cache=new LinkedHashMap<K, V>(hashTableCapacity, 0.75f, true){
			
			
			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
				// TODO Auto-generated method stub
				
			

				return size()>LRUCache.this.cacheSize;
				
			}
		};
		
		
	}
 	
 	
 	
 	public V put(K key,V val){
 		return cache.put(key, val);
 	}
 	
 	
 	public V get(Object key){
 		return cache.get(key);
 	}
 	
 	
}
