/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import me.jan.Stromrallye;
import me.jan.graph.Battery;
import me.jan.graph.Kante;

public class Helper {
	
	// DEBUG
	public static void printGraph(int size, int[][] arry) {
		for (int i = 0; i < size; i++) {
			for (int x = 0; x < size; x++) {
				System.out.print(arry[x][i] + " ");
			}
			System.out.println();
		}
	}

	// DEBUG
	public static void printEdges(LinkedList<Kante> adj[], int size) {
		for (int i = 0; i < adj.length; i++) {
			Iterator<Kante> list = adj[i].listIterator();
			while (list.hasNext()) {
				Kante k = list.next();
				System.out.println(k.getVon() + " -> " + k.getNach() + " mit den Kosten: " + k.getKosten() + " isExpandable: " + k.isExpendable() + " Pfad: " + k.getPath() + " Path Exception: " + k.getPathException());
			}
		}
	}
	
	public static int calcSumEnergy(ArrayList<Battery> bList) {
		int energie = 0;
		for(int i = 0; i < bList.size(); i++) {
			energie += bList.get(i).getEnergy();
		}
		return energie;
	}
	
	public static int findBattbyKoord(int x, int y, ArrayList<Battery> bList) {
		for(int i = 0; i < bList.size(); i++) {
			if(bList.get(i).getX()==x&&bList.get(i).getY()==y) {
				return i;
			}
		}
		return -1;
	}
	
	// DEBUG
		public static void printBatts(ArrayList<Battery> bList, int size) {
			for (int i = 0; i < size; i++) {
				System.out.println("ID:" + bList.get(i).getId() + "|" + "Energy:" + bList.get(i).getEnergy() + "|isDone:" + bList.get(i).isDone() + "|SCCGruppe: " + bList.get(i).getSccid() + "|GeoGroup:" + bList.get(i).getGeoGroup());
			}
		}
	
	//DEBUG
	public static void printSCC(ArrayList<List<Integer>> SCCList) {
		for(int i = 0; i < SCCList.size(); i++) {
			System.out.println(SCCList.get(i).toString());
		}
	}
	
	//DEBUG
	public static void printSCCGroup() {
		for(int i = 0; i < Stromrallye.bList.size(); i++) {
			System.out.println(Stromrallye.bList.get(i).getId() + ":" + Stromrallye.bList.get(i).getSccid());
		}
	}
	
	//DEBUG
	public static void printSCCMatrix(int[][] adjMatrix) {
		for(int i = 0; i < SCC.SCC.size(); i++) {
			for(int ix = 0; ix < SCC.SCC.size(); ix++) {
				System.out.print(adjMatrix[i][ix] + " ");
			}
			System.out.println();
		}
	}
	
	//DEBUG
	public static void printEnergy(ArrayList<Battery> bList, int size, int[][] arry) {
		for (int i = 0; i < size; i++) {
			for (int x = 0; x < size; x++) {
				System.out.print(bList.get(arry[i][x]).getEnergy() + " ");
			}
			System.out.println();
		}
	}
	
	//DEBUG
	public static void printMaxList(ArrayList<int[]> maxList) {
		for(int i = 0; i < maxList.size(); i++) {
			System.out.println("Index: " + maxList.get(i)[0] + "|max:" + maxList.get(i)[1] + " in SCCGruppe: " + i);
		}
	}
	
	public static void printGeoGruppen(ArrayList<Battery> bList, int size, int[][] arry) {
		for (int i = 0; i < size; i++) {
			for (int x = 0; x < size; x++) {
				System.out.print(bList.get(arry[i][x]).getGeoGroup() + " ");
			}
			System.out.println();
		}
	}
	
