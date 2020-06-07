/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.EllipseBuilder;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.jan.Stromrallye;
import me.jan.graph.Battery;
import me.jan.heuristic.InformedHeuristik13;
import me.jan.utils.GenerateEdges;
import me.jan.utils.Helper;
import me.jan.utils.SCC;

public class GuiController {

	/** Agent **/
	@FXML
	public Text agentEnergie;//Zeigt die Energie des Roboters an.

	public static int agentBatt;//Aktueller Energiewert des Roboters.

	public static int xKoords, yKoords;//Aktuelle x und y Koordinate!

	public static ArrayList<Battery> bListdynamisch = new ArrayList<Battery>();//Dynamische Batterienlist
	public static ArrayList<Battery> bListinit = new ArrayList<Battery>();//Feste Batterienliste, des anfänglichen Spielfeldes.

	public static String Weg;//Platzhalter für den Endpfad

	public static String Bewegungsabfolge;//Hier wird der Endpfad gespeichert. Notation: RICHTUNG={HOCH, RUNTER, LINKS, RECHTS};

	public static int currentIndex;//Hilft den Skip Button(s), indem er den aktuellen Fortschritt(Anzahl Schritte) darstellt.

	/** Stage **/
	@FXML
	public AnchorPane aPane;//Um die Stage zu bekommen!

	@FXML
	public ScrollPane scrollPane;//Dient für das Scrollen.

	/** Datei auswählen **/
	public static FileChooser dateiAuswaehlen = new FileChooser();//Um die Beispieldatei einzulesen!

	/** Map **/
	@FXML
	public GridPane map2D = new GridPane();//Stellt das 2d Spielfeld dar!

	/** Aufgabenteil (b) **/

	@FXML
	public CheckBox leicht;//Schwierigkeitsstufe für den Aufgabenteil (b)

	@FXML
	public CheckBox mittel;//Schwierigkeitsstufe für den Aufgabenteil (b)

	@FXML
	public CheckBox schwierig;//Schwierigkeitsstufe für den Aufgabenteil (b)

	@FXML
	public CheckBox unmenschlich;//Schwierigkeitsstufe für den Aufgabenteil (b)

	@FXML
	public Button generiereFeld;//Erstellt das eigene Feld, anhand der Schwierigkeit.

	/** Aufgabenteil (a) **/

	@FXML
	public Button skipAnfang;//Lässt den Benutzer zum Anfang springen(Ausgangsfeld).

	@FXML
	public Button skipEnde;//Lässt den Benutzer zum Endfeld springen!

	@FXML
	public Button vorSchritt;//Lässt den Benutzer einen Schritt zurückgehen!

	@FXML
	public Button eingabe;//Lässt den Benutzer manuell ein Spielfeld eingeben!

	@FXML
	public Button ladeDatei;//Verbunden mit dem FileChooser, um die Beispieldatei einzulesen!

	@FXML
	public Text hoveredBatt;//Zeigt die zuletzt überfahrene Batterie an!

	public static int skipAnz = -1;//Stellt den Endwert, der Skip Funktion dar!
	public static int koords;//Spiegelt die Koordinate am Rand des Feldes wieder!
	public static boolean isGenerating = false;//Testet ob gerade ein Feld erstellt wurde!
	static int letzterx;//Speichert die letzte x-Koordinate
	static int letztery;//Speichert die letzte y-Koordinate
	public static int energieLeft;//Stellt die verbliebene Anzahl an Energie dar!
	public static int modeHeur;//Speichert dass Ergebnis, der Heuristik! 0 - Erfolgreich, 1/2 - Fehler!

	public void setupKeyListener() {//Wartet auf den Key Input des Benutzers für den Aufgabenteil (b)
		Main.scene.setOnKeyPressed(new EventHandler<javafx.scene.input.KeyEvent>() {
			@Override
			public void handle(javafx.scene.input.KeyEvent t) {
				if (GuiController.isGenerating) {//Testet, ob Userinput erlaubt ist!
					if (t.getCode() == t.getCode().UP) {
						geheHoch();//HOCH
					} else if (t.getCode() == t.getCode().DOWN) {
						geheRunter();//Runter
					} else if (t.getCode() == t.getCode().RIGHT) {
						geheRechts();//Rechts
					} else if (t.getCode() == t.getCode().LEFT) {
						geheLinks();//Links
					}
				}
			}
		});
	}

	/**
	 * <h1>Untersucht, auf andere Batterien, im zu generierenden Weg des Roboters.
	 * Abhängig von der Schwierigkeit</h1>
	 * 
	 * @param map 2D Map, mit Batterien (als Zahl dargestellt<<ID>>)
	 * @param posX Die Aktuelle x-Position des Roboter
	 * @param posY Die Aktuelle y-Position des Roboter
	 * @param randX Anzahl Schritte Links/Rechts
	 * @param randY Anzahl Schritte Hoch/Runter
	 * @param anzBatt ID der aktuellen Batterie, um nicht ausversehen sie zu ermitteln
	 * @return 0: Der Weg ist nicht Frei! <br>
	 *         1: Erfolgreich(Weg ist frei) horizontal->vertikal<br>
	 *         2: Erfolgreich(Weg ist frei) vertikal->horizontal
	 */
	public int testValid(int[][] map, int posX, int posY, int randX, int randY, int anzBatt) {
		boolean isValid = true;
		for (int i = posX; (randX <= 0)?(i >= posX + randX):(i <= posX + randX); i = (randX <= 0) ? i - 1 : i + 1) {// Rechts-Links
			if (map[i][posY] != 0 && map[i][posY] != -1 && map[i][posY] != anzBatt) {
				isValid = false;
			}
		}
		for (int i = posY; (randY <= 0)?(i >= posY + randY):(i <= posY + randY); i = (randY <= 0) ? i - 1 : i + 1) {// Hoch-Runter
			if (map[posX + randX][i] != 0 && map[posX + randX][i] != -1 && map[posX + randX][i] != anzBatt) {
				isValid = false;
			}
		}
		if (isValid)
			return 1;
		isValid = true;//Testet andersherum! Also horizontal->vertikal.... vertikal->horizontal
		for (int i = posY; (randY <= 0)?(i >= posY + randY):(i <= posY + randY); i = (randY <= 0) ? i - 1 : i + 1) {// Hoch-Runter
			if (map[posX][i] != 0 && map[posX][i] != -1 && map[posX][i] != anzBatt) {
				isValid = false;
			}
		}
		for (int i = posX; (randX <= 0)?(i >= posX + randX):(i <= posX + randX); i = (randX <= 0) ? i - 1 : i + 1) {// Rechts-Links
			if (map[i][posY + randY] != 0 && map[i][posY + randY] != -1 && map[i][posY + randY] != anzBatt) {
				isValid = false;
			}
		}
		return (isValid) ? 2 : 0;
	}

