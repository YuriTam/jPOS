/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2017 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.util;

import org.HdrHistogram.Histogram;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Metrics implements Loggeable {
    private Histogram template;
    private Map<String,Histogram> metrics = new ConcurrentHashMap<>();

    public Metrics(Histogram template) {
        super();
        this.template = template;
    }

    public Map<String,Histogram> metrics() {
        return metrics.entrySet()
          .stream()
          .sorted(Map.Entry.comparingByKey())
          .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().copy()));
    }

    public Map<String,Histogram> metrics (String prefix) {
        return metrics.entrySet()
          .stream()
          .filter(e -> e.getKey().startsWith(prefix))
          .sorted(Map.Entry.comparingByKey())
          .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().copy()));
    }

    public void record(String name, long elapsed) {
        getHistogram(name).recordValue(elapsed);
    }

    private Histogram getHistogram (String p) {
        Histogram h = metrics.get(p);
        if (h == null) {
            Histogram hist = new Histogram(template);
            hist.setTag(p);
            metrics.putIfAbsent(p, hist);
            h = metrics.get(p);
        }
        return h;
    }


    public void dump (PrintStream ps, String indent) {
        metrics.entrySet()
          .stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(e -> dumpHistogram (ps, indent, e.getKey(), e.getValue().copy()));
    }

    private void dumpHistogram (PrintStream ps, String indent, String key, Histogram h) {
        ps.printf ("%s%s max=%d, min=%d, mean=%.4f 0.5%%=%d 99.5%%=%d, 99.9%%=%d, 99.99%%=%d%n",
          indent,
          key,
          h.getMaxValue(),
          h.getMinValue(),
          h.getMean(),
          h.getValueAtPercentile(0.5),
          h.getValueAtPercentile(99.5),
          h.getValueAtPercentile(99.9),
          h.getValueAtPercentile(99.99)
        );
    }
}
