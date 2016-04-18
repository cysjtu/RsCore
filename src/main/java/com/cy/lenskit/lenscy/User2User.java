package com.cy.lenskit.lenscy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer;
import org.grouplens.lenskit.baseline.UserMeanBaseline;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
import org.grouplens.lenskit.data.sql.SQLStatementFactory;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.knn.user.NeighborFinder;
import org.grouplens.lenskit.knn.user.SnapshotNeighborFinder;
import org.grouplens.lenskit.knn.user.UserUserItemScorer;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.MeanCenteringVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.VectorNormalizer;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cy.lenskit.lenscy.util.LoaderService;

public class User2User {

	
    private static final Logger logger =
            LoggerFactory.getLogger(User2User.class);
    
	private LenskitConfiguration config;
	
	private LenskitRecommender rec;
	
	private  RatingPredictor pred;
	
	private ItemRecommender irec;
	
	private Connection con;

	private SQLStatementFactory stmt=null;
	
	private String jdbcUrl, user, passwd;
	
	
	
	public SQLStatementFactory getStmt() {
		return stmt;
	}

	public void setStmt(SQLStatementFactory stmt) {
		this.stmt = stmt;
	}

	
	public User2User(String jdbcUrl,String user,String passwd){
		
        this.jdbcUrl=jdbcUrl;
        this.user=user;
        this.passwd=passwd;

	}
	
	
	public void init(){
		config = new LenskitConfiguration();
		config.bind(ItemScorer.class).to(UserUserItemScorer.class);
		
		config.bind(BaselineScorer.class, ItemScorer.class).to(UserMeanItemScorer.class);
		
		config.bind(UserMeanBaseline.class, ItemScorer.class).to(ItemMeanRatingItemScorer.class);
		
		config.bind(NeighborFinder.class).to(SnapshotNeighborFinder.class);
		
		
		config.within(UserVectorNormalizer.class)
		.bind(VectorNormalizer.class)
		.to(MeanCenteringVectorNormalizer.class);
		
		
		config.set(NeighborhoodSize.class).to(30);
		
		
		//=================================
		
        con = null; //定义一个MYSQL链接对象
        try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			jdbcUrl=(jdbcUrl!=null && !jdbcUrl.isEmpty())?jdbcUrl:"jdbc:mysql://127.0.0.1:3306/lenskit";
			user=(null!=user && !user.isEmpty())?user:"root";
			passwd=(null!=passwd && !passwd.isEmpty())?passwd:"cy1993";
			
			
			con = DriverManager.getConnection(jdbcUrl, user, passwd); //链接本地MYSQL
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //MYSQL驱动
        catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        System.out.println("init jdbc connection finished");
        
       
        
        JDBCRatingDAO dao = new JDBCRatingDAO(con, stmt);

        
        config.addComponent(dao);
        
        //==========================================
        
        
        
        try {
			rec = LenskitRecommender.build(config);
		} catch (RecommenderBuildException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        pred = rec.getRatingPredictor();
        
        irec = rec.getItemRecommender();
        
        
        			
	}
	
	public void finalize(){
		
		if(null!=con){
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public double predictScore(long uid,long itemId){
		
		double score = pred.predict(uid, itemId);
		return score;
		
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String jdbcUrl="jdbc:mysql://127.0.0.1:3306/lenskit";
		String user="root";
		String passwd="cy1993";
		
		User2User user2user=new User2User(jdbcUrl, user, passwd);
		
		user2user.setStmt(new ml_1m_Statement());
		
		user2user.init();
		
		//===============
		
		ApplicationContext ctx=new ClassPathXmlApplicationContext("application-context.xml");
		
		LoaderService lds=(LoaderService) ctx.getBean("loaderService");

		List<Map<String,Object>> tests=lds.getTestSet();
		
		double RMSE=0.0;
		
		double  sum=0;
		double cnt=tests.size();
		int step=0;
		for( Map<String,Object> ite:tests){
			System.out.println(++step);
			
			Long user1=(Long) ite.get("user");
			Long item=(Long) ite.get("item");
			Integer rating=(Integer) ite.get("rating");
			double score=user2user.predictScore(user1, item);
			
			sum+=(score-rating)*(score-rating);
			
			
			
		}
		
		
		RMSE=Math.sqrt(sum/(cnt+1));
		
		
		System.err.println("RMSE = "+RMSE);
		
		
				
				
		
	}

	public List<Double> predictScore(Integer uid, List<Long> items) {
		// TODO Auto-generated method stub
		SparseVector ret=pred.predict(uid, items);
		
		if(ret.size()!=items.size()){
			System.err.println("ret.size()!=items.size()");
			System.exit(0);
			
		}
		
		List<Double> res=new ArrayList<Double>(items.size());
		
		
		for(long item:items){
			res.add(ret.get(item));
		}

		return res;
		
	}

	
	
	
	
}
