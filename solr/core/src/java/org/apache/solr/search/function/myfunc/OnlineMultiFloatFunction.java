package org.apache.solr.search.function.myfunc;

import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.MultiFloatFunction;

public abstract class OnlineMultiFloatFunction extends MultiFloatFunction {
    public OnlineMultiFloatFunction(ValueSource[] sources) {
        super(sources);
    }

    protected boolean exists(int doc, FunctionValues[] valsArr) {
        return true;
    }
}