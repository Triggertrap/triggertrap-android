package com.triggertrap.outputs;

public interface IBeeper {

    public void play(long length);

    public void play(long length, long pauseLength);

    public void stop();
}
