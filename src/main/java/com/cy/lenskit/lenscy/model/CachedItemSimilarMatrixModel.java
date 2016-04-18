package com.cy.lenskit.lenscy.model;

import java.util.Map;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.item.model.ItemItemBuildContext;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.knn.item.model.ItemItemModelBuilder;
import org.grouplens.lenskit.knn.item.model.NeighborIterationStrategy;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cy.lenskit.lenscy.util.LRUCache;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

@DefaultProvider(CachedItemItemModelBuilder.class)
@Shareable
public class CachedItemSimilarMatrixModel implements ItemItemModel {

	private static final Logger logger = LoggerFactory.getLogger(CachedItemSimilarMatrixModel.class);
	
	private final LongKeyDomain itemDomain;
	private final ItemSimilarity itemSimilarity;
    private final ItemItemBuildContext buildContext;
    private final Threshold threshold;
    private final NeighborIterationStrategy neighborStrategy;
    
    private LRUCache<Long,ImmutableSparseVector> neighbors;
    
    
    
	@Override
	public LongSortedSet getItemUniverse() {
		// TODO Auto-generated method stub
		return itemDomain.activeSetView();
	}
	
	
	
    public CachedItemSimilarMatrixModel(LongKeyDomain itemDomain,LRUCache<Long,ImmutableSparseVector> nbrs,
    		ItemSimilarity itemSimilarity,ItemItemBuildContext buildContext,
    		Threshold threshold,NeighborIterationStrategy neighborStrategy) {
    	
    	//the nbrs is initial empty
    	this.itemDomain = itemDomain;
        
        
    	this.buildContext=buildContext;
    	this.neighbors=nbrs;
    	this.itemSimilarity=itemSimilarity;
    	this.threshold=threshold;
    	this.neighborStrategy=neighborStrategy;
    	
    	logger.info("buildContext={} ,neighbors={} itemSimilarity={}   neighborStrategy={}",
    			buildContext,neighbors,itemSimilarity,neighborStrategy);
        
    }

    
    

	@Override
	public SparseVector getNeighbors(long itemId1) {
		// TODO Auto-generated method stub
		
		int idx = itemDomain.getIndex(itemId1);
        if (idx < 0) {
            return ImmutableSparseVector.empty();
        }
        

		SparseVector ret=neighbors.get(itemId1);
		if(null!=ret){
			//logger.error("====hit===={}",itemId1);
			return ret;
		}
		
		SparseVector vec1=null;
		
		
		vec1 = buildContext.itemVector(itemId1);
		

		//LongSortedSet items=itemDomain.domain();
		
		LongIterator itemIter = neighborStrategy.neighborIterator(buildContext, itemId1,
                false);
		
		MutableSparseVector msv=MutableSparseVector.create(itemDomain.domain());
		
		
        while (itemIter.hasNext()) {
            long itemId2 = itemIter.nextLong();
            if (itemId1 != itemId2) {
                SparseVector vec2 = buildContext.itemVector(itemId2);
                double sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);
                if (threshold.retain(sim)) {
                	msv.set(itemId2, sim);
                }
            }
        }
        
        ImmutableSparseVector isv=msv.freeze();
        
        neighbors.put(itemId1, isv);
		
		return isv;
	}

}