	/**
	 * Methode zur Überprüfung, ob es sich bei dem Graphen um einen Ungültigen handelt. 
	 * @param bList Batterieliste
	 * @param nodeId Knotenid
	 * @param totalBatt Anzahl bzw auch bList.size()
	 * @return true: gültiger Graph <br>false: ungültiger Graph
	 */
	public static boolean EndNodeDetection(ArrayList<Battery> bList, int nodeId, int totalBatt) {
		if(totalBatt<5) {
			return true;
		}
		int EndNodes = 0;
		for (int i = 1; i < bList.size(); i++) {
			boolean[] visited = new boolean[bList.size()];
			if(bList.get(i).isEndNode()&&!bList.get(i).isDone()) {
				int cntneig = 0;
				ArrayList<Integer> nodeholder = new ArrayList<Integer>();
				for(int x = 0; x < bList.get(i).getNeighbors().size(); x++) {
					if(bList.get(bList.get(i).getNeighbors().get(x)).getEnergy()==1) {
						cntneig++;
						nodeholder.add(bList.get(i).getNeighbors().get(x));
					}
					if(bList.get(bList.get(i).getNeighbors().get(x)).getEnergy()==2||i==nodeId)//||bList.get(i).getNeighbors().get(x)==nodeId)
						cntneig = 100;//oder break;
					visited[bList.get(i).getNeighbors().get(x)]=true;
				}
				if(cntneig==1) {
					int nsss = 0;
					for(int x = 0; x < bList.get(nodeholder.get(0)).getNeighbors().size(); x++) {
						if(x==i)
							continue;
						if(bList.get(bList.get(nodeholder.get(0)).getNeighbors().get(x)).getEnergy()==2)
							nsss = 100;			
					}
					if(nsss==0) {
						EndNodes++;
					}
				}else if(cntneig==2) {
					int cntneig2 = 0;
					ArrayList<Integer> nodeholder2 = new ArrayList<Integer>();
					for(int x = 0; x < nodeholder.size(); x++) {
						for(int c = 0; c < bList.get(nodeholder.get(x)).getNeighbors().size(); c++) {
							if(bList.get(bList.get(nodeholder.get(x)).getNeighbors().get(c)).getEnergy()==1&&bList.get(nodeholder.get(x)).getNeighbors().get(c)!=i) {
								cntneig2++;
								nodeholder2.add(bList.get(nodeholder.get(x)).getNeighbors().get(c));
							}
						}
					}
					Collections.sort(nodeholder2);
					Collections.reverse(nodeholder2);
					if(cntneig2>1) {
						int old = -1;
						for(int vvb = 0; vvb < nodeholder2.size(); vvb++) {
							int holder = nodeholder2.get(vvb);
							if(holder == old) {
									if(cntneig2==2) {
										EndNodes++;
									}else {
										int hu = 0;
										for(int c = 0; c < bList.get(holder).getNeighbors().size(); c++) {
											if(bList.get(bList.get(holder).getNeighbors().get(c)).getEnergy()==1) {
												hu++;
											}
										}
										if(hu==0) {
											EndNodes++;
										}
									}
								break;
							}
							old = nodeholder2.get(vvb);
						}
					}
				}
			}
			
		}
		return (EndNodes>1)?false:true;
	}
	
	/**
	 * Berechnet den Maximalen Energiewert in der entsprechenden Gruppe
	 * @param SCCList SCC-Liste
	 * @param bList Batterienliste
	 */
	public static void calcMax(ArrayList<List<Integer>> SCCList, ArrayList<Battery> bList) {//O(SCCGroups*nonGruppe) -> O(n)==Dead End vs. Unendlich viele expandings von nodes. -> O(n) worst case pro update.
		int[] s = {0,0};//Notation
		Stromrallye.maxList.clear();
		for(int z = 0; z < SCC.SCC.size(); z++) {//O(SCC.size())
			Stromrallye.maxList.add(s);
		}
		int max = -1;
		int energy;
		int index = -1;
		for(int i = 0; i < SCCList.size(); i++) {
			max = -1;
			index = -1;
			for(int y = 0; y < SCCList.get(i).size(); y++) {
				energy = bList.get(SCCList.get(i).get(y)).getEnergy();
				if(energy>max) {
					max = energy;
					index = SCCList.get(i).get(y);//max index für später
				}
			}
			int[] maxSCC = new int[2];
			maxSCC[0] = index;
			maxSCC[1] = max;
			Stromrallye.maxList.set(i, maxSCC);
		}
	}//O(n)
	
	public static void copyEdgeList(LinkedList<Kante>[] src, LinkedList<Kante>[] dest) {//O(m) == tricky, weil eigentlich ne, nur O(E)/O(m)
		for (int i = 0; i < src.length; i++) {
			for(Kante k : src[i]) {
				Kante kante = new Kante(k.getVon(), k.getNach(), k.getKosten(), k.isExpendable());
				dest[i].add(kante);
			}
		}
	}
	
	public static void copyBattery(ArrayList<Battery> src, ArrayList<Battery> dest) {//O(n) -> insegesammt mit Edge: O(n+m) -> pro Path, dafür sehr wenige Paths
		for(Battery b : src) {
			Battery battary = new Battery(b.getX(), b.getY(), b.getEnergy(), b.getId(), b.getSccid(), b.isDone(), b.getGeoGroup());
			dest.add(battary);
		}
	}
	
