package fr.an.screencast.player;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.utils.Dim;

public interface ScreenPlayerListener {

    public void onInit(Dim dim);

    public void onPlayerPlay();
    public void onPlayerPlayFastForward();

    public void onPlayerPaused();

    public void onPlayerStopped();

    public void newFrame();
    public void showNewImage(BufferedImage image);

    public void onPlayerReset();

    // ------------------------------------------------------------------------
    
    /**
     * 
     */
    public static class ScreenPlayerListenerAdapter implements ScreenPlayerListener {

        @Override
        public void onInit(Dim dim) {
        }

        @Override
        public void onPlayerPlay() {
        }

        @Override
        public void onPlayerPlayFastForward() {
        }

        @Override
        public void onPlayerPaused() {
        }

        @Override
        public void onPlayerStopped() {
        }

        @Override
        public void newFrame() {
        }

        @Override
        public void showNewImage(BufferedImage image) {
        }

        @Override
        public void onPlayerReset() {
        }
        
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * 
     */
    public static class ScreenPlayerListenerSupport implements ScreenPlayerListener {
        
        private List<ScreenPlayerListener> listeners = new ArrayList<ScreenPlayerListener>();
        

        public ScreenPlayerListenerSupport() {
        }

        public void addListener(ScreenPlayerListener listener) {
            listeners.add(listener);
        }
        public void removeListener(ScreenPlayerListener listener) {
            listeners.remove(listener);
        }
        
        public void onInit(Dim dim) {
            for(ScreenPlayerListener l : listeners) {
                l.onInit(dim);
            }
        }

        public void onPlayerPlay() {
            for(ScreenPlayerListener l : listeners) {
                l.onPlayerPlay();
            }
        }

        public void onPlayerPlayFastForward() {
            for(ScreenPlayerListener l : listeners) {
                l.onPlayerPlayFastForward();
            }
        }

        public void onPlayerPaused() {
            for(ScreenPlayerListener l : listeners) {
                l.onPlayerPaused();
            }
        }

        public void onPlayerStopped() {
            for(ScreenPlayerListener l : listeners) {
                l.onPlayerStopped();
            }
        }

        public void newFrame() {
            for(ScreenPlayerListener l : listeners) {
                l.newFrame();
            }
        }
        
        public void showNewImage(BufferedImage image) {
            for(ScreenPlayerListener l : listeners) {
                l.showNewImage(image);
            }
        }

        public void onPlayerReset() {
            for(ScreenPlayerListener l : listeners) {
                l.onPlayerReset();
            }
        }
    }
}
