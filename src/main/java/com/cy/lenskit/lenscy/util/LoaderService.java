package com.cy.lenskit.lenscy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import it.unimi.dsi.fastutil.Arrays;

@Service
public class LoaderService {

	@Autowired
	private JdbcTemplate jdbctemplate;

	private BufferedReader getReader(String file){
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)),"utf-8"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	
	public void loadDate(String users,String items,String ratings){
		
		
		
		
	}
	
	public void  loadRatings(String ratings) throws IOException{
		
		
		BufferedReader br=getReader(ratings);
		
		String line=br.readLine();
		
		String sql="insert into ml_1m(user,item,rating) values ";
		
		int cnt=0;
		
		StringBuilder sb=new StringBuilder(sql);
		
		for(;line!=null;line=br.readLine()){
			
			cnt++;
			
			String [] uir=line.split("::");

			sb.append("(").append(uir[0]).append(",").append(uir[1]).append(",").append(uir[2]).append("),");
			
			System.out.println(cnt);
			
			if(cnt>10000){
				cnt=0;
				sb.setCharAt(sb.length()-1,';');
				jdbctemplate.update(sb.toString());
				sb=new StringBuilder(sql);
				
			}
			
		}
		
		
		if(cnt!=0){
			cnt=0;
			sb.setCharAt(sb.length()-1,';');
			jdbctemplate.update(sb.toString());
			sb=new StringBuilder(sql);
			
		}
		
		
	}
	
	
	public void genTestSet(){
		
		String sql="select count(*) from ml_1m";
		int cnt=jdbctemplate.queryForInt(sql);
		int test_cnt=cnt/10;
		
		Set<Integer> ids=new TreeSet<Integer>();
		List<Integer> ls=new ArrayList<Integer>();
		
		while(ids.size()<test_cnt){
			int tmp=(int)(Math.random()*cnt);
			tmp=tmp>=0?tmp:0;
			if(!ids.contains(tmp)){
				ids.add(tmp);
				ls.add(tmp);
			}
		}
		
		ls.sort(null);

		String sql2="select id,user,item,rating from ml_1m where id in ( ";
		String sql4="delete from ml_1m where id in ( ";
		
		StringBuilder sb=new StringBuilder(sql2);
		StringBuilder sb2=new StringBuilder(sql4);
		for(Integer i: ls){
			sb.append(i).append(",");
			sb2.append(i).append(",");
		}
		
		sb.setCharAt(sb.length()-1, ')');
		sb2.setCharAt(sb2.length()-1, ')');
		
		
		
		List<Map<String, Object> > res=jdbctemplate.queryForList(sb.toString());
		
		String sql3="insert into ml_1m_test(id,user,item,rating) values ";
		sb=new StringBuilder(sql3);
		
		for(Map<String, Object> item :res){
			sb.append("(")
			.append(item.get("id")).append(",")
			.append(item.get("user")).append(",")
			.append(item.get("item")).append(",")
			.append(item.get("rating")).append(")").append(",");
			
		}
		
		sb.setCharAt(sb.length()-1, ';');
		
		
		jdbctemplate.update(sb.toString());
		
		//删除被选取的数据
		
		jdbctemplate.update(sb2.toString());
		
		
		
		
		
	}
	
	
	public List<Map<String,Object>> getTestSet(){
		return jdbctemplate.queryForList("select id,user,item,rating from ml_1m_test;");
	}
	
	
	public static void main(String [] args) throws IOException{
		
		ApplicationContext ctx=new ClassPathXmlApplicationContext("application-context.xml");
		
		LoaderService lds=(LoaderService) ctx.getBean("loaderService");
		
		//lds.loadRatings("/Users/nali/GitHub/RecSysDataSet/ml-1m/ratings.txt");

		lds.genTestSet();
		
		
	}
	
}
