/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan.heuristic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import me.jan.Stromrallye;
import me.jan.graph.ActionHandler;
import me.jan.graph.Battery;
import me.jan.graph.Kante;
import me.jan.utils.GenerateEdges;
import me.jan.utils.Helper;
import me.jan.utils.SCC;

public class InformedHeuristik13 {

	static int initMaxBattery;//Summe aller Energien
	static int initAgentBatt;//Start Roboterenergie
	static ArrayList<PriorityQueue<Object[]>> Bergsteiger = new ArrayList<PriorityQueue<Object[]>>();//Bergsteigeralgorithmus, welcher mit einer Art Bucket DatenStruktur initialisiert wurde und [zurück/runter] durch Beamsearchlimit getriggert wird!
	static int max;//Hat den dynamischen Maximumwert
	static int sccGroup;//Beinhaltet die aktuelle SCC-Gruppe
	static int stageID;//Bergsteigerstufe
	static boolean isUninformed;//Suchbedingung. Abhängig von den SCC und Geo Zustand.
	static Comparator<Object[]> compare;//Vergleicht die Zustände
	static int beamSearch;//Beamzähler
	static final int beamLimit = 2500;//Beamsuche limit
	static Object[] pState;//Zustand
	static int nodeID;//aktuelle Batterien ID
	static LinkedList<Kante>[] dynamicGraphKante;//dynamische Kantenliste. 
	static LinkedList<Kante>[] dynamicGraphKante2;//dynamische Kantenliste holder. siehe Comparator
	static ArrayList<Battery> dynamicGraphBatt;//dynamische BatterienListe
	static int agentBatt;//aktuelle Roboterenergie
	static int[][] newSCC;//2D Array mit den SCC Gruppen gespeichert -> isUninformed
	static Kante k;//Kantenholder
	static int x;//beliebiger Wert für Expandable
	static int newBatt;//neue Maximalenergie
	static ArrayList<Battery> dynamicGraphBatt2;//dynamische BatterienListe holder.
	static boolean isValide;//teste auf Gültigkeit
	static boolean isValidssse;//teste auf Gültigkeit2
	static PriorityQueue<Object[]> OpenLists;//Hier werden die Zustände gespeichert
	static Object[] b;//Zustandplazhalter
	static ArrayList<Battery> dynamicGraphBatt3;//dynamische BatterienListe
	static ArrayList<Battery> dynamicGraphBatt1;//dynamische BatterienListe
	static ArrayList<Battery> dynamicGraphBatt0;//dynamische BatterienListe
	public static boolean isEndNode;//Siehe GenerateEdges
	public static String EndNodedetails;//Siehe GenerateEdges
	public static int countExpansions = 0;//Zähler für die Expandierungen

