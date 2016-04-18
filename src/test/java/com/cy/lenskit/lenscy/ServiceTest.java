package com.cy.lenskit.lenscy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.la4j.Matrix;
import org.la4j.decomposition.SingularValueDecompositor;
import org.la4j.matrix.sparse.CRSMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cy.lenskit.lenscy.dao.RatingDao;
import com.cy.lenskit.lenscy.dao.SqlStatement;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value="classpath:application-context.xml")
public class ServiceTest implements InitializingBean{

	public static Logger log=LoggerFactory.getLogger(ServiceTest.class);
	
	
	@Autowired
	JdbcTemplate jdbc;
	
	@Autowired
	RatingDao rating;
	
	
	String jdbcUrl="jdbc:mysql://127.0.0.1:3306/easyrec";
	String user="root";
	String passwd="cy1993";

	String table="action_processed", userCol="user_id", itemCol="item_id", rateCol="rate";
	
	
	SqlStatement stmt=new SqlStatement();
	{
	stmt.setTable("action_processed_test");
	stmt.setUser("user_id");
	stmt.setItem("item_id");
	stmt.setRate("rate");
	}
	
	
	List<Map<String,Object>> tests=null;
	
	
	@Test
	public void matrixTest(){
		CRSMatrix m=new CRSMatrix(278858+10,271379+10);
		
		List<Map<String,Object>> res=rating.getTestSet(table,userCol,itemCol,rateCol);
		
		int step=0;
		for( Map<String,Object> ite:res){
			if((++step)%1000==0){
				System.out.println(step);
			}
			
			
			Integer user1=(Integer) ite.get("user_id");
			Integer item=(Integer) ite.get("item_id");
			Double rating=(Double) ite.get("rate");
			
			m.set(user1, item, rating);
			
			
		}
		
		
		log.error("cardinary="+m.cardinality()+"");
		
		SingularValueDecompositor svd=new SingularValueDecompositor(m);
		svd.decompose();
		
		
		
	}
	
	@Test
	@Ignore
	public void SVD_Test(){
		SVD svd=new SVD(jdbcUrl, user, passwd);
		
		svd.setStmt(stmt);
		
		svd.init();

		double RMSE=0.0;
		
		double  sum=0;
		double cnt=tests.size();
		int step=0;
		Map<Integer,List<Long>> user2items=new HashMap<Integer, List<Long>>();
		Map<Integer,List<Double>> user2scores=new HashMap<Integer, List<Double>>();
		//Map<Integer,List<Double>> user2predict=new HashMap<Integer, List<Double>>();
		
		for( Map<String,Object> ite:tests){
			if((++step)%1000==0){
				System.out.println(step);
			}
			
			
			Integer user1=(Integer) ite.get("user_id");
			Integer item=(Integer) ite.get("item_id");
			Double rating=(Double) ite.get("rate");
			
			if(!user2items.containsKey(user1)){
				List<Long> temp1=new ArrayList<Long>(500);
				List<Double>temp2=new ArrayList<Double>(500);
				
				
				user2items.put(user1, temp1);
				user2scores.put(user1, temp2);
				
			}
			
			
			user2items.get(user1).add(Long.valueOf(item));
			
			user2scores.get(user1).add(rating);
			
			//double score=svd.predictScore(user1, item);
			
			//sum+=(score-rating)*(score-rating);

		}
		int step2=0;
		for(Integer u:user2items.keySet()){
			
			if((++step2)%1000==0){
				System.out.println(step2);
			}
			
			List<Double> ret=svd.predictScore(u, user2items.get(u));
			List<Double> sc=user2scores.get(u);
			
			for(int i=0;i<sc.size();++i){
				sum+=(ret.get(i)-sc.get(i))*(ret.get(i)-sc.get(i));
				
			}
			
		}
		
		
		RMSE=Math.sqrt(sum/(cnt+1));

		log.error("SVD_Test RMSE={}",RMSE);
		
	}
	
	
	@Test
	
