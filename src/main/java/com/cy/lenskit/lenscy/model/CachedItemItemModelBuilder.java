package com.cy.lenskit.lenscy.model;

import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.item.ItemSimilarityThreshold;
import org.grouplens.lenskit.knn.item.ModelSize;
import org.grouplens.lenskit.knn.item.model.ItemItemBuildContext;
import org.grouplens.lenskit.knn.item.model.ItemItemModelBuilder;
import org.grouplens.lenskit.knn.item.model.NeighborIterationStrategy;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cy.lenskit.lenscy.util.LRUCache;

import it.unimi.dsi.fastutil.longs.LongSortedSet;



public class CachedItemItemModelBuilder implements Provider<CachedItemSimilarMatrixModel> {

	private static final Logger logger = LoggerFactory.getLogger(CachedItemItemModelBuilder.class);
	
    private final ItemSimilarity itemSimilarity;
    private final ItemItemBuildContext buildContext;
    private final Threshold threshold;
    private final NeighborIterationStrategy neighborStrategy;
    private final int modelSize;

    @Inject
    public CachedItemItemModelBuilder(
    		@Transient ItemSimilarity similarity,
            @Transient ItemItemBuildContext context,
            @Transient @ItemSimilarityThreshold Threshold thresh,
            @Transient NeighborIterationStrategy nbrStrat,
            @ModelSize int size) {
			itemSimilarity = similarity;
			buildContext = context;
			threshold = thresh;
			neighborStrategy = nbrStrat;
			modelSize = size;
    		}

    
	@Override
	public CachedItemSimilarMatrixModel get() {
		// TODO Auto-generated method stub
        logger.info("building item-item model for {} items", buildContext.getItems().size());
        logger.debug("using similarity function {}", itemSimilarity);
        logger.debug("similarity function is {}",
                     itemSimilarity.isSparse() ? "sparse" : "non-sparse");
        logger.debug("similarity function is {}",
                     itemSimilarity.isSymmetric() ? "symmetric" : "non-symmetric");

        LongSortedSet allItems = buildContext.getItems();

        
        LongKeyDomain itemDomain= LongKeyDomain.fromCollection(allItems, true);
        
        LRUCache<Long,ImmutableSparseVector> nbrs=new LRUCache<>(modelSize);
        logger.info("LRUCache  size ={}",modelSize);
        
        
		return new CachedItemSimilarMatrixModel(itemDomain, nbrs, itemSimilarity, buildContext, threshold, neighborStrategy);
		
	}

}