	/**
	 * Startet das Suchen. Expandiert von dem Roboter aus!
	 * @param agent Anfangszustand
	 * @param sccMatrix SCC 2D Array
	 * @param uninformed Suchoption
	 * @return Weg oder Fehler
	 */
	public static String initGraph(Object[] agent, int[][] sccMatrix, boolean uninformed) {
		beamSearch = 0;
		Bergsteiger.clear();
		compare = new Comparator<Object[]>() {
			@Override
			public int compare(Object[] arg0, Object[] arg1) {
				return sortPriorityQueue(arg0, arg1);
			}
		};
		isUninformed = uninformed;
		sccGroup = Stromrallye.bList.get(0).getGeoGroup();
		initQueue();
		dynamicGraphKante = new LinkedList[Stromrallye.bSize + 1];
		initMaxBattery = (int) agent[2];
		max = initMaxBattery + 1;
		initAgentBatt = (int) agent[1];
		stageID = (isUninformed)?Helper.isGeoDone((ArrayList<Battery>) agent[4], Stromrallye.geoGruppe):0;
		for (int i = 0; i < Stromrallye.adj[0].size(); i++) {//Geht die Kantenliste des Roboter durch. 
			Kante k = Stromrallye.adj[0].get(i);
			int x = (k.isExpendable()) ? (initAgentBatt) : k.getKosten() + 1;
			for (int iy = k.getKosten(); iy <= x; iy = iy + 2) {
				int newBatt = initAgentBatt - iy;
				int sccid = Stromrallye.bList.get(k.getNach()).getSccid();
				int maxNach = (Stromrallye.maxList.get(sccid)[1] >= newBatt) ? (Stromrallye.maxList.get(sccid)[1])
						: newBatt;// weil 0 keine Gruppe -> nicht maxNach wenn nicht k.von!
				boolean isValide = false;
				for (int ix = 0; ix < SCC.SCC.size(); ix++) {
					if ((maxNach >= sccMatrix[sccid][ix]) && sccMatrix[sccid][ix] != 0
							&& ix != Stromrallye.bList.get(0).getSccid() || SCC.SCC.size() <= 2)
						isValide = true;
				}
				if (!isValide)//Ausschließungskriterium wird angewandt.
					break;
				addQueueItem(k.getNach(), newBatt, initMaxBattery - iy, (LinkedList<Kante>[]) agent[3],
						(ArrayList<Battery>) agent[4], (int[][]) agent[5], "0-" + k.getNach() + "-" + iy + ",", iy,
						stageID);//Zustand wird zur PQ hinzugefügt.
			}
		}
		if(isUninformed) {
			if (Bergsteiger.get(stageID).isEmpty()) {
				JOptionPane.showMessageDialog(null, "Nicht Lößbar!");
				return "Nichts!";
			}
		}else {
			if (Bergsteiger.get(0).isEmpty()) {
				JOptionPane.showMessageDialog(null, "Nicht Lößbar!");
				return "Nichts!";
			}
		}
		
		for (int i = 0; i < SCC.SCC.size(); i++) {//Markiert SCC Gruppe des Roboter's als erledigt, da er einziger Teilnehmer der SCC sein muss. 
			sccMatrix[i][Stromrallye.bList.get(0).getSccid()] = -1;
			sccMatrix[Stromrallye.bList.get(0).getSccid()][i] = -1;
			sccMatrix[i][i] = -1;
			sccMatrix[i][i] = -1;
		}
		if(!Helper.EndNodeDetection((ArrayList<Battery>) agent[4], nodeID, (int) agent[2])) {//Hier wird auf die 2 Endnodes getestet
			return "Nichts!";
		}
		ActionHandler.deleteEdge(Stromrallye.adj, 0);
		Stromrallye.bList.get(0).setDone(true);
		Stromrallye.bList.get(0).setEnergy(0);
		return startHeuristik();
	}

