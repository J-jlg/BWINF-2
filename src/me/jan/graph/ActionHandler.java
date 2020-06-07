/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan.graph;

import java.util.LinkedList;

public class ActionHandler {
	
	/**
	 * @param adj LinkedList - Kanten
	 * @param von Knoten an der Stelle [von] wird ges�ubert
	 */
	public static void deleteEdge(LinkedList<Kante>[] adj, int von) {
		adj[von].clear();
	}

	/**
	 * @param adj LinkedList - Kanten
	 * @param k Kante k wird hinzugef�gt
	 */
	public static void addEdge(LinkedList<Kante>[] adj, Kante k) {
		adj[k.getVon()].add(k);
	}
}
