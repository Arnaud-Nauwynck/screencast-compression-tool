package fr.an.screencast.compressor.imgtool.color;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.BasicStats;

public class ColorToLocationStatsMap {

    public static class ValueLocationStats {
        private final int value;
        private int count;
        
        private final BasicStats xStats = new BasicStats(); 
        private final BasicStats yStats = new BasicStats();
        
        public ValueLocationStats(int value) {
            this.value = value;
        }

        public void add(int x, int y) {
            count++;
            xStats.add(x);
            yStats.add(y);
        }

        public int getCount() {
            return count;
        }

        public BasicStats getxStats() {
            return xStats;
        }

        public BasicStats getyStats() {
            return yStats;
        }
    }
    
    public static Comparator<ValueLocationStats> ValueLocationStatsCountComparator = new Comparator<ValueLocationStats>() {
        public int compare(ValueLocationStats o1, ValueLocationStats o2) {
            int res = 0;
            res = Integer.compare(o1.count, o2.count);
            if (res == 0) {
                res = Integer.compare(o1.value, o2.value);
            }
            return res;
        }
    };
    
    private Map<Integer,ValueLocationStats> valueToLocationStats = new HashMap<Integer,ValueLocationStats>();
    
    // ------------------------------------------------------------------------

    public ColorToLocationStatsMap() {
    }

    // ------------------------------------------------------------------------

    public void addPt(int frameIndex, int x, int y, int value) {
        ValueLocationStats tmpres = valueToLocationStats.get(value);
        if (tmpres == null) {
            tmpres = new ValueLocationStats(value);
            valueToLocationStats.put(value, tmpres);
        }
        tmpres.add(x, y);
    }

    public Map<Integer, ValueLocationStats> getValueToLocationStats() {
        return valueToLocationStats;
    }
    
    public void clear() {
        valueToLocationStats.clear();
    }

    public Map<Integer,ValueLocationStats> toValueStatsByCount() {
        TreeMap<Integer,ValueLocationStats> res = new TreeMap<Integer,ValueLocationStats>(Comparator.reverseOrder());
        for(ValueLocationStats e : valueToLocationStats.values()) {
            res.put(e.count, e);
        }
        return res;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Map<Integer,ValueLocationStats> valueStatsByCount = toValueStatsByCount();
        int i = 0;
        for(ValueLocationStats e : valueStatsByCount.values()) {
            sb.append(e.count + ":" + RGBUtils.toString(e.value));
            i++;
            if (i >= 5) {
                sb.append(" ...");
                break;
            }
            sb.append(", ");
        }
        return "ColorToLocationStatsMap [by decr count: " + sb + "]";
    }
    
    
}
