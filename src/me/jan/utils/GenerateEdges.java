/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;

import me.jan.graph.Kante;
import me.jan.heuristic.InformedHeuristik13;

public class GenerateEdges {
	

	/**
	 * Erstellt eine Kantenliste mit einer BFS-Suche
	 * @param arry 2D Array mit ID, der Batterie(n)
	 * @param size Mapgröße
	 * @param posBattery x- und y-Position des Roboters + ID + Weg
	 * @param bEnergy Energie
	 * @param index ID
	 * @param adj LinkedListe -> Kantenliste
	 */
	public static void genPosEdges(int[][] arry, int size, Object[] posBattery, int bEnergy, int index,
			LinkedList<Kante> adj[]) {
		boolean[][] visited = new boolean[size][size];
		boolean oneself2 = false, oneselfunl = false;
		posBattery[3] = "";
		Queue<Object[]> queue = new LinkedList<>();//arry: [y][x]
		queue.add(posBattery);// 0 : x | 1 : y | 2 : numb | 3 : beweg.
		while (!(queue.isEmpty())) {
			Object[] p = queue.remove();
			if (visited[(int) p[0]][(int) p[1]])
				continue;
			if ((int)p[2] > bEnergy&&bEnergy!=-1&&bEnergy!=-2) {
				return;
			}
			
			/**für 1 Step bei 0**/
			if((int)p[2]==1&&arry[(int)p[1]][(int)p[0]]==0&&index==0&&bEnergy!=-1) {//max O(2)
				Iterator<Kante> list = adj[index].listIterator();
				while (list.hasNext()) {
					Kante k = list.next();
					k.setExpendable(true);
				}
			}
			
			if(bEnergy==-1) {//End Node testing...WENN ENDNODE ID==2 oder mehr
				if((int)p[2]==2) {
					InformedHeuristik13.isEndNode = true;
					InformedHeuristik13.EndNodedetails = (String) p[3];
					return;
				}
			}
			
			if(bEnergy==-2) {//WENN ENDNODE ID==1
				if((int)p[2]==1) {
					InformedHeuristik13.isEndNode = true;
					InformedHeuristik13.EndNodedetails = (String) p[3];
					return;
				}
			}
			
			/**für 1 Step**/
			if ((int)p[2] == 2&&arry[(int)p[1]][(int)p[0]]==0&&bEnergy!=-1&&bEnergy!=-2) {
				if ((int)p[1] != (size - 1) && (visited[(int)p[0]][(int)p[1] + 1]))//max O(2)
					handleOneStep((int)p[1] + 1, (int)p[0], adj, index, arry, "Runter,", (String)p[3]);//unten
				else if ((int)p[1] != 0 && (visited[(int)p[0]][(int)p[1] - 1]))//max O(2)
					handleOneStep((int)p[1] - 1, (int)p[0], adj, index, arry, "Hoch,", (String)p[3]);//oben
				else if ((int)p[0] != (size - 1) && (visited[(int)p[0] + 1][(int)p[1]]))//max O(2)
					handleOneStep((int)p[1], (int)p[0]+1, adj, index, arry, "Rechts,", (String)p[3]);//rechts
				else if ((int)p[0] != 0 && (visited[(int)p[0] - 1][(int)p[1]]))//max O(2)
					handleOneStep((int)p[1], (int)p[0]-1, adj, index, arry, "Links,", (String)p[3]);//links
			}
			 
			/**für 2 Steps**/
			if((int)p[2]==1&&arry[(int)p[1]][(int)p[0]]==0&&bEnergy!=-1&&bEnergy!=-2) {
				if ((int)p[1] != (size - 1)) 
					handleTwoSteps((int)p[1] + 1, (int)p[0], adj, index, arry, visited, p, size, "Runter,");
				else if ((int)p[1] != 0) 
					handleTwoSteps((int)p[1] - 1, (int)p[0], adj, index, arry, visited, p, size, "Hoch,");
				else if ((int)p[0] != (size - 1)) 
					handleTwoSteps((int)p[1], (int)p[0] + 1, adj, index, arry, visited, p, size, "Rechts,");
				else if ((int)p[0] != 0)
					handleTwoSteps((int)p[1], (int)p[0] - 1, adj, index, arry, visited, p, size, "Links,");
			}
			
			if (arry[(int)p[1]][(int)p[0]] != 0 && arry[(int)p[1]][(int)p[0]] != index&&bEnergy!=-1&&bEnergy!=-2) {//Wenn x,y auf ID -> Batt
				Kante k = new Kante(index, arry[(int)p[1]][(int)p[0]], (int)p[2], (index==0)?((int)p[2]>1)?true:false:((int)p[2]>2)?true:false);
				k.setPath((String)p[3]);
				visited[(int)p[0]][(int)p[1]] = true;
				adj[index].add(k);//Fügt erfolgreiche Kanten zur Liste hinzu.
				continue;
			}
			if ((oneself2 == false) && (arry[(int)p[1]][(int)p[0]] == 0) && ((int)p[2] == 1) && bEnergy > 1 && index != 0&&bEnergy!=-1&&bEnergy!=-2) {
				Kante k = new Kante(index, index, 2, false);
				String[] split = ((String)p[3]).split(Pattern.quote(","));
				k.setPath(split[0] + "," + gegenteil(split[0]));
				adj[index].add(k);
				oneself2 = true;//Kante von v->v' mit 2
			} else if ((oneselfunl == false) && (arry[(int)p[1]][(int)p[0]] == 0) && ((int)p[2] == 2) && bEnergy > 3 && index != 0&&bEnergy!=-1&&bEnergy!=-2) {
				Kante k = new Kante(index, index, 4, true);
				String[] split = ((String)p[3]).split(Pattern.quote(","));
				k.setPath(split[0] + "," + split[1] + "," + gegenteil(split[1]) + "," + gegenteil(split[0]) + ",");
				adj[index].add(k);
				oneselfunl = true;//Kante von v->v' mit unl
			}
			p[2] = (int)p[2] + 1;//Expandiert die möglichen Zustände
			if ((int)p[1] != (size - 1) && !(visited[(int)p[0]][(int)p[1] + 1]))
				vertikal(queue, p, "Runter,", 2);
			if ((int)p[1] != 0 && !(visited[(int)p[0]][(int)p[1] - 1]))
				vertikal(queue, p, "Hoch,", 1);
			if ((int)p[0] != (size - 1) && !(visited[(int)p[0] + 1][(int)p[1]]))
				horizontal(queue, p, "Rechts,", 1);
			if ((int)p[0] != 0 && !(visited[(int)p[0] - 1][(int)p[1]]))
				horizontal(queue, p, "Links,", 2);
			visited[(int)p[0]][(int)p[1]] = true;
		}
	}