	/**
	 * Sucht einen gültigen Weg
	 */
	public static String startHeuristik() {
		while (testfkt()) {//Testet auf Vorhandensein eines Zustandes in der PQ
			for (int h = 0; h < Bergsteiger.size(); h++) {//Geht die PQ nach dem Bergsteigeralgorithmus/Greedy durch
				if (!Bergsteiger.get(h).isEmpty()) {
					stageID = h;
					OpenLists = Bergsteiger.get(stageID);
					b = OpenLists.peek();
					++countExpansions;
					dynamicGraphBatt3 = new ArrayList<>();
					Helper.copyBattery((ArrayList<Battery>) b[4], dynamicGraphBatt3);
					sccGroup = dynamicGraphBatt3.get((int) b[0]).getGeoGroup();
					BeamSearch: 
						while (!Bergsteiger.get(stageID).isEmpty()) {
						pState = Bergsteiger.get(stageID).remove();
						max = (max < (int) pState[2]) ? max : (int) pState[2];
						nodeID = (int) pState[0];
						if ((int) pState[2] <= 0) {
							System.out.println("Done mit dem Path: " + (String) pState[6] + " in:");
							System.out.println(System.nanoTime() - Stromrallye.start + "ns");
							return (String) pState[6];
						}
						for (int i = 0; i < Stromrallye.adj.length; i++)
							dynamicGraphKante[i] = new LinkedList<>();
						dynamicGraphBatt = new ArrayList<>();
						Helper.copyEdgeList((LinkedList<Kante>[]) pState[3], dynamicGraphKante);
						Helper.copyBattery((ArrayList<Battery>) pState[4], dynamicGraphBatt);
						agentBatt = dynamicGraphBatt.get(nodeID).getEnergy();
						dynamicGraphBatt.get(nodeID).setEnergy((int) pState[1]);
						if (dynamicGraphBatt.get(nodeID).getEnergy() == 0) {
							dynamicGraphBatt.get(nodeID).setDone(true);
						}
						if (dynamicGraphBatt.get(nodeID).getGeoGroup() != sccGroup && isUninformed
								&& !Helper.goisDone(dynamicGraphBatt, sccGroup))
							continue BeamSearch;
						if (agentBatt == 0) {// kann weg!
							continue;
						}
						if(agentBatt==((int) pState[2])) {//testen EndNode!
							Object[] posBatt = new Object[4];
							posBatt[0] = Stromrallye.bList.get(nodeID).getX();
							posBatt[1] = Stromrallye.bList.get(nodeID).getY();
							posBatt[2] = 0;
							GenerateEdges.genPosEdges(Stromrallye.initMap2d, Stromrallye.Mapsize, posBatt, (agentBatt==1)?-2:-1, Stromrallye.bList.get(nodeID).getId(), Stromrallye.externList);
							if(isEndNode) {
								if(agentBatt==1) {
									Kante k = new Kante(nodeID, 0, 1, false);
									k.setPath(EndNodedetails);
									Stromrallye.externList[nodeID].add(k);
									return (String) pState[6] + k.getVon() + "-" + k.getNach() + "-" + agentBatt + ",";
								}else {
									if(agentBatt%2==0) {
										Kante k = new Kante(nodeID, 0, 2, true);
										k.setPath(EndNodedetails);
										String[] slddl = EndNodedetails.split(Pattern.quote(","));
										k.setPathException(slddl[0] + "," + slddl[1] + "," + GenerateEdges.gegenteil(slddl[1]) + "," + GenerateEdges.gegenteil(slddl[0]));
										Stromrallye.externList[nodeID].add(k);
										return (String) pState[6] + k.getVon() + "-" + k.getNach() + "-" + agentBatt + ",";
									}else {
										Kante k = new Kante(nodeID, 0, 3, true);
										String[] slddl = EndNodedetails.split(Pattern.quote(","));
										k.setPath(slddl[0] + "," + slddl[1] + "," + GenerateEdges.gegenteil(slddl[1]) + ",");
										Stromrallye.externList[nodeID].add(k);
										return (String) pState[6] + k.getVon() + "-" + k.getNach() + "-" + agentBatt + ",";
									}
								}
							}
						}
						if (agentBatt > dynamicGraphBatt.get(nodeID).getEnergy()) {
							ActionHandler.deleteEdge(dynamicGraphKante, nodeID);// w.c -> O(lll) wobei lll eine
																				// Teilmenge von den Kanten ist.
							for (int c = 0; c < Stromrallye.externList[nodeID].size(); c++) {
								if (Stromrallye.externList[nodeID].get(c).getKosten() <= agentBatt) {
									dynamicGraphKante[nodeID].add(Stromrallye.externList[nodeID].get(c));
								}
							}
						}
						newSCC = new int[SCC.SCC.size()][SCC.SCC.size()];
						for (int i = 0; i < dynamicGraphKante[nodeID].size(); i++) {//Überprüft jeden Weg. Auch auf Expandable.
							k = dynamicGraphKante[nodeID].get(i);
							x = (k.isExpendable()) ? (agentBatt) : k.getKosten() + 1;
							for (int iy = k.getKosten(); iy <= x; iy = iy + 2) {
								newBatt = agentBatt - iy;
								dynamicGraphKante2 = new LinkedList[Stromrallye.bSize + 1];
								for (int ix = 0; ix < Stromrallye.adj.length; ix++)
									dynamicGraphKante2[ix] = new LinkedList<>();
								dynamicGraphBatt2 = new ArrayList<Battery>();
								Helper.copyBattery(dynamicGraphBatt, dynamicGraphBatt2);
								Helper.copyEdgeList(dynamicGraphKante, dynamicGraphKante2);
								if (!(agentBatt < k.getKosten()) && !dynamicGraphBatt2.get(k.getNach()).isDone()
										|| (int) pState[2] == agentBatt) {
									ActionHandler.deleteEdge(dynamicGraphKante2, nodeID);
									for (int c = 0; c < Stromrallye.externList[nodeID].size(); c++) {
										if (Stromrallye.externList[nodeID].get(c).getKosten() <= dynamicGraphBatt2
												.get(k.getVon()).getEnergy()) {
											dynamicGraphKante2[nodeID].add(Stromrallye.externList[nodeID].get(c));
										}
									}
									if (isUninformed)
										SCC.generate(dynamicGraphBatt2.size(), dynamicGraphKante, dynamicGraphBatt2);
									Helper.calcMax(SCC.SCC, dynamicGraphBatt2);
									int maxNach = (dynamicGraphBatt2.get(k.getNach()).getSccid() == dynamicGraphBatt2
											.get(k.getVon()).getSccid())
													? Stromrallye.maxList
															.get(dynamicGraphBatt2.get(k.getNach()).getSccid())[1]
													: (Stromrallye.maxList.get(dynamicGraphBatt2.get(k.getNach())
															.getSccid())[1] >= newBatt) ? (Stromrallye.maxList.get(
																	dynamicGraphBatt2.get(k.getNach()).getSccid())[1])
																	: newBatt;
									Stromrallye.maxList.get(dynamicGraphBatt2.get(k.getNach()).getSccid())[1] = maxNach;
									newSCC = SCC.genSCCDistMatrix(Stromrallye.externList, dynamicGraphBatt2,
											Stromrallye.maxList);
									isValide = false;
									isValidssse = true;
									for (int ix = 0; ix < SCC.SCC.size(); ix++) {
										if ((maxNach >= newSCC[dynamicGraphBatt2.get(k.getNach()).getSccid()][ix])
												&& newSCC[dynamicGraphBatt2.get(k.getNach()).getSccid()][ix] != -1
												&& newSCC[dynamicGraphBatt2.get(k.getNach()).getSccid()][ix] != 0
												|| SCC.SCC.size() <= 2)
											isValide = true;
										if (newSCC[dynamicGraphBatt2.get(k.getNach()).getSccid()][ix] != -1)
											isValidssse = false;
									}

									if (!isValide && !isValidssse) {
										break;
									}
									if (dynamicGraphBatt.get(k.getNach()).getGeoGroup() != sccGroup && isUninformed) {
										if (Helper.goisDone(dynamicGraphBatt, sccGroup)) {
											beamSearch = 0; // ineffizient
											sccGroup = dynamicGraphBatt2.get(k.getNach()).getGeoGroup();
											stageID = Helper.isGeoDone(dynamicGraphBatt2, Stromrallye.geoGruppe);
										}
									}
									addQueueItem(k.getNach(), newBatt, (int) pState[2] - iy, dynamicGraphKante2,
											dynamicGraphBatt2, newSCC,
											(String) pState[6] + k.getVon() + "-" + k.getNach() + "-" + iy + ",", iy,
											stageID);
									if (isUninformed) {//LIMIT
										beamSearch++;
										if (beamSearch == beamLimit) {
											beamSearch = 0;
											break BeamSearch;
										}
									}

								}
							}
						}
					}
					break;
				}
			}
		}
		System.out.println(System.nanoTime() - Stromrallye.start);
		return "Nichts!";

	}
	
