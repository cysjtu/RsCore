package com.cy.lenskit.lenscy.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.aspectj.lang.JoinPoint;


public class LenskitAdvice implements InitializingBean{

	public static Logger log=LoggerFactory.getLogger(LenskitAdvice.class);
	
	
	@Autowired
	private JdbcTemplate jdbc;
	
	static{
		System.out.println("=========static===================");
		
		
	}

	public LenskitAdvice(){
		System.out.println("==============constructor==============");
	}
	
	
	public void BeforeAdvice(JoinPoint point){
		log.error("============================");
		System.err.println(point.getSignature().toShortString());
		log.error(point.getSignature().toShortString());
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("============================");
		//System.exit(0);
	}
}