	public void genField(ActionEvent e) {//Testet auf die Schwierigkeit und erstellt ein zu lösendes Feld. Aufgabenteil (b)!
		setupKeyListener();
		isGenerating = true;
		currentIndex = 0;
		energieLeft = Helper.calcSumEnergy(bListinit);
		skipAnz = -1;
		koords = 0; //damit die Koordinaten beim Feldgenerieren gleich bleiben!
		int[][] map;//2D array
		int size = 0; //Map größe!
		boolean Valid = false;//solange keine Fehler beim Generieren auftreten. Dient zur Stabilität, um dem Benutzer keine nicht zu lösende Map zu geben!
		while (!Valid) {
			koords = 0;
			if (leicht.isSelected()) {// size 5-15
				size = ThreadLocalRandom.current().nextInt(5, 11);// Range für die Mapgröße: 5-10
				map = new int[size][size];// Parameter siehe Methode generateField bzw. javadoc/Methoden Doc.
				Valid = (generateField(70, 40, 30, 2, size, map, 4, 30, 30, 30, 200, 20, 0.25f, 50, 40, 0, 5) == 2) ? true
						: Valid;
			} else if (mittel.isSelected()) {
				size = ThreadLocalRandom.current().nextInt(7, 14);// 7-13
				map = new int[size][size];
				Valid = (generateField(75, 65, 15, 2, size, map, 6, 20, 20, 20, 190, 18, 0.75f, 80, 35, 15, 7) == 2) ? true
						: Valid;
			} else if (schwierig.isSelected()) {
				size = ThreadLocalRandom.current().nextInt(8, 17);// 8-16
				map = new int[size][size];
				Valid = (generateField(80, 75, 5, 1, size, map, 7, 12, 20, 15, 150, 15, 1.25f, 90, 20, 20, 9) == 2) ? true
						: Valid;
			} else if (unmenschlich.isSelected()) {// verdeutlicht, dass Prinzip mit Nodezu Node, warum dass dadurch schwieriger wird.
				size = ThreadLocalRandom.current().nextInt(9, 24);// 9-23
				map = new int[size][size];
				Valid = (generateField(100, 120, 2, 1, size, map, 8, 7, 20, 10, 100, 5, 2.0f, 100, 5, 35, 11) == 2) ? true
						: Valid;
			} else {
				isGenerating = false;
				JOptionPane.showMessageDialog(null, "Error", "Bitte wählen Sie eine Schwierigkeit aus!", 2);
				Valid = true;
			}
		}
		xKoords = letzterx;
		yKoords = letztery;
		Text roboter = (Text) getBattText(letztery, letzterx, map2D, 1);
		agentBatt = Integer.parseInt(roboter.getText());
		agentEnergie.setText(String.valueOf(agentBatt));
		Helper.copyBattery(bListinit, bListdynamisch);
		Helper.copyBattery(bListinit, Stromrallye.bList);
		koords = 0;
		Stromrallye.Mapsize = size;
		energieLeft = Helper.calcSumEnergy(bListinit);
		Bewegungsabfolge = Bewegungsabfolge.substring(1, Bewegungsabfolge.length());
		Weg = Bewegungsabfolge;
		System.out.println(Bewegungsabfolge);
	}

	/**
	 * @param percnotDone Wahrscheinlichkeit, dass keine Batterie mehr beschworen wird
	 * @param percNodezuNode Wahrscheinlichkeit, dass man eine Batterie mehrmals durchfährt
	 * @param percMuster Wahrscheinlichkeit, dass mehrmals die gleiche Anzahl an Energie verwendet wird
	 * @param hardlastNode Wahrscheinlichkeit, die letzte Batterie ausfindig zu machen -> Anzahl an Energie
	 * @param size Spielfeldgröße
	 * @param map 2D Array
	 * @param minAnzahl Minimale Anzahl an Batterien
	 * @param minusRound Abzug der Wahrscheinlichkeit pro Batterie, dass eine neue Batterie entsteht
	 * @param minusMuster Abzug der Wahrscheinlichkeit, bei einem Muster
	 * @param minusPercMuster Abzug der Wahrscheinlichkeit, dass nochmal ein Muster entsteht
	 * @param minusFaktor Abzug der Wahrscheinlichkeit in Abhängigkeit von der Größe der Ersatzbatterie
	 * @param minusNodezuNode Abzug der Wahrscheinlichkeit, bei vermehrten anfahren einer Batterie
	 * @param faktorGruppen Abzug der Wahrschenlichkeit bei Batterien nahe aneinander
	 * @param komplexMoves Wahrscheinlichkeit, dass man auch mal auch um eine Batterie herum muss, anstatt nur eine gerade Linie zu fahren/bewegen
	 * @param minusKomplex Abzug der Wahrscheinlichkeit, bei Komplexen Bewegungen
	 * @param percSelbst Wahrscheinlicheit, zu sich selbst. z.B. Node(0) -> Node(0)
	 * @return 1: Fehler <br>
	 *         2: Erfolgreich -> Spielfeld wurde erstellt!
	 **/
	public int generateField(float percnotDone, float percNodezuNode, float percMuster, int hardlastNode, int size,
			int[][] map, int minAnzahl, int minusRound, int minusMuster, int minusPercMuster, int minusFaktor,
			int minusNodezuNode, float faktorGruppen, int komplexMoves, int minusKomplex, int percSelbst, int anzKomplexMax) {
		drawField(size);
		int anzBatts = 0;
		Bewegungsabfolge = "";
		int posX = ThreadLocalRandom.current().nextInt(0, size);// letzter Befahrener Punkt: x
		int posY = ThreadLocalRandom.current().nextInt(0, size);// letzter Befahrener Punkt: y
		map[posX][posY] = -1;// Punkt befahren = -1
		int randX = ThreadLocalRandom.current().nextInt((-posX / hardlastNode), (((size - posX) - 1) / hardlastNode));// Anzahl Schritte, Links oder Rechts
		int randY = ThreadLocalRandom.current().nextInt((-posY / hardlastNode), (((size - posY) - 1) / hardlastNode));// Anzahl Schritte,Hoch oder Runter
		int limit = 0;
		while (randX == 0 && randY == 0) {
			randX = ThreadLocalRandom.current().nextInt((-posX / hardlastNode), (((size - posX) - 1) / hardlastNode)); // 2.
			randY = ThreadLocalRandom.current().nextInt((-posY / hardlastNode), (((size - posY) - 1) / hardlastNode)); // 2.
			if (++limit == 20)
				return 1;// failed
		} //bestimmt Schritte Hoch-Runter/Links-Rechts
		int energie = -2;
		int hRandX = (randX < 0) ? -randX : randX;
		for (int i = 0; i < hRandX; i++) {
			Bewegungsabfolge = ((randX < 0) ? ",Rechts" : ",Links") + Bewegungsabfolge;
		}
		int hRandY = (randY < 0) ? -randY : randY;
		for (int i = 0; i < hRandY; i++) {
			Bewegungsabfolge = ((randY < 0) ? ",Runter" : ",Hoch") + Bewegungsabfolge;
		}
		for (int i = posX; (randX <= 0)?(i >= posX + randX):(i <= posX + randX); i = (randX <= 0) ? i - 1 : i + 1) {// Links oder Rechts
			energie++;
			if (map[i][posY] != 0 && map[i][posY] != -1) {
			} else {
				map[i][posY] = -1;//Markiert map[x][y] als Befahren -> keine Neu Batterie!
			}
		}
		for (int i = posY; (randY <= 0)?(i >= posY + randY):(i <= posY + randY); i = (randY <= 0) ? i - 1 : i + 1) {// Runter oder Hoch
			energie++;
			if (map[i][posY] != 0 && map[i][posY] != -1) {
			} else {
				map[posX + randX][i] = -1;//Markiert map[x][y] als Befahren -> keine Neu Batterie!
			}
		}
		bListinit.clear();
		Stromrallye.bList.clear();
		bListdynamisch.clear();
		Battery bholder = new Battery(0, 0, 0, 0, 0, false, 0);//dummy Batterie/Startbatterie
		bListinit.add(bholder);
		drawBatt(posX, posY, randX, randY, size, energie);
		map[posX + randX][posY + randY] = ++anzBatts;//Setzt die ID der Batterie auf die 2D Map!
		percnotDone = percnotDone - (float) (energie * 200 / (size * size));
		Battery b = new Battery(posX + randX, posY + randY, energie, anzBatts, 0, false, 0);
		bListinit.add(b);
		try {
			return generateBatts(percnotDone, percNodezuNode, percMuster, hardlastNode, size, map, minAnzahl, anzBatts,
					posX, posY, randX, randY, minusRound, minusMuster, minusPercMuster, minusFaktor, minusNodezuNode,
					faktorGruppen, komplexMoves, minusKomplex, anzKomplexMax);//siehe Methoden Doc.
		} catch (Exception e) {
			return 1;
		} //Geht mit Fehlern um.
	}
	
