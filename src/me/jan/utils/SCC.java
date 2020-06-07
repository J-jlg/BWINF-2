/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import me.jan.Stromrallye;
import me.jan.graph.Battery;
import me.jan.graph.Kante;

public class SCC {

	static int index;
	static int lowlink[];
	static int indexV[];
	static boolean onStack[];
	static Stack stack;
	public static ArrayList<List<Integer>> SCC = new ArrayList<List<Integer>>();
	static Kante k;

	/**
	 * Erstellt die SCC Gruppen siehe Tarjan
	 */
	public static void generate(int Nodes, LinkedList<Kante>[] adj, ArrayList<Battery> bList) {
		SCC.clear();
		k = null;
		index = 0;
		stack = new Stack();
		onStack = new boolean[adj.length];
		indexV = new int[adj.length];
		lowlink = new int[adj.length];
		for (int i = 0; i < Nodes; i++) {
			if (indexV[i] == 0) {
				strongconnect(i, adj, bList);
			}
		}
		if(!stack.isEmpty()) {
			List<Integer> SCCGroup = new ArrayList<>();
			while(!stack.isEmpty()) {
				int v = (Integer)stack.pop();
				SCCGroup.add(v);
				bList.get(v).setSccid(SCC.size());
			}
			SCC.add(SCCGroup);
		}
	}

	// O(V + E) -> Tarjan
	// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
	public static void strongconnect(int v, LinkedList<Kante>[] adj, ArrayList<Battery> bList) {
		indexV[v] = index;
		lowlink[v] = index;
		index++;
		stack.push(v);
		onStack[v] = true;
		Iterator<Kante> list = adj[v].listIterator();
		while (list.hasNext()) {
			k = list.next();
			if (indexV[k.getNach()] == 0) {
				strongconnect(k.getNach(), adj, bList);
				lowlink[v] = (lowlink[v] < lowlink[k.getNach()]) ? lowlink[v] : lowlink[k.getNach()];
			} else if (onStack[k.getNach()]) {
				lowlink[v] = (lowlink[v] < indexV[k.getNach()]) ? lowlink[v] : indexV[k.getNach()];
			}
		}
		int w;
		if (lowlink[v] == indexV[v]) {
			List<Integer> SCCGroup = new ArrayList<>();
			do {
				w = (int) stack.pop();
				onStack[w] = false;
				SCCGroup.add(w);
				if (SCC.size() > 0) {
					if (SCC.get(SCC.size() - 1).get(0) == w) {
						continue;
					}
				}
				bList.get(w).setSccid(SCC.size());
			} while (w != v);
			if (SCC.size() > 0) {
				if (SCC.get(SCC.size() - 1).get(0) == w) {
				} else {
					SCC.add(SCCGroup);
				}
			} else {
				SCC.add(SCCGroup);
			}
		}
	}

	/**
	 * siehe GenerateEdges, nur für die Kanten zwischen den SCC-Gruppen -> Breitensuche
	 * @param arry 2D Feld
	 * @param size Größe
	 * @param posBattery x- und y-Position
	 * @param bMax Maximalenergie
	 * @param index ID
	 * @param adjExtern Erweiterte LinkedList bzw. Kantenliste
	 * @param SCCid SCC-ID
	 */
	public static void genEdgesSCC(int[][] arry, int size, int[] posBattery, int bMax, int index, int adjExtern[][],
			int SCCid) {
		boolean[][] visited = new boolean[size][size];
		Queue<int[]> queue = new LinkedList<>();
		queue.add(posBattery);// 0 : x | 1 : y | 2 : numb
		while (true) {
			if (queue.isEmpty()) {
				return;
			}
			int[] p = queue.remove();
			if (visited[p[0]][p[1]])
				continue;
			if (p[2] > bMax) {
				return;
			} // MAP wird nicht gespeichert!dxc
			if ((Stromrallye.bList.get(arry[p[1]][p[0]]).getSccid() != SCCid) && (arry[p[1]][p[0]] != 0)) {//wenn expanded node = anderer SCC, inerhalb der Reichweite von Node v
				if (adjExtern[SCCid][Stromrallye.bList.get(arry[p[1]][p[0]]).getSccid()] > p[2]//wenn neuer Wert von v kleiner ist(Dist)und nicht 0 dann:
						|| adjExtern[SCCid][Stromrallye.bList.get(arry[p[1]][p[0]]).getSccid()] == 0) {
					adjExtern[SCCid][Stromrallye.bList.get(arry[p[1]][p[0]]).getSccid()] = p[2]; //zugewießen
					adjExtern[Stromrallye.bList.get(arry[p[1]][p[0]]).getSccid()][SCCid] = p[2];
				}
				continue;
			}
			p[2]++;
			if (p[1] != (size - 1) && !(visited[p[0]][p[1] + 1]))
				horizontal(queue, p, 2);//Runter
			if (p[1] != 0 && !(visited[p[0]][p[1] - 1]))
				horizontal(queue, p, 1);//Hoch
			if (p[0] != (size - 1) && !(visited[p[0] + 1][p[1]]))
				vertikal(queue, p, 2);//Rechts
			if (p[0] != 0 && !(visited[p[0] - 1][p[1]]))
				vertikal(queue, p, 1);//Links
			visited[p[0]][p[1]] = true;
		}
	}

