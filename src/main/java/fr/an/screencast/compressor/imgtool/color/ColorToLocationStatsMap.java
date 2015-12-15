package fr.an.screencast.compressor.imgtool.color;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.BasicStats;
import fr.an.screencast.compressor.utils.Rect;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;

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

        public int getValue() {
            return value;
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
    
    private Int2ReferenceOpenHashMap<ValueLocationStats> valueToLocationStats = new Int2ReferenceOpenHashMap<ValueLocationStats>();
    
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

    public void addPts(int frameIndex, Rect roi, BufferedImage img) {
        final int[] data = ImageRasterUtils.toInts(img);
        final int width = img.getWidth();
        int x = roi.fromX;
        int y = roi.fromY;
        int idx = y*width + x;
        final int incrIdxY = width + roi.fromX - roi.toX; 
        for(y = roi.fromY; y < roi.toY; y++,idx+=incrIdxY) {
            for(x = roi.fromX; x < roi.toX; x++,idx++) {
                int value = data[idx];
                addPt(frameIndex, x, y, value);
            }
        }
    }

    
    public static class MostUsedColorStats {
        ValueLocationStats firstColor;
        // ValueLocationStats secondColor;
    }
    public ValueLocationStats findMostUsedColor() {
        ValueLocationStats first = null;
        int currFirstCount = 0;
        for (ValueLocationStats e : valueToLocationStats.values()) {
            if (e.count > currFirstCount) {
                currFirstCount = e.count;
                first = e;
            }
        }
        return first;
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

    private static final NumberFormat FMT_DBL_1 = new DecimalFormat("##.#");

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ColorToLocationStatsMap [");
        if (! valueToLocationStats.isEmpty()) {
            sb.append("count:" + valueToLocationStats.size());
            Map<Integer,ValueLocationStats> valueStatsByCount = toValueStatsByCount();
    
            int sum = 0;
            int sumMax5 = 0;
            int i = 0;
            for(ValueLocationStats e : valueStatsByCount.values()) {
                sum += e.count;
                if (i < 10) {
                    sumMax5 += e.count;
                }
                i++;
            }
            if (sum == 0) sum = 1;
            double ratio5 = sumMax5 * 100.0 / sum;
            sb.append(" cumul5: " + sumMax5 + ": " + FMT_DBL_1.format(ratio5) + "%");
            sb.append(", colors by decr freq: ");            
            i = 0;
            for(ValueLocationStats e : valueStatsByCount.values()) {
                sb.append(e.count);
                double ratio = e.count * 100.0 / sum;
                sb.append(": " + FMT_DBL_1.format(ratio) + "%");
                sb.append(" x " + RGBUtils.toString(e.value));
                i++;
                if (i >= 10) {
                    sb.append(" ...");
                    break;
                }
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
}