	public boolean isDOness() {
		return false;
	}

	/**
	 * @param posX x-Position der Batterie im GridPane
	 * @param posY y-Position der Batterie im GridPane
	 * @param randX Anzahl Schritte nach Links/Rechts
	 * @param randY Anzahl Schritte nach Oben/Unten
	 * @param size Mapgröße
	 * @param energie Anzahl an Energie
	 */
	public void drawBatt(int posX, int posY, int randX, int randY, int size, int energie) {
		Text energieText = new Text();
		energieText.setFill(Color.BLACK);
		
		Ellipse batterie = new Ellipse();
		batterie.setCenterX(0);
		batterie.setCenterY(0);
		batterie.setRadiusX(map2D.getColumnConstraints().get(0).getPercentWidth() / size);
		batterie.setRadiusY(map2D.getColumnConstraints().get(0).getPercentWidth() / size);
		batterie.setStroke(Color.BLACK);
		batterie.setFill(Color.GREY);
		energieText.setText(String.valueOf(energie));
		energieText.setOnMouseEntered((event) -> {
			hoveredBatt.setText(energieText.getText());//Setzt hoveredBatt, beim Überfahren der Batt auf energie
		});
		energieText.setOnMouseEntered((event) -> {
			hoveredBatt.setText(energieText.getText());//Setzt hoveredBatt, beim Überfahren der Batt auf energie
		});
		map2D.add(batterie, posX + randX, posY + randY);//Aktuelle Position + Schritte
		map2D.add(energieText, posX + randX, posY + randY);//Aktuelle Position + Schritte
	}

