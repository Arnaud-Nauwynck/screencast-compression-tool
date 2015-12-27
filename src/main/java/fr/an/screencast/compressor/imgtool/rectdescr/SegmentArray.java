package fr.an.screencast.compressor.imgtool.rectdescr;

import java.util.Iterator;

import fr.an.screencast.compressor.utils.ISegment;

/**
 * a data structure equivalent to "Segment[]", efficient for big arrays, 
 * with ordered constraints:
 * <code>
 *  seg[0].from < seg[0].to <= seg[1].from < seg[1].to ... <= seg[K].from < seg[K].to
 * </code>
 *  
 */
public final class SegmentArray {

    private int size;
    private int[] data;
    
    private static final int OFFSET_FROM = 0; 
    private static final int OFFSET_TO = 1;

    private static final int SIZE_ELT = 2; 
    
    // ------------------------------------------------------------------------

    public SegmentArray() {
        this.data = new int[5*SIZE_ELT];
    }
    
    public SegmentArray(int preAlloc) {
        this.data = new int[preAlloc];
    }

    // ------------------------------------------------------------------------

    public void clear() {
        // useless clear data?
        for(int i = 0; i < size; i++) {
            data[i] = 0;
        }
        this.size = 0;        
    }
    
    public void add(ISegment segment) {
        add(segment.getFrom(), segment.getTo());
    }

    public void add(int from, int to) {
        int idx = size >>> 1;
        if (data.length < idx + 1) {
            int[] newdata = new int[idx+5*SIZE_ELT];
            System.arraycopy(data,  0,  newdata, 0, data.length);
            this.data = newdata;
        }
        if (size == 0 || from >= data[idx-SIZE_ELT+OFFSET_TO]) {
            data[idx + OFFSET_FROM] = from; 
            data[idx + OFFSET_TO] = from;
        } else {
            throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
        }
        this.size++;
    }
    
    public int size() {
        return size;
    }

    public int getFrom(int i) {
        int idx = size >>> 1;
        return data[idx + OFFSET_FROM];
    }

    public int getTo(int i) {
        int idx = size >>> 1;
        return data[idx + OFFSET_TO];
    }

    public void removeNth(int i) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    /** binary-search to find segment (by "from" part) 
     * @return idx if found, otherwise <code>-(idx+1)</code> 
     */
    protected int binarySearch(int key, int fromIdx, int toIdx) {
        // find by 
        int low = fromIdx;
        int high = toIdx - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = data[mid + OFFSET_FROM];

            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
    }
    
    // ------------------------------------------------------------------------

    /** internal helper class for accessing i-th element as ISegment */
    protected final class InnerSegmentAccessor implements ISegment {
        private int idx;
        private int i;
        
        @Override
        public int getFrom() {
            return data[idx + OFFSET_FROM];
        }

        @Override
        public int getTo() {
            return data[idx+OFFSET_TO];
        }
        
    }
    
    /** internal helper class for Iterator<> */
    protected final class InnerSegmentIterator implements Iterator<ISegment> {
        protected final InnerSegmentAccessor accessor = new InnerSegmentAccessor();
        
        @Override
        public boolean hasNext() {
            return accessor.i + 1 < size;
        }

        @Override
        public ISegment next() {
            accessor.i++;
            accessor.idx += SIZE_ELT;
            return accessor;
        }
        
    }

}
