package dev.byblos.model;

import com.google.common.collect.AbstractIterator;

import static com.google.common.base.Preconditions.checkArgument;

/** TimeSeries can be lazy or eager. By default manipulations are done as a view over another
* time series. This view can be materialized for a given range by calling the bounded method.
 */
public interface TimeSeq {

    long step();

    double get(long timestamp);

    default Iterable<Double> range(long s, long e) {
        checkArgument(s <= e, "start must be <= end");
        var end = e / step() * step();
        return () -> new AbstractIterator<Double>() {
            private long t = s / step() * step();

            @Override
            protected Double computeNext() {
                if (t < end) {
                    var v = get(t);
                    t += step();
                    return v;
                }
                return endOfData();
            }
        };
    }

    default ArrayTimeSeq bounded(long s, long e) {
        checkArgument(s <= e, "start must be <= end");
        var end = e / step() * step();
        var start = s / step() * step();
        var length = (int)((end - start) / step());
        var data = new double[length];
        var i = 0;
        for (var v: range(start, end)) {
            data[i] = v;
            i += 1;
        }
        return new ArrayTimeSeq(start, step(), data);
    }
}
