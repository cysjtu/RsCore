package com.cy.lenskit.lenscy;


import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;
import org.grouplens.lenskit.data.sql.SQLStatementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ml_1m_Statement implements SQLStatementFactory {

	
	
	
    private static final Logger logger =
            LoggerFactory.getLogger(ml_1m_Statement.class);
    
    @Nonnull
    private String tableName = "ml_1m";
    
    @Nonnull
    private String userColumn = "user";
    @Nonnull
    private String itemColumn = "item";
    
    @Nonnull
    private String ratingColumn = "rating";
    
    @Nullable
    private String timestampColumn = null;

  
    
   
	/**
     * Get the name of the rating table.
     *
     * @return The rating table name.
     */
    @Nonnull
    public String getTableName() {
        return tableName;
    }

    /**
     * Set the name of the rating table.
     *
     * @param name The name of the rating table.  This table name is used without escaping to build
     *             SQL queries, so include whatever escaping or quotations are needed to make the
     *             name valid in the backing DBMS in the name here.
     */
    public void setTableName(@Nonnull String name) {
        tableName = name;
    }

    /**
     * Get the name of the user ID column in the rating table.
     *
     * @return The user column name.
     */
    @Nonnull
    public String getUserColumn() {
        return userColumn;
    }

    /**
     * Set the name of the user ID column in the rating table.
     *
     * @param col The name of the user column.  This column name is used without escaping to build
     *            SQL queries, so include whatever escaping or quotations are needed to make the
     *            name valid in the backing DBMS in the name here.
     */
    public void setUserColumn(@Nonnull String col) {
        userColumn = col;
    }

    /**
     * Get the name of the item ID column in the rating table.
     *
     * @return The item column name.
     */
    @Nonnull
    public String getItemColumn() {
        return itemColumn;
    }

    /**
     * Set the name of the item ID column in the rating table.
     *
     * @param col The name of the item column.  This column name is used without escaping to build
     *            SQL queries, so include whatever escaping or quotations are needed to make the
     *            name valid in the backing DBMS in the name here.
     */
    public void setItemColumn(@Nonnull String col) {
        itemColumn = col;
    }

    /**
     * Get the name of the rating column in the rating table.
     *
     * @return The rating column name.
     */
    @Nonnull
    public String getRatingColumn() {
        return ratingColumn;
    }

    /**
     * Set the name of the rating column in the rating table.
     *
     * @param col The name of the rating column.  This column name is used without escaping to build
     *            SQL queries, so include whatever escaping or quotations are needed to make the
     *            name valid in the backing DBMS in the name here.
     */
    public void setRatingColumn(@Nonnull String col) {
        ratingColumn = col;
    }

    /**
     * Get the name of the timestamp column in the rating table (or {@code null} if there is no
     * timestamp column).
     *
     * @return The timestamp column name, or {@code null} if no timestamp is used.
     */
    @Nullable
    public String getTimestampColumn() {
        return timestampColumn;
    }

    /**
     * Set the name of the timestamp column in the rating table. Set to {@code null} if there is no
     * timestamp column.
     *
     * @param col The name of the timestamp column, or {@code null}.  This column name is used
     *            without escaping to build SQL queries, so include whatever escaping or quotations
     *            are needed to make the name valid in the backing DBMS in the name here.
     */
    public void setTimestampColumn(@Nullable String col) {
        timestampColumn = col;
    }


    /**
     * Add the SELECT and FROM clauses to the query.
     *
     * @param query The query accumulator.
     */
    protected void rqAddSelectFrom(StringBuilder query) {
        query.append("SELECT ");
        query.append(userColumn);
        query.append(", ");
        query.append(itemColumn);
        query.append(", ");
        query.append(ratingColumn);
        if (timestampColumn != null) {
            query.append(", ");
            query.append(timestampColumn);
        }
        query.append(" FROM ");
        query.append(tableName);
    }

    /**
     * Add an ORDER BY clause to a query.
     *
     * @param query The query accumulator
     * @param order The sort order.
     */
    protected void rqAddOrder(StringBuilder query, SortOrder order) {
        switch (order) {
        case ANY:
            break;
        case ITEM:
            query.append(" ORDER BY ");
            query.append(itemColumn);
            if (timestampColumn != null) {
                query.append(", ");
                query.append(timestampColumn);
            }
            break;
        case USER:
            query.append(" ORDER BY ").append(userColumn);
            if (timestampColumn != null) {
                query.append(", ");
                query.append(timestampColumn);
            }
            break;
        case TIMESTAMP:
            /* If we don't have timestamps, we return in any order. */
            if (timestampColumn != null) {
                query.append(" ORDER BY ").append(timestampColumn);
            }
            break;
        default:
            throw new IllegalArgumentException("unknown sort order " + order);
        }
    }

    /**
     * Finish a query (append a semicolon).
     *
     * @param query The query accumulator
     */
    protected void rqFinish(StringBuilder query) {
    }

	public String prepareUsers() {
		// TODO Auto-generated method stub
		return String.format("SELECT DISTINCT %s FROM %s ", userColumn, tableName);
	}

	public String prepareItems() {
		// TODO Auto-generated method stub
		return String.format("SELECT DISTINCT %s FROM %s", itemColumn, tableName);
	}

	public String prepareEvents(SortOrder order) {
		// TODO Auto-generated method stub
        StringBuilder query = new StringBuilder();
        rqAddSelectFrom(query);
        rqAddOrder(query, order);
        rqFinish(query);
        logger.debug("Rating query: {}", query);
        return query.toString();

	}

	public String prepareUserEvents() {
		// TODO Auto-generated method stub
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT ");
        query.append(userColumn);
        query.append(", ");
        query.append(itemColumn);
        query.append(", ");
        query.append(ratingColumn);
        if (timestampColumn != null) {
            query.append(", ");
            query.append(timestampColumn);
        }
        query.append(" FROM ");
        query.append(tableName);
       
        query.append(" where ").append(userColumn).append(" = ?");
        rqFinish(query);
        logger.debug("User rating query: {}", query);
        return query.toString();

	}

	public String prepareItemEvents() {
		// TODO Auto-generated method stub
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(userColumn);
        query.append(", ");
        query.append(itemColumn);
        query.append(", ");
        query.append(ratingColumn);
        if (timestampColumn != null) {
            query.append(", ");
            query.append(timestampColumn);
        }
        query.append(" FROM ");
        query.append(tableName);
        query.append(" where ").append(itemColumn).append(" = ?");
        rqFinish(query);
        logger.debug("Item rating query: {}", query);
        return query.toString();

	}
	
	

	public String prepareItemUsers() {
		// TODO Auto-generated method stub
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT ").append(userColumn)
             .append(" FROM ").append(tableName)
             .append(" WHERE ").append(itemColumn).append(" = ?");
        rqFinish(query);
        logger.debug("Item user query: {}", query);
        return query.toString();

	}

  



}