	public static void copyMatrix(int[][] src, int[][] dest, int size) {
		for(int i = 0; i < size; i++)
			for(int icc = 0; icc < size; icc++)
				dest[i][icc] = src[i][icc];
	}
	
	/**
	 * @return Anzahl verbleibenden SCC-Knoten
	 */
	public static int calcSccc() {
		int z = 0;
		for(int i = 0; i < SCC.SCC.size(); i++) {
			for(int u = 0; u < SCC.SCC.get(i).size(); u++) {
				z++;
			}
		}
		return z;
	}
	
	public static boolean isSameArray(ArrayList<Battery> bList, ArrayList<Battery> bList2) {
		for (int i = 0; i < bList.size(); i++) {
			if(bList.get(i).getEnergy()!=bList2.get(i).getEnergy())
				return false;
		}
		return true;
	}
	
	/**
	 * Testet die Geo-Gruppe auf Vollendung -> Keine Enegrie mehr -> Batt.isDone für alle veV
	 * @param bList Batterienliste
	 * @param geoID Geo-Gruppenid
	 * @return true: Fertig <br> false: Nicht
	 */
	public static boolean goisDone(ArrayList<Battery> bList, int geoID) {
		for(int i = 0; i < bList.size(); i++) {
			if(bList.get(i).getGeoGroup()==geoID) {
				if(!bList.get(i).isDone())
					return false;//1
			}
		}
		return true;//-1
	}
	
	/**
	 * Anzahl verbleibendender Geo-Gruppen
	 * @param bList Batterienliste
	 * @param size Bergsteiger/Geo->size
	 * @return Anzahl an verbleibendenden Geo-Gruppen
	 */
	public static int isGeoDone(ArrayList<Battery> bList, int size) {
		int cnt = size;
		for (int i = 0; i < size; i++) {
			boolean Valid = true;
			for(int x = 0; x < bList.size(); x++) {
				if(bList.get(x).getGeoGroup()==i) {
					if(!bList.get(x).isDone()) {
						Valid = false;
					}
				}
			}
			if(Valid) {
				cnt--;
			}
		}
		return cnt;
	}
	
	/**
	 * Berechnet den weitesten/größten x- bzw. y-Wert einer Batterie
	 */
	public static int getLastSize() {
		int max = 0;
		for(int i = 0; i < Stromrallye.bList.size(); i++) {
			if(Stromrallye.bList.get(i).getX()>max||Stromrallye.bList.get(i).getY()>max) {
				max = (Stromrallye.bList.get(i).getX()>=Stromrallye.bList.get(i).getY())?Stromrallye.bList.get(i).getX():Stromrallye.bList.get(i).getY();
			}
		}
		return max;
	}
	
	/**
	 * Berechnet den kleinsten x- bzw. y-Wert einer Batterie
	 */
	public static int getFirstSize() {
		int min = 0;
		for(int i = 0; i < Stromrallye.bList.size(); i++) {
			if(Stromrallye.bList.get(i).getX()<min||Stromrallye.bList.get(i).getY()<min) {
				min = (Stromrallye.bList.get(i).getX()<=Stromrallye.bList.get(i).getY())?Stromrallye.bList.get(i).getX():Stromrallye.bList.get(i).getY();
			}
		}
		return min;
	}
	
	/**
	 * Testet den Rahmen der Map. (Helper für die Gruppierung/Einteilung)
	 * @param Valid Ungerade Ja/Nein
	 * @param initMap2d 2D Feld
	 * @param x x-Position
	 * @param i holder
	 * @param geoGruppe Geo-ID
	 * @return Valid
	 */
	public static boolean calcUneven(boolean Valid, int[][] initMap2d, int x, int i, int geoGruppe) {
		if(!Stromrallye.isArryZero(initMap2d[x][i])||Stromrallye.isAgentStart(x, i)) {
			Valid = true;
			Stromrallye.bList.get(initMap2d[x][i]).setGeoGroup(geoGruppe);//if not 0//und get 0
		}
		return Valid;
	}
	
	/**
	 * Helper für das Gruppieren. siehe generateUnEvenMap für die Gruppenbounds/Gruppengrößen
	 * @param arryStart Start
	 * @param arrayEnd Ende
	 * @param arrayStart2 Start2
	 * @param arrayEnd2 Ende2
	 * @param mapSize mapgröße
	 */
	public static void calcValid(int arryStart, int arrayEnd, int arrayStart2, int arrayEnd2, int mapSize) {
		boolean Valid = false; 
		for(int pX = arryStart; pX >= arrayEnd; pX--) {
			for(int pI = arrayStart2; pI >= arrayEnd2; pI--) {
				Valid = Helper.calcUneven(Valid, Stromrallye.initMap2d, mapSize+pX, mapSize+pI, Stromrallye.geoGruppe);
			}
		}
		if(Valid) {
			Stromrallye.geoGruppe++;
		}
	}
	
