package fr.an.screencast.compressor.utils;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Test;

public class RectUtilsTest {

    @Test
    public void testComplementOfEnclosing() throws InterruptedException {
        // Prepare
        Rect area = Rect.newPtToPt(0, 0, 1000, 1000);
        Rect r1 = new Rect();
        Rect r2 = new Rect();
        Rect[] res = RectUtils.newArray(4);
        Rect enclosingRect = new Rect();
        Rect rIntersect = new Rect();
        
        boolean debugPaint = true;
        JFrame frame = new JFrame();
        Canvas canvas = new Canvas() {
            private static final long serialVersionUID = 1L;
            public void paint(Graphics g2d) {
                g2d.setColor(Color.GRAY);
                r1.graphicsFillRect(g2d);
                r2.graphicsFillRect(g2d);

                g2d.setColor(Color.BLACK);
                r1.graphicsDrawRect(g2d);
                r2.graphicsDrawRect(g2d);
                
                g2d.setColor(Color.BLUE);
                enclosingRect.graphicsDrawRectDilate(g2d, 1);
                
                g2d.setColor(Color.RED);
                res[0].graphicsDrawRectErode(g2d, 1);
                g2d.setColor(Color.ORANGE);
                res[1].graphicsDrawRectErode(g2d, 1);
                if (res[2].isNotEmpty()) {
                    g2d.setColor(Color.YELLOW);
                    res[2].graphicsDrawRectErode(g2d, 1);
                }
                if (res[3].isNotEmpty()) {
                    g2d.setColor(Color.GREEN);
                    res[3].graphicsDrawRectErode(g2d, 1);
                }
            }
        };
        if (debugPaint) {
            canvas.setPreferredSize(new Dimension(area.getWidth(), area.getHeight()));
            frame.getContentPane().add(canvas);
            frame.pack();
            frame.setVisible(true);
        }
        
        Random rand = new Random(0);
        for (int i = 0; i < 2000; i++) {
            // Prepare
            setRandRect(r1, rand, area);
            setRandRect(r2, rand, area);
            // Perform
            RectUtils.complementOfEnclosing(res, r1, r2);
            // Post-check
            RectUtils.enclosingRect(enclosingRect, r1, r2);
            RectUtils.intersectRect(rIntersect, r1, r2);
            // only check sum of area
            int expectedArea = enclosingRect.getArea();
            int sumArea = r1.getArea() + r2.getArea() - rIntersect.getArea()
                + res[0].getArea() + res[1].getArea() + res[2].getArea() + res[3].getArea();
            if (expectedArea != sumArea) {
                if (debugPaint) {
                    canvas.repaint();
                    Thread.sleep(100);
                }
                // redo for debug
                Rect leftRect = r1, rightRect = r2;
                if (r2.fromX < r1.fromX) {
                    // swap to have r1 on left of r2
                    Rect tmp = leftRect; leftRect = rightRect; rightRect = tmp;
                }
                System.out.println("r1: " + leftRect);
                System.out.println("r2: " + rightRect);
                RectUtils.enclosingRect(enclosingRect, r1, r2);
                System.out.println("enclosing: " + enclosingRect);
                RectUtils.complementOfEnclosing(res, r1, r2);

            }
            Assert.assertEquals(expectedArea, sumArea);
        }
        
        if (debugPaint) {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void setRandRect(Rect r, Random rand, Rect area) {
        int fromX = area.fromX + rand.nextInt(area.toX - area.fromX);
        int toX = fromX + rand.nextInt(area.toX - fromX);
        if (toX < fromX) {
            throw new IllegalStateException("Failed");
        }
        int fromY = area.fromY + rand.nextInt(area.toY - area.fromY);
        int toY = fromY + rand.nextInt(area.toY - fromY);
        if (toY < fromY) {
            throw new IllegalStateException("Failed");
        }
        r.setPtToPt(fromX, fromY, toX, toY);
    }
    
}