	/**
	 * Siehe
	 * {@link #generateField(float percnotDone, float percNodezuNode, float percMuster, int hardlastNode, int size, int[][] map, int minAnzahl, int minusRound, int minusMuster, int minusPercMuster, int minusFaktor, int minusNodezuNode, float faktorGruppen, int komplexMoves, int minusKomplex, int percSelbst)}.
	 */
	public int generateBatts(float percnotDone, float percNodezuNode, float percMuster, int hardlastNode, int size,
			int[][] map, int minAnzahl, int anzBatts, int posX, int posY, int randX, int randY, int minusRound,
			int minusMuster, int minusPercMuster, int minusFaktor, int minusNodezuNode, float faktorGruppen,
			int komplexMoves, int minusKomplex, int anzKomplexMax) {
		int energie = 0;
		int newEnergie;
		Random random = new Random();
		int perc = -1;
		int mode = -1;//für testValid!
		letzterx = -1;
		letztery = -1;
		while (true) {
			percNodezuNode -= minusRound; //Standard 30!
			posX = posX + randX;
			posY = posY + randY;
			if (anzBatts >= minAnzahl) {
				perc = random.nextInt(100);
				if (perc > percnotDone) {//Letzte Batterie->Roboter
					Ellipse e = (Ellipse) getBatt(letztery, letzterx, map2D, 2);
					e.setFill(Color.GREEN);
					e.setRadiusX(592 / size / 2);
					e.setRadiusY(500 / size / 2);
					Text t = (Text) getBattText(letztery, letzterx, map2D, 2);
					t.setFill(Color.ALICEBLUE);
					t.setTranslateY(t.getTranslateY() + 5);
					Battery agent = new Battery(letzterx, letztery, Integer.parseInt(t.getText()), 0, 0, false, 0);
					bListinit.set(0, agent);
					bListinit.remove(bListinit.size() - 1);
					return 2;
				}
			}
			perc = random.nextInt(100);
			if (perc < percMuster) {//Muster
				percnotDone -= minusMuster;//Standard 20
				percMuster -= minusPercMuster;
				randX = ThreadLocalRandom.current().nextInt(-posX, (size - posX) - 1);
				randY = ThreadLocalRandom.current().nextInt(-posY, (size - posY) - 1);
				newEnergie = (randX <= 0) ? -randX : randX;
				newEnergie = newEnergie + ((randY <= 0) ? -randY : randY);
				while (randX == 0 && randY == 0 || (map[posX + randX][posY + randY] != 0)
						|| testValid(map, posX, posY, randX, randY, anzBatts) == 0 || newEnergie != energie) {
					randX = ThreadLocalRandom.current().nextInt(-posX, (size - posX) - 1);
					randY = ThreadLocalRandom.current().nextInt(-posY, (size - posY) - 1);
					newEnergie = (randX <= 0) ? -randX : randX;
					newEnergie = newEnergie + ((randY <= 0) ? -randY : randY);
					percnotDone -= 5;
					if (percnotDone < 0) {
						return 1;
					}
				}
				energie = -2;
				mode = testValid(map, posX, posY, randX, randY, anzBatts);
				if (mode == 1) {
					int hRandX = (randX < 0) ? -randX : randX;
					for (int i = 0; i < hRandX; i++) {
						Bewegungsabfolge = ((randX < 0) ? ",Rechts" : ",Links") + Bewegungsabfolge;
					}
					int hRandY = (randY < 0) ? -randY : randY;
					for (int i = 0; i < hRandY; i++) {
						Bewegungsabfolge = ((randY < 0) ? ",Runter" : ",Hoch") + Bewegungsabfolge;
					}
					for (int i = posX; (randX <= 0)?(i >= posX + randX):(i <= posX + randX); i = (randX <= 0) ? i - 1 : i + 1) {
						energie++;
						if (map[i][posY] != 0 && map[i][posY] != -1) {//Um eins zu behalten!
						} else {
							map[i][posY] = -1;
						}
					}
					for (int i = posY; (randY <= 0)?(i >= posY + randY):(i <= posY + randY); i = (randY <= 0) ? i - 1 : i + 1) {
						energie++;
						if (map[posX + randX][i] != 0 && map[posX + randX][i] != -1) {//Um eins zu behalten!
						} else {
							map[posX + randX][i] = -1;
						}
					}
				} else {
					int hRandY = (randY < 0) ? -randY : randY;
					for (int i = 0; i < hRandY; i++) {
						Bewegungsabfolge = ((randY < 0) ? ",Runter" : ",Hoch") + Bewegungsabfolge;
					}
					int hRandX = (randX < 0) ? -randX : randX;
					for (int i = 0; i < hRandX; i++) {
						Bewegungsabfolge = ((randX < 0) ? ",Rechts" : ",Links") + Bewegungsabfolge;
					}
					for (int i = posY; (randY <= 0)?(i >= posY + randY):(i <= posY + randY); i = (randY <= 0) ? i - 1 : i + 1) {
						energie++;
						if (map[posX][i] != 0 && map[posX][i] != -1) {//Um eins zu behalten!
						} else {
							map[posX][i] = -1;
						}
					}
					for (int i = posX; (randX <= 0)?(i >= posX + randX):(i <= posX + randX); i = (randX <= 0) ? i - 1 : i + 1) {
						energie++;
						if (map[i][posY + randY] != 0 && map[i][posY + randY] != -1) {//Um eins zu behalten!
						} else {
							map[i][posY + randY] = -1;
						}
					}
				}
				Text energieText = new Text("");
				energieText.setFill(Color.BLACK);
				Ellipse batterieNode = new Ellipse();
				batterieNode.setCenterX(0);
				batterieNode.setCenterY(0);
				batterieNode.setRadiusX(map2D.getColumnConstraints().get(0).getPercentWidth() / size);
				batterieNode.setRadiusY(map2D.getColumnConstraints().get(0).getPercentWidth() / size);
				batterieNode.setStroke(Color.BLACK);
				batterieNode.setFill(Color.GREY);
				energieText.setText(String.valueOf(energie));
				energieText.setOnMouseEntered((event) -> {
					hoveredBatt.setText(energieText.getText());
				});
				batterieNode.setOnMouseEntered((event) -> {
					hoveredBatt.setText(energieText.getText());
				});
				if (energie <= 0) {
					return 1;
				}
				letzterx = (posX + randX);
				letztery = (posY + randY);
				map2D.add(batterieNode, posX + randX, posY + randY);
				map2D.add(energieText, posX + randX, posY + randY);
				map[posX + randX][posY + randY] = ++anzBatts;
				percnotDone = percnotDone - (float) (energie * minusFaktor / (size * size));
				Battery b = new Battery(posX + randX, posY + randY, energie, anzBatts, 0, false, 0);
				bListinit.add(b);
			} else {
				if (energie >= (int) size * faktorGruppen) {
					int prob = random.nextInt();
					if (prob <= 70) {
						randX = ThreadLocalRandom.current().nextInt((int) -posX / 2, (int) ((size - posX) - 1) / 2);
						randY = ThreadLocalRandom.current().nextInt((int) -posY / 2, (int) ((size - posY) - 1) / 2);
					} else {
						randX = ThreadLocalRandom.current().nextInt(-posX, (size - posX) - 1);
						randY = ThreadLocalRandom.current().nextInt(-posY, (size - posY) - 1);
					}
				} else {
					randX = ThreadLocalRandom.current().nextInt(-posX, (size - posX) - 1);
					randY = ThreadLocalRandom.current().nextInt(-posY, (size - posY) - 1);
				}
				while (randX == 0 && randY == 0 || (map[posX + randX][posY + randY] != 0)) {
					if (energie >= (int) size * faktorGruppen) {
						int prob = random.nextInt();
						if (prob <= 70) {
							randX = ThreadLocalRandom.current().nextInt((int) -posX / 2, (int) ((size - posX) - 1) / 2);
							randY = ThreadLocalRandom.current().nextInt((int) -posY / 2, (int) ((size - posY) - 1) / 2);
						} else {
							randX = ThreadLocalRandom.current().nextInt(-posX, (size - posX) - 1);
							randY = ThreadLocalRandom.current().nextInt(-posY, (size - posY) - 1);
						}
					} else {
						randX = ThreadLocalRandom.current().nextInt(-posX, (size - posX) - 1);
						randY = ThreadLocalRandom.current().nextInt(-posY, (size - posY) - 1);
					}
					percnotDone -= 5;
					if (percnotDone < 0) {
						return 1;
					}
				}
				int energieholder = 0;
				energie = -2;
				int sameBattcheck = -1;
				int hRandX = (randX < 0) ? -randX : randX;
				for (int i = 0; i < hRandX; i++) {
					Bewegungsabfolge = ((randX < 0) ? ",Rechts" : ",Links") + Bewegungsabfolge;
				}
				int hRandY = (randY < 0) ? -randY : randY;
				for (int i = 0; i < hRandY; i++) {
					Bewegungsabfolge = ((randY < 0) ? ",Runter" : ",Hoch") + Bewegungsabfolge;
				}
				for (int i = posX; (randX <= 0)?(i >= posX + randX):(i <= posX + randX); i = (randX <= 0) ? i - 1 : i + 1) {// Links,Rechts
					energie++;
					if (map[i][posY] == anzBatts || map[i][posY] == sameBattcheck) {
					} else {
						if (map[i][posY] != 0 && map[i][posY] != -1 && map[i][posY] != anzBatts
								&& map[i][posY] != sameBattcheck) {// Um eins zu behalten!
							Text batterieText = (Text) getBattText(posY, i, map2D, 2);
							if (energieholder == 0) {
								energieholder = Integer.parseInt(batterieText.getText()) - 1;
								sameBattcheck = map[i][posY];
								batterieText.setText(String.valueOf(energie + 1));
								bListinit.get(Helper.findBattbyKoord(i, posY, bListinit)).setEnergy(energie + 1);
								energie = 0;
								percnotDone -= minusNodezuNode;
							} else {
								int enhold = energieholder;
								sameBattcheck = map[i][posY];
								energieholder = Integer.parseInt(batterieText.getText()) - 1;
								batterieText.setText(String.valueOf(energie + 1 + enhold));
								bListinit.get(Helper.findBattbyKoord(i, posY, bListinit))
										.setEnergy(energie + 1 + enhold);
								energie = 0;
								percnotDone -= minusNodezuNode;
							}
						} else {
							map[i][posY] = -1;
						}
					}
				}
				for (int i = posY; (randY <= 0)?(i >= posY + randY):(i <= posY + randY); i = (randY <= 0) ? i - 1 : i + 1) {// Runter,Hoch
					energie++;
					if (map[posX + randX][i] == anzBatts || map[posX + randX][i] == sameBattcheck) {
					} else {
						if (map[posX + randX][i] != 0 && map[posX + randX][i] != -1
								&& map[posX + randX][i] != anzBatts) {// Um eins zu behalten!
							Text batterieText = (Text) getBattText((posX + randX), i, map2D, 2);
							if (energieholder == 0) {
								sameBattcheck = map[posX + randX][i];
								energieholder = Integer.parseInt(batterieText.getText());
								batterieText.setText(String.valueOf(energie));
								bListinit.get(Helper.findBattbyKoord((posX + randX), i, bListinit))
									.setEnergy(energie);
								energie = 0;
								percnotDone -= minusNodezuNode;
							} else {
								sameBattcheck = map[posX + randX][i];
								int enhold = energieholder;
								energieholder = Integer.parseInt(batterieText.getText());
								batterieText.setText(String.valueOf(energie + enhold));
								bListinit.get(Helper.findBattbyKoord((posX + randX), i, bListinit))
										.setEnergy(energie + enhold);
								energie = 0;
								percnotDone -= minusNodezuNode;
							}
						} else {
							map[posX + randX][i] = -1;
						}
					}
				}
				letzterx = (posX + randX);
				letztery = (posY + randY);
				Text energieText = new Text("");
				energieText.setFill(Color.BLACK);
				Ellipse batterieNode = new Ellipse();
				batterieNode.setCenterX(0);
				batterieNode.setCenterY(0);
				batterieNode.setRadiusX(map2D.getColumnConstraints().get(0).getPercentWidth() / size);
				batterieNode.setRadiusY(map2D.getColumnConstraints().get(0).getPercentWidth() / size);
				batterieNode.setStroke(Color.BLACK);
				batterieNode.setFill(Color.GREY);
				if (energieholder != 0) {
					energieText.setText(String.valueOf(energie + energieholder));
					energie = energie + energieholder;
				} else {
					int perckomplex = random.nextInt(100);
					if (perckomplex < komplexMoves) {
						if (energie > 4) {// 1 und 2 gleich! 3= gegenetil von 2. dann 2 -> random mal
							int extraMoves = random.nextInt(anzKomplexMax);// 0-6
							System.out.println("Did it!");
							while ((extraMoves % 2) != 0) {
								extraMoves = random.nextInt(anzKomplexMax);
							}
							String[] split = Bewegungsabfolge.split(Pattern.quote(","));
							String holder = "";
							for (int j = 0; j < extraMoves / 2; j++) {
								holder = holder + GenerateEdges.gegenteil(split[2]) + "," + split[2] + ",";
							}
							Bewegungsabfolge = "," + split[1] + "," + split[2] + "," + holder + ",";
							for (int i = 3; i < (split.length); i++) {
								Bewegungsabfolge = Bewegungsabfolge + split[i] + ",";
							}
							Bewegungsabfolge = Bewegungsabfolge.replaceAll(",,", ",");
							energie += extraMoves;
							percnotDone -= minusKomplex;
						}
					}
				}
				energieText.setText(String.valueOf(energie));
				if (energie <= 0) {
					return 1;
				}
				energieText.setOnMouseEntered((event) -> {
					hoveredBatt.setText(energieText.getText());
				});
				batterieNode.setOnMouseEntered((event) -> {
					hoveredBatt.setText(energieText.getText());
				});
				map2D.add(batterieNode, posX + randX, posY + randY);
				map2D.add(energieText, posX + randX, posY + randY);
				map[posX + randX][posY + randY] = ++anzBatts;
				percnotDone = percnotDone - (float) (energie * minusFaktor / (size * size));
				Battery b = new Battery(posX + randX, posY + randY, energie, anzBatts, 0, false, 0);
				bListinit.add(b);
			}
		}
	}
	
	
	/** Button: Leicht **/
	public void leichtSelected(ActionEvent e) {
		mittel.setSelected(false);
		schwierig.setSelected(false);
		unmenschlich.setSelected(false);
	}

