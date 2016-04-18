package com.cy.lenskit.lenscy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cy.lenskit.lenscy.util.MathTool;;


public class SVD_SGD implements Trainer {
	
	public static Log log=LogFactory.getLog(SVD_SGD.class);
	
	protected boolean isTranspose;
	protected int mUserNum;
	protected int mItemNum;
	protected int dim;
	protected float[][] p;
	protected float[][] q;
	protected float[] bu;
	protected float[] bi;
	protected float mean;
	protected float mMaxRate;
	protected float mMinRate;
	protected String mTestFileName;
	protected String mSeparator;
	protected MathTool mt;
	protected Map<Integer, Integer> mUserId2Map;
	protected Map<Integer, Integer> mItemId2Map;
	protected List<Node>[] mRateMatrix;

	public SVD_SGD(int dim, boolean isTranspose) {
		this.isTranspose = isTranspose;
		this.dim = dim;
		mt = MathTool.getInstance();
		mUserId2Map = new HashMap<Integer, Integer>();
		mItemId2Map = new HashMap<Integer, Integer>();
	}

	private void mapping(String fileName, String separator) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(
				new File(fileName)));
		int userId;
		int itemId;
		int mLineNum = 0;
		String mLine;
		while ((mLine = br.readLine()) != null) {
			String[] splits = mLine.split(separator);
			userId = Integer.valueOf(splits[0]);
			itemId = Integer.valueOf(splits[1]);
			if (isTranspose) {
				int temp = userId;
				userId = itemId;
				itemId = temp;
			}
			if (!mUserId2Map.containsKey(userId)) {
				mUserNum++;
				mUserId2Map.put(userId, mUserNum);
			}
			if (!mItemId2Map.containsKey(itemId)) {
				mItemNum++;
				mItemId2Map.put(itemId, mItemNum);
			}
			mLineNum++;
			if (mLineNum % 50000 == 0)
				print(mLineNum + " lines read");
		}
	}

	
	
	
	private void mapping(List<Integer> users,List<Integer> items) throws Exception {
		
		int mLineNum = 0;
	
		if (isTranspose) {
			List<Integer> temp = users;
			users = items;
			items = temp;
		}
		
		
		for(Integer user:users) {
			
			
			
			if (!mUserId2Map.containsKey(user.intValue())) {
				mUserNum++;
				mUserId2Map.put(user.intValue(), mUserNum);
			}
			
			mLineNum++;
			if (mLineNum % 50000 == 0)
				print(mLineNum + " lines read");
		}
		
		
		for(Integer item:items) {
			
			
			
			if (!mItemId2Map.containsKey(item.intValue())) {
				mItemNum++;
				mItemId2Map.put(item.intValue(), mItemNum);
			}
			
			mLineNum++;
			if (mLineNum % 50000 == 0)
				print(mLineNum + " lines read");
		}
		
	}
	

	
	
	protected void print(String out) {
		System.out.println(out);
	}

	@SuppressWarnings("unchecked")
	public void loadFile(String mTrainFileName, String mTestFileName,String separator) throws Exception {
		this.mTestFileName = mTestFileName;
		mSeparator = separator;
		
		print("------Mapping UserId and ItemId ...------");
		print("------reading train file ...------");
		mapping(mTrainFileName, separator);
		print("------read train file complete!------");

		print("------reading test file ...------");
		mapping(mTestFileName, separator);
		print("------read test file complete!------");
		print("------Mapping complete!------");

		
		p = new float[mUserNum + 1][dim];
		bu = new float[mUserNum + 1];
		for (int i = 1; i <= mUserNum; i++) {
			p[i] = new float[dim];
		}
		q = new float[mItemNum + 1][dim];
		for (int i = 1; i <= mItemNum; i++) {
			q[i] = new float[dim];
		}
		bi = new float[mItemNum + 1];
		mRateMatrix = new ArrayList[mUserNum + 1];
		for (int i = 1; i < mRateMatrix.length; i++)
			mRateMatrix[i] = new ArrayList<Node>();

		int userId, itemId, mLineNum = 0;
		float rate = 0;
		String mLine;
		BufferedReader br = new BufferedReader(new FileReader(new File(
				mTrainFileName)));
		while ((mLine = br.readLine()) != null) {
			String[] splits = mLine.split(separator);
			userId = Integer.valueOf(splits[0]);
			itemId = Integer.valueOf(splits[1]);
			if (isTranspose) {
				int temp = userId;
				userId = itemId;
				itemId = temp;
			}
			rate = Float.valueOf(splits[2]);
			mLineNum++;
			mRateMatrix[mUserId2Map.get(userId)].add(new Node(mItemId2Map
					.get(itemId), rate));
			if (mLineNum % 50000 == 0)
				print(mLineNum + " lines read");
			mean += rate;
			if (rate < mMinRate)
				mMinRate = rate;
			if (rate > mMaxRate)
				mMaxRate = rate;
		}
		mean /= mLineNum;
		init();
	}

	private void init() {
		
		for(int i = 1; i <= mUserNum; i++){
			bu[i]=0;
		}
		for (int i = 1; i <= mUserNum; i++)
			for (int j = 0; j < dim; j++)
				p[i][j] = (float) (Math.random() / 10);
		
		for(int i = 1; i <= mItemNum; i++){
			bi[i]=0;
		}
		for (int i = 1; i <= mItemNum; i++)
			for (int j = 0; j < dim; j++)
				q[i][j] = (float) (Math.random() / 10);
	}


	public void train(float gama, float lambda, int nIter) {
		print("------start training------");
		double Rmse = 0, mLastRmse = 100000;
		int nRateNum = 0;
		float rui = 0;
		for (int n = 1; n <= nIter; n++) {
			Rmse = 0;
			nRateNum = 0;
			for (int i = 1; i <= mUserNum; i++)
				for (int j = 0; j < mRateMatrix[i].size(); j++) {
					rui = mean
							+ bu[i]
							+ bi[mRateMatrix[i].get(j).getId()]
							+ mt.getInnerProduct(p[i], q[mRateMatrix[i].get(j)
									.getId()]);
					
					//log.info(rui);
					
					if (rui > mMaxRate)
						rui = mMaxRate;
					else if (rui < mMinRate)
						rui = mMinRate;
					
					float e = mRateMatrix[i].get(j).getRate() - rui;

					//log.info(e);
					
					bu[i] += gama * (e - lambda * bu[i]);
					bi[mRateMatrix[i].get(j).getId()] += gama
							* (e - lambda * bi[mRateMatrix[i].get(j).getId()]);
					for (int k = 0; k < dim; k++) {
						p[i][k] += gama
								* (e * q[mRateMatrix[i].get(j).getId()][k] - lambda
										* p[i][k]);
						q[mRateMatrix[i].get(j).getId()][k] += gama
								* (e * p[i][k] - lambda
										* q[mRateMatrix[i].get(j).getId()][k]);
					}
					Rmse += e * e;
					nRateNum++;
				}
			Rmse = Math.sqrt(Rmse / nRateNum);
			print("n = " + n + " Rmse = " + Rmse);
			
			mLastRmse = Rmse;
			gama *= 0.9;
		}
		print("------training complete!------");
	}


	public void predict(String mOutputFileName, String separator)
			throws Exception {
		print("------predicting------");
		int userId, itemId;
		float rate = 0;
		String mLine;
		double Rmse = 0;
		int nNum = 0;
		BufferedReader br = new BufferedReader(new FileReader(new File(
				mTestFileName)));
		BufferedWriter bw = null;
		if (!mOutputFileName.equals(""))
			bw = new BufferedWriter(new FileWriter(new File(mOutputFileName)));
		while ((mLine = br.readLine()) != null) {
			String[] splits = mLine.split(separator);
			userId = Integer.valueOf(splits[0]);
			itemId = Integer.valueOf(splits[1]);
			if (splits.length > 2)
				rate = Float.valueOf(splits[2]);
			if (isTranspose) {
				int temp = userId;
				userId = itemId;
				itemId = temp;
			}
			float rui = mean
					+ bu[mUserId2Map.get(userId)]
					+ bi[mItemId2Map.get(itemId)]
					+ mt.getInnerProduct(p[mUserId2Map.get(userId)],
							q[mItemId2Map.get(itemId)]);
			if (mOutputFileName.equals("")) {
				Rmse += (rate - rui) * (rate - rui);
				nNum++;
			} else {
				bw.write(userId + separator + itemId + separator + rui + "\n");
				bw.flush();
			}
		}
		print("test file Rmse = " + Math.sqrt(Rmse / nNum));
		br.close();
		if (bw != null)
			bw.close();
	}


	public void loadHisFile(String mHisFileName, String separator)
			throws Exception {

	}
	
	

	public void loadFileSQL(JdbcTemplate jdbc) throws Exception {
		// TODO Auto-generated method stub
		this.mTestFileName = "test.SQL ";
		//mSeparator = separator;
		
		print("------Mapping UserId and ItemId ...------");
		print("------reading train file ...------");
		String select_users_train="select distinct user_id from action_processed ";
		String select_items_train="select distinct item_id from action_processed ";
		String select_users_test="select distinct user_id from action_processed_test ";
		String select_items_test="select distinct item_id from action_processed_test ";
		String select_ratings_train="select user_id,item_id,rate from action_processed ";
		String select_ratings_test="select user_id,item_id,rate from action_processed_test ";
		
		List<Integer> items=null,users=null;
		
		users=jdbc.queryForList(select_users_train, Integer.class);
		items=jdbc.queryForList(select_items_train, Integer.class);
		
		mapping(users, items);
		print("------read train file complete!------");

		print("------reading test file ...------");
		
		users=jdbc.queryForList(select_users_test, Integer.class);
		items=jdbc.queryForList(select_items_test, Integer.class);

		mapping(users, items);
		print("------read test file complete!------");
		print("------Mapping complete!------");

		
		p = new float[mUserNum + 1][dim];
		bu = new float[mUserNum + 1];
		for (int i = 1; i <= mUserNum; i++) {
			p[i] = new float[dim];
		}
		q = new float[mItemNum + 1][dim];
		for (int i = 1; i <= mItemNum; i++) {
			q[i] = new float[dim];
		}
		
		bi = new float[mItemNum + 1];
		mRateMatrix = new ArrayList[mUserNum + 1];
		for (int i = 1; i < mRateMatrix.length; i++)
			mRateMatrix[i] = new ArrayList<Node>();

		int userId, itemId, mLineNum = 0;
		float rate = 0;
		
		List<Map<String,Object>> ratings=jdbc.queryForList(select_ratings_train);
		

		for(Map<String,Object> r:ratings){
			
			
			userId = ((Integer) r.get("user_id")).intValue();
			
			itemId = ((Integer) r.get("item_id")).intValue();
			if (isTranspose) {
				int temp = userId;
				userId = itemId;
				itemId = temp;
			}
			rate =((Double) r.get("rate")).floatValue();
			
			if(rate==0.0) rate=1.0f;
			
			mLineNum++;
			mRateMatrix[mUserId2Map.get(userId)].add(new Node(mItemId2Map
					.get(itemId), rate));
			if (mLineNum % 50000 == 0)
				print(mLineNum + " lines read");
			mean += rate;
			if (rate < mMinRate)
				mMinRate = rate;
			if (rate > mMaxRate)
				mMaxRate = rate;
		}
		mean /= mLineNum;
		
		log.error("mean="+mean);
		log.error("min="+mMinRate+"  "+"max="+mMaxRate);
		
		init();
		
	}

	public void loadHisFileSQL(JdbcTemplate jdbc) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void predict(JdbcTemplate jdbc) throws Exception {
		
		// TODO Auto-generated method stub
		print("------predicting------");
		int userId, itemId;
		float rate = 0;
		String mLine;
		double Rmse = 0;
		int nNum = 0;
		String select_ratings_test="select user_id,item_id,rate from action_processed_test ";
		
		List<Map<String,Object>> ratings=jdbc.queryForList(select_ratings_test);
		
		double cnt=ratings.size();
		int step=0;
		for(Map<String,Object> r:ratings) {
			
			if((++step)%1000==0){
				System.err.println(step/cnt);
			}
			userId = ((Integer) r.get("user_id")).intValue();
			
			itemId = ((Integer) r.get("item_id")).intValue();
			if (isTranspose) {
				int temp = userId;
				userId = itemId;
				itemId = temp;
			}
			rate =((Double) r.get("rate")).floatValue();

			if(rate==0.0) continue;
			
			float rui = mean
					+ bu[mUserId2Map.get(userId)]
					+ bi[mItemId2Map.get(itemId)]
					+ mt.getInnerProduct(p[mUserId2Map.get(userId)],
							q[mItemId2Map.get(itemId)]);
			
			if (rui > mMaxRate)
				rui = mMaxRate;
			else if (rui < mMinRate)
				rui = mMinRate;
			
			
			
			Rmse += (rate - rui) * (rate - rui);
			nNum++;
			
		}
		
		print("test file Rmse = " + Math.sqrt(Rmse / nNum));
		
		
	}

	public static void main(String [] args){
		
		ApplicationContext ctx=new ClassPathXmlApplicationContext("application-context.xml");
		JdbcTemplate jdbc=(JdbcTemplate)ctx.getBean("jdbcTemplate");
		
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

	
}