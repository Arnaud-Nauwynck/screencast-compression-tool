package fr.an.screencast.player;

import java.awt.Image;

import fr.an.screencast.compressor.utils.Dim;

public interface ScreenPlayerListener {

    public void onInit(Dim dim);

    public void onPlayerPlay();
    public void onPlayerPlayFastForward();

    public void onPlayerPaused();

    public void onPlayerStopped();

    public void newFrame();
    public void showNewImage(Image image);

    public void onPlayerReset();


}