	/**
	 * Testet auf das Vorhandensein eines Objektes in der PQ
	 * @return true/false
	 */
	public static boolean testfkt() {
		for (int i = 0; i < Bergsteiger.size(); i++) {
			if (!Bergsteiger.get(i).isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param arg0 Zustand 1
	 * @param arg1 Zustand 1
	 * @return Gewichtigkeit von Gruppe-(-Gruppe)
	 */
	public static int sortPriorityQueue(Object[] arg0, Object[] arg1) {
		if (isUninformed) {
			return (Helper.isGeoDone((ArrayList<Battery>) arg0[4], Stromrallye.geoGruppe) < Helper
					.isGeoDone((ArrayList<Battery>) arg1[4], Stromrallye.geoGruppe)) ? -1
							: (sccGroup == ((ArrayList<Battery>) arg0[4]).get((int) arg0[0]).getGeoGroup()) ? -1 : 1;
		} else {
			return (int) arg0[2] - (int) arg1[2];
		}
	}
	
	/**
	 * Erstellt die verschiedenen Stationen der PQ/Bergsteigeralgo. -> Stationen.
	 */
	public static void initQueue() {
		if (isUninformed) {
			for (int x = 0; x < Stromrallye.geoGruppe + 1; x++) {
				PriorityQueue<Object[]> OpenLists = new PriorityQueue<>(compare);// initialCapacity -> 11. Somit 1 Empty!
				Bergsteiger.add(OpenLists);
			}
		} else {
			PriorityQueue<Object[]> OpenLists = new PriorityQueue<>(compare);
			Bergsteiger.add(OpenLists);
		}
	}
	
	/**
	 * Fügt ein Zustand der Priority Queue hinzu
	 * @param kNach ID von v'
	 * @param newBatt Energie
	 * @param newMax Maximalenergie
	 * @param adj LinkedList -> Kanten
	 * @param bList Batterienliste
	 * @param sccMatrix 2D SCC Array
	 * @param path Weg
	 * @param iy holder
	 * @param id holder
	 */
	public static void addQueueItem(int kNach, int newBatt, int newMax, LinkedList<Kante>[] adj, ArrayList<Battery> bList, int[][] sccMatrix, String path, int iy, int id) {
		Object[] pState = new Object[7];
		pState[0] = kNach;
		pState[1] = newBatt;
		pState[2] = newMax;
		pState[3] = adj;
		pState[4] = bList;
		pState[5] = sccMatrix;
		pState[6] = path;
		Bergsteiger.get(id).add(pState);
	}
}