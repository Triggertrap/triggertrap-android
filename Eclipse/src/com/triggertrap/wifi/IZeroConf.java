package com.triggertrap.wifi;

import java.util.ArrayList;

public interface IZeroConf {

	public void watch();
	public void unwatch();
	public void registerMaster();
	public void unregisterMaster();
	public void disconnectSlaveFromMaster(String uniqueSlaveName);
	public ArrayList<TTSlaveInfo> getConnectedSlaves();
	public void close();
}
