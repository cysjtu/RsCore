package com.cy.lenskit.lenscy.util;

import java.io.Serializable;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.vectors.SparseVector;

import com.google.common.base.Function;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class RatingVectorSummaryPublic implements UserHistorySummarizer, Serializable{
    private static final long serialVersionUID = 21241231212L;

    @Override
    public Class<? extends Event> eventTypeWanted() {
        return Rating.class;
    }

    @Override @Nonnull
    public SparseVector summarize(@Nonnull UserHistory<? extends Event> history) {
        return history.memoize(SummaryFunction.INSTANCE);
    }


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RatingVectorUserHistorySummarizer";
    }

    /**
     * All rating vector summarizers are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else {
            return getClass().equals(o.getClass());
        }
    }

    static enum SummaryFunction implements Function<UserHistory<? extends Event>, SparseVector> {
        INSTANCE;

        @Override
        @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
        public SparseVector apply(UserHistory<? extends Event> history) {
            if (history == null) {
                throw new NullPointerException("history is null");
            }
            return Ratings.userRatingVector(history.filter(Rating.class)).immutable();
        }
    }
    
}
