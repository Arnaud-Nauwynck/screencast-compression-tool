package fr.an.screencast.compressor.utils;

/**
 * static utility methods for Rect
 */
public final class RectUtils {

    /* private to force all static */
    private RectUtils() {}
    
    /**
     * enclosing rectangle of 2 rectangles
     * <PRE>
     *    r1                  res
     * +------+             +-----------+
     * |      |  r2     =>  |           |
     * |    +-+----+        |           |
     * +----+-+    |        |           |
     *      |      |        |           |
     *      +------+        +-----------+
     *    
     * </PRE>
     * @param res optional already allocated result
     * @param r1
     * @param r2
     * @return
     */
    public static Rect enclosingRect(Rect res, Rect r1, Rect r2) {
        if (res == null) res = new Rect();
        res.fromX = Math.min(r1.fromX, r2.fromX);
        res.fromY = Math.min(r1.fromY, r2.fromY);
        res.toX = Math.max(r1.toX, r2.toX);
        res.toY = Math.max(r1.toY, r2.toY);
        return res;
    }
    
    /**
     * compute complement rectangles (maximum 4) within enclosing rectangle of 2 rectangles  
     * 
     * @param res optional already allocated (array length 3) result 
     * @param r1
     * @param r2
     * @return
     */
    public static Rect[] complementOfEnclosing(Rect[] res, Rect r1, Rect r2) {
        if (res == null || res.length < 3) {
            res = newArray(3);
        }
        if (res.length >= 4) {
            // dummy empty
            int maxX = Math.max(r1.toX,  r2.toX);
            int maxY = Math.max(r1.toY,  r2.toY);
            res[3].setPtToPt(maxX, maxY, maxX, maxY);
        }

        if (r2.fromX < r1.fromX) {
            // swap to have r1 on left of r2
            Rect tmp = r1; r1 = r2; r2 = tmp;
        }
        
        if (r1.toY <= r2.fromY) {
            // |
            // +
            // 
            //    +--
            intersectRectLeftAbove(res, r1, r2);
            
        } else if (r1.toY <= r2.toY) {
            // assert r1.toY >= r2.fromY
            //  |   +--
            //  +
            //
            //      +--
            if (r1.fromY < r2.fromY) {  
                intersectLeftAbove(res, r1, r2);
            } else { // r1.fromY >= r2.fromY
                res = intersectRectLeftInside(res, r1, r2);
            }
                
        } else { // r1.toY > r2.toY 
            // assert r1.toY >= r2.toY  
            //  |   +--
            //  |
            //  |   +--
            //  +
            if (r1.fromY <= r2.fromY) {
                intersectRightInside(res, r1, r2);
            } else if (r1.fromY < r2.toY) {
                intersectRectLeftIntersectBelow(res, r1, r2);
            } else {
                intersectRectLeftBelow(res, r1, r2);
            }
        }

        return res;
    }

