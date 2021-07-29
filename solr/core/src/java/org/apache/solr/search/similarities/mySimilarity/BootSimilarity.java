package org.apache.solr.search.similarities.mySimilarity;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

import java.util.Collections;

public class BootSimilarity extends Similarity {

	@Override
	public long computeNorm(FieldInvertState state) {
		return 1;
	}

	@Override
	public SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics... termStats) {
		return new BoostSimWeight(queryBoost);
	}

	@Override
	public SimScorer simScorer(SimWeight weight, LeafReaderContext context) {
		BoostSimWeight boostSimWeight = (BoostSimWeight)weight;
		return new BoostSimScorer(boostSimWeight);
	}

	static class BoostSimWeight extends SimWeight{
		private final float boost;

		public BoostSimWeight(float boost){
			this.boost =boost;
		}
	}

	static class BoostSimScorer extends SimScorer {
		private final BoostSimWeight boostSimWeight;

		public BoostSimScorer(BoostSimWeight boostSimWeight){
			this.boostSimWeight = boostSimWeight;
		}

		@Override
		public float score(int doc, float freq) {
			return boostSimWeight.boost;
		}

		@Override
		public float computeSlopFactor(int distance) {
			return 1.0f;
		}

		@Override
		public Explanation explain(int doc, Explanation freq) {
			return Explanation.match(
					boostSimWeight.boost,
					"(boost is:" + boostSimWeight.boost + " )",
					Collections.singleton(freq));
		}

		@Override
		public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
			return PayloadHelper.decodeFloat(payload.bytes, payload.offset);
		}
	}

}
