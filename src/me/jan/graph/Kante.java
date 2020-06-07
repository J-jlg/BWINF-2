/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan.graph;

public class Kante {
	
	private int von, nach, kosten;
	private boolean isExpendable = false;//Ob man von v -> v' auch mehr als [kosten] Schritte nehmen kann
	private String path;
	private String pathException;
	
	public Kante(int von, int nach, int kosten, boolean isExpendable) {
		this.setVon(von);
		this.setNach(nach);
		this.setKosten(kosten);
		this.setExpendable(isExpendable);
	}

	public int getVon() {
		return von;
	}

	public void setVon(int von) {
		this.von = von;
	}

	public int getNach() {
		return nach;
	}

	public void setNach(int nach) {
		this.nach = nach;
	}

	public int getKosten() {
		return kosten;
	}

	public void setKosten(int kosten) {
		this.kosten = kosten;
	}

	public boolean isExpendable() {
		return isExpendable;
	}

	public void setExpendable(boolean isExpendable) {
		this.isExpendable = isExpendable;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPathException() {
		return pathException;
	}

	public void setPathException(String pathException) {
		this.pathException = pathException;
	}
}