    private static void intersectRectLeftAbove(Rect[] res, Rect r1, Rect r2) {
        if (r1.toX <= r2.toX) {
            //     r1                    
            //  +----+                +-----+--------+
            //  |    |            =>  |     | res0   |
            //  +----+                +-----+--------+
            //             r2         |     res1     |
            //            +----+      +--------+-----+
            //            |    |      |  res2  |     |
            //            +----+      +--------+-----+
            res[0].setPtToPt(r1.toX, r1.fromY, r2.toX, r1.toY);
            res[1].setPtToPt(r1.fromX, r1.toY, r2.toX, r2.fromY);
            res[2].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r2.toY);
        } else {
            //     r1                    
            //  +--------------+      +--------------+
            //  |              |  =>  |              |
            //  +--------------+      +--------------+
            //          r2            |     res0     |
            //       +----+           +----+----+----+
            //       |    |           |res1|    |res2|
            //       +----+           +----+----+----+
            res[0].setPtToPt(r1.fromX, r1.toY, r1.toX, r2.fromY);
            res[1].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r2.toY);
            res[2].setPtToPt(r2.toX, r2.fromY, r1.toX, r2.toY);
        }
    }

    private static void intersectRectLeftBelow(Rect[] res, Rect r1, Rect r2) {
        if (r1.toX <= r2.toX) {
            //             r2                    
            //            +----+      +---------+----+
            //            |    |  =>  |res0     |    |
            //            +----+      +---------+----+
            //   r1                   |    res1      |
            //  +----+                +----+---------+
            //  |    |                |    |   res2  |
            //  +----+                +----+---------+
            res[0].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r2.toY);
            res[1].setPtToPt(r1.fromX, r2.toY, r2.toX, r1.fromY);
            res[2].setPtToPt(r1.toX, r1.fromY, r2.toX, r1.toY);
        } else { // (r1.toX > r2.toX
            //        r2                    
            //       +----+          +----+-----+----+
            //       |    |      =>  |res0|     |res1|
            //       +----+          +----+-----+----+
            //   r1                  |    res2       |
            //  +--------------+     +---------------+
            //  |              |     |               |
            //  +--------------+     +---------------+
            res[0].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r2.toY);
            res[1].setPtToPt(r2.toX, r2.fromY, r1.toX, r2.toY);
            res[2].setPtToPt(r1.fromX, r2.toY, r1.toX, r1.fromY);
        }
    }

    private static void intersectRightInside(Rect[] res, Rect r1, Rect r2) {
        if (r1.toX <= r2.fromX) {
            //     r1                    
            //  +----+                +----+---------+
            //  |    |     r2     =>  |    | res0    |
            //  |    |    +----+      |    +----+----+
            //  |    |    |    |      |    |res1|    |
            //  |    |    |    |      |    +----+----+
            //  |    |    +----+      |    | res2    |
            //  +----+                +----+---------+
            res[0].setPtToPt(r1.toX, r1.fromY, r2.toX, r2.fromY);
            res[1].setPtToPt(r1.toX, r2.fromY, r2.fromX, r2.toY);
            res[2].setPtToPt(r1.toX, r2.toY, r2.toX, r1.toY);

        } else if (r1.toX < r2.toX) { 
            //     r1                    
            //  +----+               +----+---------+
            //  |    |  r2       =>  |    | res0    |
            //  | +--+--------+      |    +---------+
            //  | |  |        |      |              |
            //  | |  |        |      |    +---------+
            //  | +--+--------+      |    | res1    |
            //  +----+               +----+---------+
            res[0].setPtToPt(r1.toX, r1.fromY, r2.toX, r2.fromY);
            res[1].setPtToPt(r1.toX, r2.toY, r2.toX, r1.toY);
            
            res[2].setPtToPt(r2.toX, r1.toY, r2.toX, r1.toY); // dummy empty
        } else {
            //     r1                    
            //  +-------------+     +--------------+
            //  |  r2         | =>  |              |
            //  | +---+       |     |              |
            //  | |   |       |     |              |
            //  | +---+       |     |              |
            //  +-------------+     +--------------+
            res[0].setPtToPt(r1.toX, r1.toY, r1.toX, r1.toY); // dummy empty
            res[1].setPtToPt(r1.toX, r1.toY, r1.toX, r1.toY); // dummy empty                
            res[2].setPtToPt(r1.toX, r1.toY, r1.toX, r1.toY); // dummy empty
        }
    }

    private static void intersectRectLeftIntersectBelow(Rect[] res, Rect r1, Rect r2) {
        if (r1.toX < r2.fromX) {
            //             r2                    
            //            +----+      +---------+----+
            //    r1      |    |  =>  |res0     |    |
            //  +-----+   |    |      +----+----+    |
            //  |     |   |    |      |    |res1|    |
            //  |     |   +----+      |    +----+----+
            //  |     |               |    |   res2  |
            //  +-----+               +----+---------+
            res[0].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r1.fromY);
            res[1].setPtToPt(r1.toX, r1.fromY, r2.fromX, r2.toY);
            res[2].setPtToPt(r1.toX, r2.toY, r2.toX, r1.toY);
            
        } else if (r1.toX < r2.toX) {
            //         r2
            //       +------+       +----+------+   
            //    r1 |      |       |res0|      |
            //  +----+-+    |       +----+      |
            //  |    | |    |   =>  |           |
            //  |    +-+----+       |      +----+
            //  |      |            |      |res1|
            //  +------+            +------+----+
            res[0].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r1.fromY);
            res[1].setPtToPt(r1.toX, r2.toY, r2.toX, r1.toY);
            
            res[2].setPtToPt(r2.toX, r1.toY, r2.toX, r1.toY); // dummy empty
            
        } else {
            //         r2
            //       +---+         +----+---+----+   
            //    r1 |   |         |res0|   |res1|
            //  +----+---+---+     +----+   +----+
            //  |    |   |   | =>  |             |
            //  |    +---+   |     |             |
            //  +------------+     +-------------+
            res[0].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r1.fromY);
            res[1].setPtToPt(r2.toX, r2.fromY, r1.toX, r1.fromY);
            
            res[2].setPtToPt(r1.toX, r1.toY, r1.toX, r1.toY); // dummy empty
        }
    }

    private static void intersectLeftAbove(Rect[] res, Rect r1, Rect r2) {
        if (r1.toX < r2.fromX) {
            //     r1                    
            //  +----+                +-----+---------+
            //  |    |     r2     =>  |     | res0    |
            //  |    |    +----+      |     +----+----+
            //  |    |    |    |      |     |res1|    |
            //  +----+    |    |      +-----+----+    |
            //            |    |      |  res2    |    |
            //            +----+      +----------+----+
            res[0].setPtToPt(r1.toX, r1.fromY, r2.toX, r2.fromY);
            res[1].setPtToPt(r1.toX, r2.fromY, r2.fromX, r1.toY);
            res[2].setPtToPt(r1.fromX, r1.toY, r2.fromX, r2.toY);
        } else if (r1.toX <= r2.toX) { // && r1.toX >= r2.fromX            
            //     r1                  res
            //  +------+             +------+----+
            //  |      |  r2     =>  |      |res0|
            //  |    +-+----+        |      +----+
            //  +----+-+    |        +----+      |
            //       |      |        |res1|      |
            //       +------+        +----+------+
            res[0].setPtToPt(r1.toX, r1.fromY, r2.toX, r2.fromY);
            res[1].setPtToPt(r1.fromX, r1.toY, r2.fromX, r2.toY);
            
            res[2].setPtToPt(r2.toX, r2.toY, r2.toX, r2.toY); // dummy empty
            
        } else {
            //    r1
            //  +------------+     +-------------+
            //  |      r2    | =>  |             |
            //  |    +---+   |     |             |
            //  +----+---+---+     +-------------+
            //       |   |         |res0|   |res1|
            //       +---+         +----+---+----+   
            res[0].setPtToPt(r1.fromX, r1.toY, r2.fromX, r2.toY);
            res[1].setPtToPt(r2.toX, r1.toY, r1.toX, r2.toY);
            
            res[2].setPtToPt(r1.toX, r2.toY, r1.toX, r2.toY); // dummy empty
        }
    }

    private static Rect[] intersectRectLeftInside(Rect[] res, Rect r1, Rect r2) {
        if (r1.toX <= r2.fromX) {
            //               r2                    
            //            +----+      +---------+----+
            //   r1       |    |  =>  | res0    |    |
            //  +----+    |    |      +----+----+    |
            //  |    |    |    |      |    |res1|    |
            //  +----+    |    |      +----+----+    |
            //            +----+      | res2    |    |
            //                        +---------+----+
            res[0].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r1.fromY);
            res[1].setPtToPt(r1.toX, r1.fromY, r2.fromX, r1.toY);
            res[2].setPtToPt(r1.fromX, r1.toY, r2.fromX, r2.toY);
            
        } else if (r1.toX < r2.toX) { // && r1.toX >= r2.fromX
            if (r1.fromX < r2.fromX) {
                //               r2                    
                //            +----+      +---------+----+
                //   r1       |    |  =>  | res0    |    |
                //  +-----------+  |      +---------+    |
                //  |         | |  |      |              |
                //  +-----------+  |      +---------+    |
                //            +----+      | res1    |    |
                //                        +---------+----+
                res[0].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r1.fromY);
                res[1].setPtToPt(r1.fromX, r1.toY, r2.fromX, r2.toY);
                
                res[2].setPtToPt(r2.toX, r2.toY, r2.toX, r2.toY); // dummy empty
            } else {
                // degenerated: r1 inside & tangent to r2
                //               r2                    
                //  +--------------+      +--------------+
                //  | r1           |  =>  |              |
                //  +------+       |      +---------+    |
                //  +------+       |      +---------+    |
                //  |              |      |              |
                //  +--------------+      +--------------+
                res[0].setPtToPt(r2.toX, r2.toY, r2.toX, r2.toY); // dummy empty
                res[1].setPtToPt(r2.toX, r2.toY, r2.toX, r2.toY); // dummy empty
                res[2].setPtToPt(r2.toX, r2.toY, r2.toX, r2.toY); // dummy empty
            }
        } else {
            //         r2                    
            //        +----+      +---------+----+--+
            //   r1   |    |  =>  | res0    |    |r1|
            //  +-----+----+-+    +---------+    +--+
            //  |     |    | |    |                 |
            //  +-----+----+-+    +---------+    +--+
            //        +----+      | res2    |    |r3|
            //                    +---------+----+--+
            if (res == null || res.length < 4) {
                res = newArray(4);
            }
            res[0].setPtToPt(r1.fromX, r2.fromY, r2.fromX, r1.fromY);
            res[1].setPtToPt(r2.toX, r2.fromY, r1.toX, r1.fromY);
            res[2].setPtToPt(r1.fromX, r1.toY, r2.fromX, r2.toY);
            res[3].setPtToPt(r2.toX, r1.toY, r1.toX, r2.toY);
        }
        return res;
    }
    
    public static Rect[] newArray(int len) {
        Rect[] res = new Rect[len];
        for(int i = 0; i < len; i++) {
            res[i] = new Rect();
        }
        return res;
    }

    /**
     * intersection of rect
     * @param res optional already allocated result
     * @param r1
     * @param r2
     */
    public static Rect intersectRect(Rect res, Rect r1, Rect r2) {
        if (res == null) res = new Rect();
        int fromX = Math.max(r1.fromX, r2.fromX);
        int fromY = Math.max(r1.fromY, r2.fromY);
        int toX = Math.min(r1.toX, r2.toX);
        int toY = Math.min(r1.toY, r2.toY);
        if (toX > fromX && toY > fromY) {
            res.setPtToPt(fromX, fromY, toX, toY);
        } else {
            // empty
            res.setPtToPt(toX, toY, toX, toY);
            // return null ??
        }
        return res;
    }

}