	/** Button: Mittel **/
	public void mittelSelected(ActionEvent e) {
		leicht.setSelected(false);
		schwierig.setSelected(false);
		unmenschlich.setSelected(false);
	}

	/** Button: Schwer **/
	public void schwerSelected(ActionEvent e) {
		leicht.setSelected(false);
		mittel.setSelected(false);
		unmenschlich.setSelected(false);
	}

	/** Button: Sehr Schwer **/
	public void unmenschlichSelected(ActionEvent e) {
		leicht.setSelected(false);
		mittel.setSelected(false);
		schwierig.setSelected(false);
	}

	/**Dateieingabe**/
	public void ladeDatei(ActionEvent e) {
		isGenerating = false;
		koords = 0;
		modeHeur = 0;
		dateiAuswaehlen.setTitle("Beispieleingabe");
		Stage stage = (Stage) aPane.getScene().getWindow();
		File eingabe = dateiAuswaehlen.showOpenDialog(stage);
		try {
			modeHeur = Stromrallye.startHeuristik(eingabe);
			GuiController.bListinit.clear();
			Helper.copyBattery(Stromrallye.bList, bListinit);
			Bewegungsabfolge = "";
			Helper.copyBattery(bListinit, bListdynamisch);
			drawField(Stromrallye.Mapsize);
			drawBatt(Stromrallye.Mapsize);
		} catch (IOException e1) {
			System.out.println("Keine Datei geladen!");
		}
	}

