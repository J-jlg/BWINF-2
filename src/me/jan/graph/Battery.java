/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan.graph;

import java.util.ArrayList;

public class Battery {
	
	private int x,y,energy,id,sccid,GeoGroup;
	private boolean isDone = false;
	private boolean endNode = false;
	private ArrayList<Integer> neighbors = new ArrayList<Integer>();
	
	public Battery(int x, int y, int energy, int id, int sscid, boolean isDone, int geoGroup) {
		this.setX(x);
		this.setY(y);
		this.setEnergy(energy);
		this.setId(id);
		this.setSccid(sscid);
		this.setDone(isDone);
		this.setGeoGroup(geoGroup);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getSccid() {
		return sccid;
	}

	public void setSccid(int sccid) {
		this.sccid = sccid;
	}

	public boolean isDone() {
		return isDone;
	}
	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public int getGeoGroup() {
		return GeoGroup;
	}

	public void setGeoGroup(int geoGroup) {
		GeoGroup = geoGroup;
	}

	public boolean isEndNode() {
		return endNode;
	}

	public void setEndNode(boolean endNode) {
		this.endNode = endNode;
	}

	public ArrayList<Integer> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(ArrayList<Integer> neighbors) {
		this.neighbors = neighbors;
	}
}
