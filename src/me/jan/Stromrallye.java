/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import application.GuiController;
import me.jan.graph.Battery;
import me.jan.graph.Kante;
import me.jan.utils.GenerateEdges;
import me.jan.utils.Helper;
import me.jan.utils.SCC;

public class Stromrallye {

	public static int maxBatt;//größte Energie
	public static int Mapsize;//Feldgröße
	static int xAgent, yAgent, bAgent;//Roboterattribute
	public static int[][] initMap2d;//2D Feld
	public static LinkedList<Kante>[] adj;//Kantenliste 
	public static ArrayList<Battery> bList = new ArrayList<Battery>();//Batterienliste
	public static ArrayList<int[]> maxList = new ArrayList<int[]>();//0:index|1:max
	public static LinkedList<Kante>[] externList;//Erweiterte Liste
	public static int[][] SCCmatrix;//SCC Matrix
	public static int bSize;//Anzahl an Batterien
	public static long start;//Zeitmessung
	public static int geoGruppe;//Geo-Gruppe ID für Helper
	
	/**
	 * Initialisiert die Suche bzw. deren Werte/Listen
	 * @param file Datei
	 * @return Status <br> -1: Fehler <br> 0: Erfolg <br> 1: Spielfeld gelöst(Bei geg. Eingaben) <br> 2: weiterer Sonderfall
	 * @throws IOException
	 */
	public static int startHeuristik(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st = null;
		Mapsize = Integer.parseInt(br.readLine());
		if(Mapsize<=0) {
			JOptionPane.showMessageDialog(null, "Bitte geben sie eine gültige Spielfeldgröße an!", "Fehler",
					JOptionPane.ERROR_MESSAGE);
			return -1;//Return Error!
		}
		bList.clear();
		initMap2d = new int[Mapsize][Mapsize];
		st = br.readLine();
		String[] splitAgent = st.split(Pattern.quote(","));
		xAgent = Integer.parseInt(splitAgent[0]);
		yAgent = Integer.parseInt(splitAgent[1]);
		bAgent = Integer.parseInt(splitAgent[2]);
		if(bAgent<0) {
			System.out.println("Ungültige Batterie-Anzahl");
			JOptionPane.showMessageDialog(null, "Bitte geben sie eine gültige Batterie-Anzahl an!", "Fehler",
					JOptionPane.ERROR_MESSAGE);
			return -1;//Return Error!
		}
		maxBatt = Integer.parseInt(splitAgent[2]);
		Battery batteryAgent = new Battery(xAgent - 1, yAgent - 1, bAgent, 0, 0, false, 0);
		bList.add(batteryAgent);//O(1)
		bSize = Integer.parseInt(br.readLine());
		if(bSize<0) {
			JOptionPane.showMessageDialog(null, "Bitte geben Sie einen gültigen Wert ein!", "Fehler",
					JOptionPane.ERROR_MESSAGE);
			return -1;
		}
		if (bAgent == 0) {
			if(bSize == 0) {
				JOptionPane.showMessageDialog(null, "Das eingegebene Spielfeld scheint ungültig zu sein!", "Fehler",
						JOptionPane.ERROR_MESSAGE);
				return 1;//Instant fertig! trotzdem Map Gen!
			}else {
				JOptionPane.showMessageDialog(null, "Das eingegebene Spielfeld scheint nicht lösbar zu sein!", "Fehler",
						JOptionPane.ERROR_MESSAGE);
				return -1;
			}
		}
		if (bSize == 0) {
			if(Mapsize==1) {
				JOptionPane.showMessageDialog(null, "Das eingegebene Spielfeld scheint nicht lösbar zu sein!", "Fehler",
						JOptionPane.ERROR_MESSAGE);
				return -1;
			}else {
				return 2;//spezieller Fall, wenn nur Agent auf dem Feld, welches > 2!
			}
		}
		adj = new LinkedList[bSize + 1];
		externList = new LinkedList[bSize + 1];
		externList[0] = new LinkedList<>(); 
		adj[0] = new LinkedList<>();
		String[] splitBattery;
		for (int i = 1; i < (bSize + 1); i++) {
			externList[i] = new LinkedList<>(); //Implement adjListen(en) mit dem Readen, von O(n) Batterien! Spaart O(n)
			adj[i] = new LinkedList<>();
			st = br.readLine();
			splitBattery = st.split(Pattern.quote(","));
			Battery battery = new Battery(Integer.parseInt(splitBattery[0]) - 1, Integer.parseInt(splitBattery[1]) - 1,
					Integer.parseInt(splitBattery[2]), i, 0, (Integer.parseInt(splitBattery[2]) <= 0) ? true : false, 0);
			initMap2d[battery.getY()][battery.getX()] = i;
			bList.add(battery);
			maxBatt = maxBatt + Integer.parseInt(splitBattery[2]);
			if(Integer.parseInt(splitBattery[2])<0){
				JOptionPane.showMessageDialog(null, "Bitte geben Sie einen gültigen Wert ein!", "Fehler",
						JOptionPane.ERROR_MESSAGE);
				return -1;
			}
		}//benötigt 1xO(n-1) -> setup
		start = System.nanoTime();
		for (int i = 1; i < bList.size(); i++) { 
			System.out.println("STARTING: " + i);
			Object[] posBatt = new Object[4];
			posBatt[0] = bList.get(i).getX();
			posBatt[1] = bList.get(i).getY();
			posBatt[2] = 0;
			GenerateEdges.genPosEdges(initMap2d, Mapsize, posBatt, (bList.get(i).getEnergy()), bList.get(i).getId(),
					adj); // o(4^e(n))
		}
		System.out.println("STARTING: " + 0);
		initMap2d[batteryAgent.getY()][batteryAgent.getX()] = 0;// kann in for loop!//nein, weil 0 = empty
		Object[] posBatt = new Object[4];
		posBatt[0] = bList.get(0).getX();
		posBatt[1] = bList.get(0).getY();
		posBatt[2] = 0;
		GenerateEdges.genPosEdges(initMap2d, Mapsize, posBatt, (bList.get(0).getEnergy()), bList.get(0).getId(), adj);
		SCC.generate(bList.size(), adj, bList); // O(V+E) -> Tarjan
		Helper.calcMax(SCC.SCC, bList);// muss nur 1x ausgerechnet werden!-> oder auch nicht xD -> Doch, weil extern// doch schon hat! Also anstatt n*n in 2d grid -> O(m) //Extern
		SCCmatrix = new int[SCC.SCC.size()][SCC.SCC.size()];
		int total = 0;
		for(int c = 0; c < maxList.size(); c++) {
			total = (total<maxList.get(c)[1])?maxList.get(c)[1]:total;
		}
		for (int i = 0; i < bList.size(); i++) {
			int[] posBattSCC = new int[3];
			posBattSCC[0] = bList.get(i).getX();
			posBattSCC[1] = bList.get(i).getY();
			posBattSCC[2] = 0;
			SCC.genEdgesSCC(initMap2d, Mapsize, posBattSCC, total, bList.get(i).getId(), SCCmatrix, bList.get(i).getSccid());
		}//O(n*Mapsize^2)
		for (int destination = 0; destination < SCC.SCC.size(); destination++) {
			int best = maxList.get(destination)[1];
			for (int i = 0; i < SCC.SCC.size(); i++) {
				int[] sumCalc = new int[2];
				sumCalc[0] = i;
				sumCalc[1] = maxList.get(i)[1];
				int z = GenerateEdges.BFSSSC(SCCmatrix, sumCalc, maxList.get(destination)[1], SCC.SCC.size(),
						destination);
				best = (z > best) ? z : best;
			}
			for (int i = 0; i < SCC.SCC.get(destination).size(); i++) { 
				Object[] posBatt2 = new Object[4];
				posBatt2[0] = bList.get(SCC.SCC.get(destination).get(i)).getX();
				posBatt2[1] = bList.get(SCC.SCC.get(destination).get(i)).getY();
				posBatt2[2] = 0;
				GenerateEdges.genPosEdges(initMap2d, Mapsize, posBatt2, best,
						bList.get(SCC.SCC.get(destination).get(i)).getId(), externList);
			}
		}
		Helper.printEdges(externList, externList.length);
		GuiController.energieLeft = Helper.calcSumEnergy(bList);
		return 0;//done Setup!
	}
	
	/**
	 * siehe Helper
	 */
	public static boolean isArryZero(int i) {
		return (i==0)?true:false;
	}
	
	/**
	 * siehe Helper
	 */
	public static boolean isAgentStart(int i, int x) {
		return (i==bList.get(0).getY()&&x==bList.get(0).getX())?true:false;
	}
}