	public void SVD_SGD_TEST(){
		
		SVD_SGD svd=new SVD_SGD(20, false);
		try {
			svd.loadFileSQL(jdbc);
			svd.train(0.01f, 0.05f, 80);
			svd.predict(jdbc);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	
	public void SVD_PLUS_PLUS_TEST(){
		SVD_PlusPlus svd=new SVD_PlusPlus(30, false);
		try {
			svd.loadFileSQL(jdbc);
			svd.train(0.01f, 0.018f, 40);
			svd.predict(jdbc);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	@Test
	@Ignore
	public void SlopOne_Test(){
		SlopOne slopone=new SlopOne(jdbcUrl, user, passwd);
		
		slopone.setStmt(stmt);
		
		slopone.init();

		
		
		double RMSE=0.0;
		
		double  sum=0;
		double cnt=tests.size();
		int step=0;
		Map<Integer,List<Long>> user2items=new HashMap<Integer, List<Long>>();
		Map<Integer,List<Double>> user2scores=new HashMap<Integer, List<Double>>();
		//Map<Integer,List<Double>> user2predict=new HashMap<Integer, List<Double>>();
		
		for( Map<String,Object> ite:tests){
			if((++step)%1000==0){
				System.out.println(step);
			}
			
			
			Integer user1=(Integer) ite.get("user_id");
			Integer item=(Integer) ite.get("item_id");
			Double rating=(Double) ite.get("rate");
			
			if(!user2items.containsKey(user1)){
				List<Long> temp1=new ArrayList<Long>(500);
				List<Double>temp2=new ArrayList<Double>(500);
				
				
				user2items.put(user1, temp1);
				user2scores.put(user1, temp2);
				
			}
			
			
			user2items.get(user1).add(Long.valueOf(item));
			
			user2scores.get(user1).add(rating);
			
			//double score=svd.predictScore(user1, item);
			
			//sum+=(score-rating)*(score-rating);

		}
		int step2=0;
		for(Integer u:user2items.keySet()){
			
			if((++step2)%1000==0){
				System.out.println(step2);
			}
			
			List<Double> ret=slopone.predictScore(u, user2items.get(u));
			List<Double> sc=user2scores.get(u);
			
			for(int i=0;i<sc.size();++i){
				sum+=(ret.get(i)-sc.get(i))*(ret.get(i)-sc.get(i));
				
			}
			
		}
		
		
		
		RMSE=Math.sqrt(sum/(cnt+1));
		
		
		log.error("SlopOne_Test RMSE={}",RMSE);
		
		
	}
	
	
	@Test
	
	public void User2User_test(){
		User2User user2user=new User2User(jdbcUrl, user, passwd);
		
		user2user.setStmt(stmt);
		
		user2user.init();
		
		
		double RMSE=0.0;
		
		double  sum=0;
		double cnt=tests.size();
		int step=0;
		Map<Integer,List<Long>> user2items=new HashMap<Integer, List<Long>>();
		Map<Integer,List<Double>> user2scores=new HashMap<Integer, List<Double>>();
		//Map<Integer,List<Double>> user2predict=new HashMap<Integer, List<Double>>();
		
		for( Map<String,Object> ite:tests){
			if((++step)%1000==0){
				System.out.println(step);
			}
			
			
			Integer user1=(Integer) ite.get("user_id");
			Integer item=(Integer) ite.get("item_id");
			Double rating=(Double) ite.get("rate");
			
			if(!user2items.containsKey(user1)){
				List<Long> temp1=new ArrayList<Long>(500);
				List<Double>temp2=new ArrayList<Double>(500);
				
				
				user2items.put(user1, temp1);
				user2scores.put(user1, temp2);
				
			}
			
			
			user2items.get(user1).add(Long.valueOf(item));
			
			user2scores.get(user1).add(rating);
			
			//double score=svd.predictScore(user1, item);
			
			//sum+=(score-rating)*(score-rating);

		}
		int step2=0;
		for(Integer u:user2items.keySet()){
			
			if((++step2)%1000==0){
				System.out.println(step2);
			}
			
			List<Double> ret=user2user.predictScore(u, user2items.get(u));
			List<Double> sc=user2scores.get(u);
			
			for(int i=0;i<sc.size();++i){
				sum+=(ret.get(i)-sc.get(i))*(ret.get(i)-sc.get(i));
				
			}
			
		}
		
		
		RMSE=Math.sqrt(sum/(cnt+1));

		
		log.error("User2User_test RMSE={}",RMSE);

	}
	
	
	@Test
	
	public void Item2Item_TEST(){
		//ItemItemScorer
		//org.grouplens.lenskit.knn.item.model.ItemItemModelBuilder
		Item2Item item2item=new Item2Item(jdbcUrl, user, passwd);
		
		item2item.setStmt(stmt);
		
		item2item.init();

		double RMSE=0.0;
		double  sum=0;
		double cnt=tests.size();
		int step=0;
		Map<Integer,List<Long>> user2items=new HashMap<Integer, List<Long>>();
		Map<Integer,List<Double>> user2scores=new HashMap<Integer, List<Double>>();
		//Map<Integer,List<Double>> user2predict=new HashMap<Integer, List<Double>>();
		
		for( Map<String,Object> ite:tests){
			if((++step)%1000==0){
				System.out.println(step);
			}
			
			
			Integer user1=(Integer) ite.get("user_id");
			Integer item=(Integer) ite.get("item_id");
			Double rating=(Double) ite.get("rate");
			
			if(!user2items.containsKey(user1)){
				List<Long> temp1=new ArrayList<Long>(500);
				List<Double>temp2=new ArrayList<Double>(500);
				
				
				user2items.put(user1, temp1);
				user2scores.put(user1, temp2);
				
			}
			
			
			user2items.get(user1).add(Long.valueOf(item));
			
			user2scores.get(user1).add(rating);
			
			//double score=svd.predictScore(user1, item);
			
			//sum+=(score-rating)*(score-rating);

		}
		int step2=0;
		for(Integer u:user2items.keySet()){
			
			if((++step2)%1000==0){
				System.out.println(step2);
			}
			
			List<Double> ret=item2item.predictScore(u, user2items.get(u));
			List<Double> sc=user2scores.get(u);
			
			for(int i=0;i<sc.size();++i){
				sum+=(ret.get(i)-sc.get(i))*(ret.get(i)-sc.get(i));
				
			}
			
		}
		
		
		
		RMSE=Math.sqrt(sum/(cnt+1));
		
		log.error("Item2Item_TEST RMSE={}",RMSE);
		
	}


	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		tests=rating.getTestSet(table+"_test",userCol,itemCol,rateCol);
	}
	
}
