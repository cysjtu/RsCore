package com.cy.lenskit.lenscy.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IdMapingDao {

	public static Logger log=LoggerFactory.getLogger(IdMapingDao.class);
	
	@Autowired
	JdbcTemplate jdbc;
	
	
	private Map<String,Integer> string2int=null;
	
	
	public void doMap(String table,String colem){
		StringBuilder sb=new StringBuilder();
		sb.append("select distinct ").append(colem).append(" from ").append(table).append(";");
		String getstringIds=sb.toString();
		
		List<String> res=jdbc.queryForList(getstringIds, String.class);
		
		sb=new StringBuilder();
		sb.append("insert into idmapping(stringId) values ");
		
		
		
		for(String s:res){
			sb.append("(\"").append(s).append("\"),");
			
		}
		
		try{
			sb.setCharAt(sb.length()-1, ';');			
			jdbc.update(sb.toString());
			
		}catch(Exception e){
			log.error(e.getMessage());
			
		}
		log.error("finish maping");
		
		
	}
	
	public Integer String2Id(String sid){
		String sql="select intId from idmapping where stringId=? ;";
		
		List<Integer> res=jdbc.queryForList(sql, Integer.class);
		
		if(null==res || res.size()!=1){
			log.error("String2Id failed!!!");
			return null; 
		}
		else{
			return res.get(0);
			
		}
	}
	
	public Integer String2IdCatched(String sid){
		if(null==string2int){
			
			synchronized(this){
				if(null==string2int){
					string2int=new HashMap<String, Integer>(250000);
					
					String sql="select * from idmapping ;";
					List<Map<String,Object>> res=jdbc.queryForList(sql);
					for(Map<String,Object> m:res){
						String strid=(String) m.get("stringId");
						Integer iid=(Integer) m.get("intId");
						string2int.put(strid, iid);
						
						
					}
				}
			}
			
		}
		
		
		return string2int.get(sid);
	
		
	}
}