	/**
	 * Zeichnet ein Spielfeld/2D Grid anhand von size
	 * @param size Spielfeldgröße
	 */
	public void drawField(int size) {
		if (!map2D.getChildren().isEmpty()) {
			Node node = map2D.getChildren().get(0);
			map2D.getChildren().clear();
			map2D.getChildren().add(0, node);
		} // sichert die alten proportionen!
		map2D.getColumnConstraints().clear();
		map2D.getRowConstraints().clear();
		map2D.setGridLinesVisible(false);//da nur für Debug!
		map2D.setVisible(true);
		for (int i = 0; i < size + 1; i++) {
			ColumnConstraints column = new ColumnConstraints();
			RowConstraints row = new RowConstraints();
			column.setPercentWidth(100);
			row.setPercentHeight(100);
			column.setHalignment(HPos.CENTER);
			row.setValignment(VPos.CENTER);
			map2D.getColumnConstraints().add(column);
			map2D.getRowConstraints().add(row);
		} // Gen 2dMap
		for (int i = 0; i < size; i++) {
			for (int ic = 0; ic < size + 1; ic++) {
				if (ic == size) {
					koords++;
					Text text2 = new Text();
					text2.setFill(Color.BLACK);
					text2.setText(String.valueOf(koords));
					map2D.add(text2, i, ic);
					Text text23 = new Text();
					text23.setFill(Color.BLACK);
					text23.setText(String.valueOf(koords));
					map2D.add(text23, ic, i);
				} else {
					Pane paned = new Pane();// "507.0" prefWidth="592.0"
					paned.setMinWidth(10);
					paned.setMinHeight(10);
					paned.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
							new CornerRadii(2), new BorderWidths(0.9))));
					map2D.add(paned, i, ic);
				}
			}
		}
		scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setContent(map2D);
	}

	
	/**
	 * Zeichnet eine Batterie
	 * @param size Spielfeldgröße
	 */
	public void drawBatt(int size) {
		Ellipse batt = new Ellipse();
		batt.setCenterX(0);
		batt.setCenterY(0);
		batt.setRadiusX(592 / size / 2);
		batt.setRadiusY(507 / Stromrallye.Mapsize / 2);
		batt.setStroke(Color.BLACK);
		batt.setFill(Color.GREEN);
		Text battText = new Text(String.valueOf(bListinit.get(0).getEnergy()));
		battText.setTranslateY(battText.getTranslateY() + 10);
		battText.setFill(Color.ALICEBLUE);
		map2D.add(batt, Stromrallye.bList.get(0).getX(), Stromrallye.bList.get(0).getY());
		map2D.add(battText, Stromrallye.bList.get(0).getX(), Stromrallye.bList.get(0).getY());
		yKoords = Stromrallye.bList.get(0).getY();
		xKoords = Stromrallye.bList.get(0).getX();
		for (int i = 1; i < Stromrallye.bList.size(); i++) {
			Text battText2 = new Text(String.valueOf(bListinit.get(i).getEnergy()));
			battText2.setFill(Color.BLACK);
			Ellipse ellipseBatt = new Ellipse();
			ellipseBatt.setCenterX(0);
			ellipseBatt.setCenterY(0);
			ellipseBatt.setRadiusX(map2D.getColumnConstraints().get(0).getPercentWidth() / size);
			ellipseBatt.setRadiusY(map2D.getColumnConstraints().get(0).getPercentWidth() / size);
			ellipseBatt.setStroke(Color.BLACK);
			ellipseBatt.setFill(Color.GRAY);
			battText2.setOnMouseEntered((event) -> {
				hoveredBatt.setText(battText2.getText());//„Tooltip"
			});
			ellipseBatt.setOnMouseEntered((event) -> {
				hoveredBatt.setText(battText2.getText());//„Tooltip"
			});
			map2D.add(ellipseBatt, Stromrallye.bList.get(i).getX(), Stromrallye.bList.get(i).getY());
			map2D.add(battText2, Stromrallye.bList.get(i).getX(), Stromrallye.bList.get(i).getY());
		}
		scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setContent(map2D);
	}

	/**
	 * Berechnet den besten Weg, für das eingelesene Feld
	 * @param e ActionEvent
	 */
	public void startAgenten(ActionEvent e) {
		if (modeHeur == 2) {
			if (bListinit.get(0).getX() != 0) {
				for (int i = 0; i < (bListinit.get(0).getEnergy() / 2); i++) {
					Bewegungsabfolge = Bewegungsabfolge + "Links," + "Rechts,";
				}
			}
			System.out.println(Bewegungsabfolge);
			return;
		}
		currentIndex = 0;
		agentBatt = bListinit.get(0).getEnergy();
//		Helper.printGeoGruppen(Stromrallye.bList, Stromrallye.Mapsize, Stromrallye.initMap2d);
		Object[] agentHeur = new Object[7];//Zustand bzw. Element für die PQ!
		agentHeur[0] = 0;
		agentHeur[1] = Stromrallye.bList.get(0).getEnergy();
		agentHeur[2] = Stromrallye.maxBatt;
		agentHeur[3] = Stromrallye.adj;
		agentHeur[4] = Stromrallye.bList;
		agentHeur[5] = Stromrallye.SCCmatrix;
		agentHeur[6] = "";
		GuiController.bListdynamisch.clear();
		Helper.copyBattery(bListinit, GuiController.bListdynamisch);
		int size = Helper.getLastSize() + 1 - Helper.getFirstSize();// TODO: weiter mit min.
		if (size % 2 == 0) {//Gruppiert die 2D Map!
			Helper.generateEvenMap(Helper.getFirstSize(), Helper.getLastSize() + 1);
		} else {
			Helper.generateUnEvenMap(Helper.getFirstSize(), Helper.getLastSize() + 1);
		}
		String path = InformedHeuristik13.initGraph(agentHeur, Stromrallye.SCCmatrix,
				(SCC.SCC.size() == 2) ? true : false);//Berechnet einen korrekten Weg!
		Weg = path;
		System.out.println(System.nanoTime() - Stromrallye.start + "ns");
		System.out.println(path);
		if (path.equals("Nichts!")) {
			JOptionPane.showMessageDialog(null, "Das eingegebene Spielfeld scheint nicht lösbar zu sein!", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		} else {
			String[] split = path.split(Pattern.quote(","));
			computePath(path, 0, split.length, split);//Springt automatisch zum Endzustand
		}

	}

	/**
	 * Springt zum Ende/Endzustand
	 */
	public void skipEnd(ActionEvent e) {
		if (isGenerating) {
			if (currentIndex != Bewegungsabfolge.split(Pattern.quote(",")).length) {
				skipAnz = Bewegungsabfolge.split(Pattern.quote(",")).length;
				computePathOne(Bewegungsabfolge, currentIndex, Bewegungsabfolge.split(Pattern.quote(",")).length);
				currentIndex = Bewegungsabfolge.split(Pattern.quote(",")).length;
			}
		} else {
			skipAnz = -1;
			String[] split = Weg.split(Pattern.quote(","));
			if (currentIndex != split.length)
				computePath(Weg, currentIndex, split.length, split);
		}
	}

	/**
	 * Stellt den Ausgangszustand her!
	 */
	public void goInit(ActionEvent e) {
		koords = 0;
		energieLeft = Helper.calcSumEnergy(bListinit);
		currentIndex = 0;
		skipAnz = -1;
		agentBatt = bListinit.get(0).getEnergy();
		agentEnergie.setText(String.valueOf(agentBatt) + " Energie");
		GuiController.bListdynamisch.clear();
		Helper.copyBattery(bListinit, bListdynamisch);
		drawField(Stromrallye.Mapsize);
		drawBatt(Stromrallye.Mapsize);
	}

	/**
	 * Dient zum Springen zu/zwischen Zuständen
	 * @param path Wegholder
	 * @param von Anfangszustand in Ganzzahl
	 * @param bis Endzustand in Ganzzahl
	 * @param split Weg Array, geteilt in ','
	 */
	public void computePath(String path, int von, int bis, String[] split) {
		for (int i = von; i < bis; i++) {
			String[] splitNode = split[i].split(Pattern.quote("-"));
			for (int x = 0; x < Stromrallye.externList[Integer.parseInt(splitNode[0])].size(); x++) {
				if (Stromrallye.externList[Integer.parseInt(splitNode[0])].get(x).getNach() == Integer
						.parseInt(splitNode[1])) {
					String[] s = Stromrallye.externList[Integer.parseInt(splitNode[0])].get(x).getPath().split(",");
					if (Stromrallye.externList[Integer.parseInt(splitNode[0])].get(x).getKosten() != Integer
							.parseInt(splitNode[2])) {
						if (Stromrallye.externList[Integer.parseInt(splitNode[0])].get(x).isExpendable()) {
							if ((Stromrallye.externList[Integer.parseInt(splitNode[0])].get(x).getKosten() == 1)
									|| (Stromrallye.externList[Integer.parseInt(splitNode[0])].get(x)
											.getKosten() == 2)) {
								s = Stromrallye.externList[Integer.parseInt(splitNode[0])].get(x).getPathException()
										.split(",");
							}
							String pathssdf = "";
							int cntt = Integer.parseInt(splitNode[2]);
							int kosten = Stromrallye.externList[Integer.parseInt(splitNode[0])].get(x).getKosten();
							String goIn = s[s.length - 1];
							s[s.length - 1] = GenerateEdges.gegenteil(s[s.length - 2]);
							for (int j = 0; j < s.length; j++)
								pathssdf = pathssdf + s[j] + ",";
							for (int k = kosten; k <= cntt - 2; k = k + 2)
								pathssdf = pathssdf + s[s.length - 2] + ","
										+ ((k + 2 <= cntt - 2) ? (GenerateEdges.gegenteil(s[s.length - 2]) + ",") : "");
							pathssdf = pathssdf + goIn + ",";
							s = pathssdf.split(Pattern.quote(","));
							PathHelper(s);
							String holder = "";
							String[] splits = Bewegungsabfolge.split(Pattern.quote(","));
							for (int icc = 0; icc < splits.length - 2; icc++) {
								holder = holder + "," + splits[icc];
							}
							System.out.println(Bewegungsabfolge);
							break;
						}
					} else {
						PathHelper(s);
						System.out.println(Bewegungsabfolge);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Übersetzt den Weg/String in Aktion
	 */
	public void PathHelper(String[] s) {
		for (int c = 0; c < s.length; c++) {
			if (s[c].equals("Runter")) {
				geheRunter();
				Bewegungsabfolge = Bewegungsabfolge + "Runter,";
			} else if (s[c].equals("Hoch")) {
				geheHoch();
				Bewegungsabfolge = Bewegungsabfolge + "Hoch,";
			} else if (s[c].equals("Rechts")) {
				geheRechts();
				Bewegungsabfolge = Bewegungsabfolge + "Rechts,";
			} else if (s[c].equals("Links")) {
				geheLinks();
				Bewegungsabfolge = Bewegungsabfolge + "Links,";
			}
		}
	}

	/*
	 * Manuelleeingabe
	 */
	public void sysoutBew(ActionEvent e) throws IOException {
		File bspDatei = new File("bsp0.txt");
	    bspDatei.createNewFile();
		FileWriter input = new FileWriter(bspDatei);
		input.write(JOptionPane.showInputDialog("Bitte geben Sie die Größe des Spielfeldes ein!"));
		input.write("\r\n");
		input.write(JOptionPane.showInputDialog("Roboter"));
		input.write("\r\n");
		String anz = JOptionPane.showInputDialog("Anzahl an Ersatzbatterien");
		input.write(anz);
		input.write("\r\n");
		int f = Integer.parseInt(anz);
		for(int i = 0; i < f; i++) {
			input.write(JOptionPane.showInputDialog("Ersatzbatterie"));
			input.write("\r\n");
		}
		input.close();
		try {
			modeHeur = Stromrallye.startHeuristik(bspDatei);
			GuiController.bListinit.clear();
			Helper.copyBattery(Stromrallye.bList, bListinit);
			Bewegungsabfolge = "";
			Helper.copyBattery(bListinit, bListdynamisch);
			drawField(Stromrallye.Mapsize);
			drawBatt(Stromrallye.Mapsize);
		} catch (IOException e1) {
			System.out.println("Keine Datei geladen!");
		}
		
	}

	/**
	 * @param path Weg in String
	 * @param von Anfangszustand in Ganzzahl
	 * @param bis Endzustand in Ganzzahl
	 */
	public void computePathOne(String path, int von, int bis) {
		for (int i = von; i < bis; i++) {
			String[] split = path.split(Pattern.quote(","));
			if (split[i].equals("Runter")) {
				geheRunter();
			} else if (split[i].equals("Hoch")) {
				geheHoch();
			} else if (split[i].equals("Rechts")) {
				geheRechts();
			} else if (split[i].equals("Links")) {
				geheLinks();
			}
		}
	}

	/**
	 * Hilft dem Benutzer im Aufgabenteil (b)
	 */
	public void showTipp(ActionEvent e) {
		if (isGenerating) {
			String[] split = Bewegungsabfolge.split(Pattern.quote(","));
			JOptionPane.showMessageDialog(null, ("Im optimalen Pfad, müsste man jetzt: " + split[skipAnz + 1]), "Tipp:",
					0);
		}
	}

	/**
	 * Führt einen Zustandswechsel aus
	 */
	public void onestep(ActionEvent e) {
		if (isDone(agentBatt - 1, energieLeft) && isGenerating) {

		} else {
			if (currentIndex != Bewegungsabfolge.split(Pattern.quote(",")).length) {
				computePathOne(Bewegungsabfolge, skipAnz + 1, skipAnz + 2);
			}
		}
	}

	/**
	 * @param row Reihe 
	 * @param column Spalte
	 * @param grid 2D Map
	 * @param mode Modus(1-2) <br> 1:Roboter <br> 2:Ersatzbatterie
	 * @return Batterie als Node
	 */
	public Node getBatt(int row, int column, GridPane grid, int mode) {
		Node batt = null;
		ObservableList<Node> batts = grid.getChildren();//https://docs.oracle.com/javase/8/javafx/api/javafx/collections/ObservableList.html
		for (Node node : batts) {
			if (node instanceof Ellipse && grid.getRowIndex(node) == row && grid.getColumnIndex(node) == column){
				if(((Ellipse) node).getFill().equals(Color.GRAY)&&mode==2) {
					batt = node;
					return batt;
				}else if(((Ellipse) node).getFill().equals(Color.GREEN)&&mode==1){
					batt = node;
					return batt;
				}
			}
		}
		return batt;
	}

	/**
	 * @param row Reihe 
	 * @param column Spalte
	 * @param grid 2D Map
	 * @param mode Modus(1-2) <br> 1:Roboter <br> 2:Ersatzbatterie
	 */
	public void removeBatt(int row, int column, GridPane grid, int mode) {
		ObservableList<Node> batt = grid.getChildren();
		for (Node node : batt) {
			if (node instanceof Ellipse && grid.getRowIndex(node) == row && grid.getColumnIndex(node) == column) {
				if(((Ellipse) node).getFill().equals(Color.GRAY)&&mode==2) {
					Ellipse el = (Ellipse) node;
					grid.getChildren().remove(el);
					break;
				}else if(((Ellipse) node).getFill().equals(Color.GREEN)&&mode==1){
					Ellipse el = (Ellipse) node;
					grid.getChildren().remove(el);
					break;
				}

			}
		}
	}

	/**
	 * @param row Reihe 
	 * @param column Spalte
	 * @param grid 2D Map
	 * @param mode Modus(1-2) <br> 1:Roboter <br> 2:Ersatzbatterie
	 * @return Energie als Text
	 */
	public Node getBattText(int row, int column, GridPane grid, int mode) {
		Node batts = null;
		ObservableList<Node> batt = grid.getChildren();
		for (Node node : batt) {
			if (node instanceof Text && grid.getRowIndex(node) == row && grid.getColumnIndex(node) == column){
				if(((Text) node).getFill().equals(Color.ALICEBLUE)&&mode==1) {
					batts = node;
					return batts;
				}else if(((Text) node).getFill().equals(Color.BLACK)&&mode==2) {
					batts = node;
					return batts;
				}
			}
		}
		return batts;
	}
	

	/**
	 * @param row Reihe 
	 * @param column Spalte
	 * @param grid 2D Map
	 * @param mode Modus(1-2) <br> 1:Roboter <br> 2:Ersatzbatterie
	 */
	public void removeBattText(int row, int column, GridPane grid, int mode) {
		ObservableList<Node> childrens = grid.getChildren();
		for (Node node : childrens) {
			if (node instanceof Text && grid.getRowIndex(node) == row && grid.getColumnIndex(node) == column){
				if(((Text) node).getFill().equals(Color.ALICEBLUE)&&mode==1){
					Text el = (Text) node;
					grid.getChildren().remove(el);
					break;
				}else if(((Text) node).getFill().equals(Color.BLACK)&&mode==2) {
					Text el = (Text) node;
					grid.getChildren().remove(el);
					break;
				}
			}
		}
	}

	/**
	 * Teste den Zustand auf Vollständigkeit
	 */
	public static boolean isDone(int energie, int maxEnergie) {
		if (maxEnergie == 0) {
			JOptionPane.showMessageDialog(null,
					"Sie haben gewonnen! Erstellen Sie entweder ein neues Spielfeld, oder setzten Sie es mit dem: ← Button zurück",
					"Glückwunsch", 0);
			return true;
		} else {
			if (energie < 0) {
				JOptionPane.showMessageDialog(null,
						"Bitte starten sie dass Spiel neu oder setzen sie dass Feld zurück, mit dem Button: ←",
						"Game Over!", 0);
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Bereinigt die Map
	 */
	public void clear(ActionEvent e) {
		if (!map2D.getChildren().isEmpty()) {
			Node node = map2D.getChildren().get(0);
			map2D.getChildren().clear();
			map2D.getChildren().add(0, node);
		}
	}
	
	/**
	 * Führt Move: Hoch/Runter aus
	 * @param limit Teste auf Arraybounds
	 * @param mode Modus <br> 1:Runter <br> 2:Hoch
	 */
	public void geheVertikal(int limit, int mode) {//limit, Hoch: 0  | Runter: (Stromrallye.Mapsize-1)
		if (isDone(agentBatt - 1, energieLeft) && isGenerating || yKoords == limit) {}
		else {
			skipAnz++;
			Ellipse battNode = (Ellipse) getBatt(yKoords, xKoords, map2D,1);
			Text battText = (Text) getBattText(yKoords, xKoords, map2D, 1);
			removeBatt(yKoords, xKoords, map2D, 1);
			removeBattText(yKoords, xKoords, map2D, 1);
			map2D.add(battNode, xKoords, (mode==1)?(yKoords+1):(yKoords-1));
			agentBatt--;
			energieLeft--;
			battText.setFill(Color.ALICEBLUE);
			battText.setText(String.valueOf(agentBatt));
			map2D.add(battText, xKoords, (mode==1)?(yKoords+1):(yKoords-1));
			yKoords = (mode==1)?(yKoords+1):(yKoords-1);
			hasCollision(yKoords, xKoords, map2D);
			battText.setText(String.valueOf(agentBatt));
			agentEnergie.setText(String.valueOf(agentBatt) + " Energie");
		}
	}
	
	
	/**
	 * Führt Move: Links/Rechts aus
	 * @param limit Teste auf Arraybounds
	 * @param mode Modus <br> 1:Links <br> 2:Rechts
	 */
	public void geheHorizontal(int limit, int mode) {//limit:Links : 0 | Rechts: (Stromrallye.Mapsize-1)
		if (isDone(agentBatt - 1, energieLeft) && isGenerating || xKoords == limit) {} 
		else {
			skipAnz++;
			Ellipse battNode = (Ellipse) getBatt(yKoords, xKoords, map2D, 1);
			Text battText = (Text) getBattText(yKoords, xKoords, map2D, 1);
			removeBatt(yKoords, xKoords, map2D, 1);
			removeBattText(yKoords, xKoords, map2D, 1);
			map2D.add(battNode, (mode==1)?xKoords-1:xKoords+1, yKoords);
			agentBatt--;
			energieLeft--;
			battText.setFill(Color.ALICEBLUE);
			battText.setText(String.valueOf(agentBatt));
			map2D.add(battText, (mode==1)?xKoords-1:xKoords+1, yKoords);
			xKoords = (mode==1)?(xKoords-1):(xKoords+1);
			hasCollision(yKoords, xKoords, map2D);
			battText.setText(String.valueOf(agentBatt));
			agentEnergie.setText(String.valueOf(agentBatt) + " Energie");
		}
	}

	/**
	 * Testet auf eine Kollision
	 * @param row Reihe
	 * @param column Spalte
	 * @param gridPane 2D Map
	 */
	public void hasCollision(final int row, final int column, GridPane gridPane) {
		for (int i = 1; i < bListdynamisch.size(); i++) {
			if (column == bListdynamisch.get(i).getX() && row == bListdynamisch.get(i).getY()) {
				try {
					Text battText1 = (Text) getBattText(row, column, gridPane, 1);
					Text battText2 = (Text) getBattText(row, column, gridPane, 2);
					agentBatt = bListdynamisch.get(i).getEnergy();
					String hold = battText1.getText();
					int energy = Integer.parseInt(battText1.getText());
					bListdynamisch.get(i).setEnergy(energy);
					battText2.setText(hold);
					battText2.setFill(Color.BLACK);
					agentEnergie.setText(String.valueOf(agentBatt) + " Energie");
					removeBattText(row, column, gridPane, 2);
					map2D.add(battText2, column, row);
				} catch (Exception e) {
					agentEnergie.setText(String.valueOf(agentBatt) + " Energie");
				}
			}
		}
	}
	
	/**
	 * ButtonInput Hoch
	 */
	public void geheHoch() {
		geheVertikal(0, 2);//HOCH
	}
	
	/**
	 * ButtonInput Runter
	 */
	public void geheRunter() {
		geheVertikal((Stromrallye.Mapsize-1), 1);//Runter
	}
	
	/**
	 * ButtonInput Links
	 */
	public void geheLinks() {
		geheHorizontal(0, 1);//Links
	}
	
	/**
	 * ButtonInput Rechts
	 */
	public void geheRechts() {
		geheHorizontal((Stromrallye.Mapsize-1), 2);//Rechts
	}
}