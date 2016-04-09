package com.cy.lenskit.lenscy;

import org.springframework.jdbc.core.JdbcTemplate;

import com.mysql.jdbc.Connection;

public interface Trainer {
	
	void loadFile(String mTrainFileName, String mTestFileName, String separator)
			throws Exception;

	
	void loadHisFile(String mHisFileName, String separator) throws Exception;

	void loadFileSQL(JdbcTemplate jdbc)
			throws Exception;

	
	void loadHisFileSQL(JdbcTemplate jdbc) throws Exception;

	
	void train(float gama, float alpha, int nIter);

	void predict(String mOutputFileName, String separator) throws Exception;
	
	void predict(JdbcTemplate out_jdbc) throws Exception;

	
}