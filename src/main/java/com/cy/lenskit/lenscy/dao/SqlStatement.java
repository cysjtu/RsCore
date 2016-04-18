package com.cy.lenskit.lenscy.dao;

import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.sql.SQLStatementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatement implements SQLStatementFactory{

	public static Logger log=LoggerFactory.getLogger(SqlStatement.class);
	
	private String table;
	private String user;
	private String item;
	private String rate;
	private String time;
	
	
	
	
	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	
	
	
	
	
	
	
	
	//interface
	public String prepareUsers() {
		// TODO Auto-generated method stub
		return String.format("SELECT DISTINCT %s FROM %s ", user, table);
		
	}

	public String prepareItems() {
		// TODO Auto-generated method stub
		return String.format("SELECT DISTINCT %s FROM %s", item, table);
	}

	public String prepareEvents(SortOrder order) {
		// TODO Auto-generated method stub
		StringBuilder query=new StringBuilder();
		
        query.append("SELECT ");
        query.append(user);
        query.append(", ");
        query.append(item);
        query.append(", ");
        query.append(rate);
        if (time != null) {
            query.append(", ");
            query.append(time);
        }
        query.append(" FROM ");
        query.append(table);
        
        //add order 
        switch (order) {
        case ANY:
            break;
        case ITEM:
            query.append(" ORDER BY ");
            query.append(item);
            if (time != null) {
                query.append(", ");
                query.append(time);
            }
            break;
        case USER:
            query.append(" ORDER BY ").append(user);
            if (time != null) {
                query.append(", ");
                query.append(time);
            }
            break;
        case TIMESTAMP:
            /* If we don't have timestamps, we return in any order. */
            if (time != null) {
                query.append(" ORDER BY ").append(time);
            }
            break;
        default:
            throw new IllegalArgumentException("unknown sort order " + order);
        }
        
        
        String ret=query.toString();
        log.error(ret);
        return ret;
        

	}

	public String prepareUserEvents() {
		// TODO Auto-generated method stub
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT ");
        query.append(user);
        query.append(", ");
        query.append(item);
        query.append(", ");
        query.append(rate);
        if (time != null) {
            query.append(", ");
            query.append(time);
        }
        query.append(" FROM ");
        query.append(table);
       
        query.append(" where ").append(user).append(" = ?");
       
        log.error("User rating query: {}", query);
        return query.toString();
	}
	
	

	public String prepareItemEvents() {
		// TODO Auto-generated method stub
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(user);
        query.append(", ");
        query.append(item);
        query.append(", ");
        query.append(rate);
        if (time != null) {
            query.append(", ");
            query.append(time);
        }
        query.append(" FROM ");
        query.append(table);
        query.append(" where ").append(item).append(" = ?");
       
        log.warn("Item rating query: {}", query);
        return query.toString();

	}

	public String prepareItemUsers() {
		// TODO Auto-generated method stub
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT ").append(user)
             .append(" FROM ").append(table)
             .append(" WHERE ").append(item).append(" = ?");
       
        log.warn("Item user query: {}", query);
        return query.toString();

	}

}