	/**
	 * Breitensuche für die SCC Abstände!
	 * @param SCCMatrix 2D Array
	 * @param sumCalc Zustand 0:x|1:sum
	 * @param maxDest Maximale Reichweite
	 * @param SCCGroups Anzahl SCC Gruppen
	 * @param dest Zielgruppe
	 * @return Schritte in GZ
	 */
	public static int BFSSSC(int[][] SCCMatrix, int[] sumCalc, int maxDest, int SCCGroups, int dest) {
		int maxholder = maxDest;
		Queue<int[]> queue = new LinkedList<>(); 
		queue.add(sumCalc);
		while (!(queue.isEmpty())) {
			int[] p = queue.remove();
			if (p[1] <= maxDest)
				continue;
			if (p[0] == dest) {
				maxholder = (p[1] > maxholder) ? p[1] : maxholder;
				continue;
			}
			for (int i = 0; i < SCCGroups; i++) {
				if (SCCMatrix[p[0]][i] != 0) {
					int[] pNew = new int[2];
					pNew[1] = p[1] - SCCMatrix[p[0]][i];
					pNew[0] = i;
					queue.add(pNew);
				}
			}
		}
		return maxholder;
	}
	
	/**
	 * Hoch/Runter
	 * @param queue OpenList
	 * @param p Zustand   
	 * @param vertikal Hoch, oder Runter,
	 * @param mode Modus <br> 1:Hoch <br> 2:Runter
	 */
	public static void vertikal(Queue<Object[]> queue, Object[] p, String vertikal, int mode) {
		Object[] pIn = new Object[4];
		System.arraycopy(p, 0, pIn, 0, p.length);
		pIn[1] = (mode==1)?(((int) pIn[1])-1):(((int) pIn[1])+1);
		pIn[3] = pIn[3] + vertikal;
		queue.add(pIn);
	}
	
	/**
	 * Links/Rechts
	 * @param queue OpenList
	 * @param p Zustand
	 * @param horizontal Rechts, oder Links,
	 * @param mode Modus <br> 1:Rechts <br> 2:Links
	 */
	public static void horizontal(Queue<Object[]> queue, Object[] p, String horizontal, int mode) {
		Object[] pIn = new Object[4];
		System.arraycopy(p, 0, pIn, 0, p.length);
		pIn[0] = (mode==1)?(((int) pIn[0])+1):(((int) pIn[0])-1);
		pIn[3] = pIn[3] + horizontal;
		queue.add(pIn);
	}
	