	/**
	 * Erstellt die SCC Matrix -> dynamisch. Berechnet welche von welcher aus erreichbar ist.
	 * @param adj LinkedList -> Kantenliste
	 * @param bList Batterienliste
	 * @param maxList Maxwerte der Gruppen
	 * @return SCC Matrix, mit Distanzwerten als GZ
	 */
	public static int[][] genSCCDistMatrix(LinkedList<Kante>[] adj, ArrayList<Battery> bList,
			ArrayList<int[]> maxList) {
		int[][] Matrix = new int[SCC.size()][SCC.size()];// zu sich selbst -2
		for (int nodeID = 0; nodeID < bList.size(); nodeID++) {
			for (int i = 0; i < adj[nodeID].size(); i++) {
				Kante k = adj[nodeID].get(i);
				if (k.getKosten() > maxList.get(bList.get(k.getVon()).getSccid())[1]) {
					continue;
				}
				if (bList.get(k.getVon()).getSccid() != bList.get(k.getNach()).getSccid()
						&& (!bList.get(k.getNach()).isDone())) {
					if (Matrix[bList.get(k.getVon()).getSccid()][bList.get(k.getNach()).getSccid()] == 0
							|| Matrix[bList.get(k.getVon()).getSccid()][bList.get(k.getNach()).getSccid()] > k
									.getKosten()) {
						Matrix[bList.get(k.getVon()).getSccid()][bList.get(k.getNach()).getSccid()] = k.getKosten();
						Matrix[bList.get(k.getNach()).getSccid()][bList.get(k.getVon()).getSccid()] = k.getKosten();
					}
				}
			}
		}
		for (int i = 0; i < SCC.size(); i++) {
			boolean isValisssd = true;
			for (int x = 0; x < SCC.get(i).size(); x++) {
				if (!bList.get(SCC.get(i).get(x)).isDone()) {
					isValisssd = false;
				}
			}
			if (isValisssd) {
				for (int iscc = 0; iscc < SCC.size(); iscc++) {
					Matrix[i][iscc] = -1;
					Matrix[iscc][i] = -1;
				}
			}
		}
		for (int i = 0; i < SCC.size(); i++) {// oder halt != index
			Matrix[i][i] = -1;
		}
		return Matrix;
	}
	
	/**
	 * Fügt den Zustand hinzu Links/Rechts
	 * @param queue OpenList
	 * @param p Zustand
	 * @param mode Modus
	 */
	public static void vertikal(Queue<int[]> queue, int[] p, int mode) {//1: Links, 2:Rechts
		int[] pIn = new int[3];
		System.arraycopy(p, 0, pIn, 0, p.length);
		pIn[0] = (mode==1)?(pIn[0]-1):(pIn[0]+1);
		queue.add(pIn);
	}
	
	/**
	 * Fügt den Zustand hinzu Hoch/Runter
	 * @param queue OpenList
	 * @param p Zustand
	 * @param mode Modus
	 */
	public static void horizontal(Queue<int[]> queue, int[] p, int mode) {//1: Hoch, 2:Runter
		int[] pIn = new int[3];
		System.arraycopy(p, 0, pIn, 0, p.length);
		pIn[1] = (mode==1)?(pIn[1]-1):(pIn[1]+1);
		queue.add(pIn);
	}
}