	//siehe calcValid und generateUnEvenMap
	public static void calcValid2(int arryStart, int arrayEnd, int arrayStart2, int arrayEnd2, int mapSize, int z) {
		boolean Valid = false;
		for(int pX = arryStart; pX >= arrayEnd; pX--) {
			for(int pI = arrayStart2; pI <= arrayEnd2; pI++) {
				Valid = Helper.calcUneven(Valid, Stromrallye.initMap2d, mapSize+pX, z+pI, Stromrallye.geoGruppe);	
			}
		}
		if(Valid) {
			Stromrallye.geoGruppe++;
		}
	}
	
	//siehe calcValid und generateUnEvenMap
	public static void calcValid3(int arryStart, int arrayEnd, int arrayStart2, int arrayEnd2, int mapSize, int z) {
		boolean Valid = false;
		for(int pX = arryStart; pX <= arrayEnd; pX++) {
			for(int pI = arrayStart2; pI >= arrayEnd2; pI--) {
				Valid = Helper.calcUneven(Valid, Stromrallye.initMap2d, z+pX, mapSize+pI, Stromrallye.geoGruppe);	
			}
		}
		if(Valid) {
			Stromrallye.geoGruppe++;
		}
	}
	
	/**
	 * Berechnet/Erstellt die Gruppen.
	 * @param minSize Startpunkt im 2D Feld - Bounds ungerade
	 * @param mapSize Endpunkt im 2D Feld - Bounds ungerade
	 */
	public static void generateUnEvenMap(int minSize, int mapSize) {
		int calkc = (mapSize-minSize)%3;
		int size = (calkc==0)?mapSize:(calkc==1)?(mapSize-4):(calkc==2)?(mapSize-2):-111;//-111 -> unmöglich
		if(size==-111) {return;}
		for(int i = minSize; i < size; i = i + 3) {
			for(int x = minSize; x < size; x = x + 3) {
				boolean Valid = false;
				for(int pX = 0; pX <= 2; pX++) {
					for(int pI = 0; pI <= 2; pI++) {
						Valid = Helper.calcUneven(Valid, Stromrallye.initMap2d, x+pX, i+pI, Stromrallye.geoGruppe);	
					}
				}
				if(Valid) {
					Stromrallye.geoGruppe++;
				}
			}
		}
		if(calkc!=0) {
			if(calkc==1) {
				calcValid(-1, -2, -1, -2, mapSize);//Ecke 2x2
				calcValid(-3, -4, -3, -4, mapSize);//2x3
				calcValid(-1, -2, -3, -4, mapSize);//2x3
				calcValid(-3, -4, -1, -2, mapSize);//2x3
				for(int z = minSize; z < mapSize-4; z = z + 3) {//außen! 2x3
					calcValid2(-3, -4, 0, 2, mapSize, z);
				}
				for(int z = minSize; z < mapSize-4; z = z + 3) {//außen! 2x3
					calcValid2(-1, -2, 0, 2, mapSize, z);
					calcValid3(0, 2, -1, -2, mapSize, z);
				}
			}else if(calkc==2) {
				calcValid(-1, -2, -1, -2, mapSize);
				for(int z = minSize; z < mapSize-2; z = z + 3) {
					calcValid2(-1, -2, 0, 2, mapSize, z);
					calcValid3(0, 2, -1, -2, mapSize, z);
				}
			}
		}
	}
	
	/**
	 * Berechnet/Erstellt die Gruppen.
	 * @param minSize Startpunkt im 2D Feld - Bounds gerade
	 * @param mapSize Endpunkt im 2D Feld - Bounds gerade
	 */
	public static void generateEvenMap(int minSize, int mapSize) {
		for (int i = minSize; i < mapSize; i = i + 2) {
			int cnt = 0;
			for (int x = minSize; x < mapSize; x++) {
				cnt++;
				if(cnt == 2) {
					cnt = 0;
					boolean Valid = false;
					for(int pX = 0; pX >= -1; pX--) {
						for(int pI = 0; pI <= 1; pI++) {
							Valid = Helper.calcUneven(Valid, Stromrallye.initMap2d, x+pX, i+pI, Stromrallye.geoGruppe);
						}
					}
					if(Valid) {
						Stromrallye.geoGruppe++;
					}
				}
			}
		}
	}
}