	/**
	 * Geht mit der Kante, mit Kosten: 1 um.
	 * @param x x-Position
	 * @param y y-Position
	 * @param adj LinkedList -> Kantenliste
	 * @param index ID
	 * @param arry 2D Map als Array
	 * @param Weg Bewegung
	 * @param pathcurr aktueller Pfad
	 */
	public static void handleOneStep(int x, int y, LinkedList<Kante> adj[], int index, int[][] arry, String Weg, String pathcurr) {
		if (arry[x][y] != 0) {
			Iterator<Kante> list = adj[index].listIterator();
			while (list.hasNext()) {
				Kante k = list.next();
				if (k.getNach() == arry[x][y]) {
					k.setPathException(pathcurr + Weg);
					k.setExpendable(true);
					break;
				}
			}
		}
	}
	
	 /** Geht mit der Kante, mit Kosten: 2 um.
	 * @param x x-Position
	 * @param y y-Position
	 * @param adj LinkedList -> Kantenliste
	 * @param index ID
	 * @param arry 2D Map als Array
	 * @param Weg Bewegung
	 * @param pathcurr aktueller Pfad
	 * @param p Zustand
	 * @param size Mapgröße
	 */
	public static void handleTwoSteps(int x, int y, LinkedList<Kante> adj[], int index, int[][] arry, boolean[][] visited, Object[] p, int size, String Weg) {		
		if(arry[x][y] != index && arry[x][y] != 0) {
			String save = (String) p[3] + Weg;
			if(!visited[x][y]) {
				boolean isValid = false;
				isValid = calcValid(1, 0, isValid, arry, p, "Runter,");
				isValid = calcValid(-1, 0, isValid, arry, p, "Hoch,");
				isValid = calcValid(0, 1, isValid, arry, p, "Rechts,");
				isValid = calcValid(0, -1, isValid, arry, p, "Links,");
				if(isValid) {
					Kante k = new Kante(index, arry[x][y], 2, true);
					k.setPath(save);
					k.setPathException((String) p[3] + Weg);
					adj[index].add(k);
					visited[y][x] = true;
				}else {
					Kante k = new Kante(index, arry[x][y], 2, false);
					k.setPath(save);
					adj[index].add(k);
					visited[y][x] = true;
				}
			}else {
				boolean isValid = false;
				isValid = calcValid(1, 0, isValid, arry, p, "Runter,");
				isValid = calcValid(-1, 0, isValid, arry, p, "Hoch,");
				isValid = calcValid(0, 1, isValid, arry, p, "Rechts,");
				isValid = calcValid(0, -1, isValid, arry, p, "Links,");
				if(isValid) {
					Iterator<Kante> list = adj[index].listIterator();
					while (list.hasNext()) {
						Kante k = list.next();
						if (k.getNach() == arry[x][y]) {
							k.setPath((String) p[3] + Weg);
							k.setPathException((String) p[3] + Weg);
							k.setExpendable(true);
							break;
						}
					}		
				}
			}
		}
	}
	
	/**
	 * Testet ob die jeweilige Richtung möglich ist! Arraybounds
	 * @param x x-Position
	 * @param y y-Position
	 * @param isValid bool ob Aktion möglich ist
	 * @param arry 2D Feld
	 * @param p Zustand
	 * @param Weg Bewegung
	 * @return isValid
	 */
	public static boolean calcValid(int x, int y, boolean isValid, int[][] arry, Object[] p, String Weg) {
		try {
			if(!isValid) {
				if((arry[((int)p[1] + x)][((int)p[0] + y)] == 0)) {
					p[3] = p[3] + Weg + gegenteil(Weg) + ",";
				}
			}
			return(arry[((int)p[1] + x)][((int)p[0] + y)] == 0)?true:isValid;
		} catch (Exception e) {
			return isValid;
		} 
	}
	
	/**
	 * Gibt das Gegenteil aus
	 * @param input Richtung
	 * @return
	 */
	public static String gegenteil(String input) {
		if(input.contains("Runter")) {
			return "Hoch";
		}else if(input.contains("Hoch")) {
			return "Runter";
		}else if(input.contains("Links")) {
			return "Rechts";
		}else {
			return "Links";
		}
	}
}
