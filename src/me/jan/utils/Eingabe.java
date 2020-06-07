/**@author ${Jan-Luca Gruber}
 * Runde 2: Aufgabe 1 - Stromrallye
 * **/

package me.jan.utils;

import java.util.Scanner;
import java.util.regex.Pattern;

public class Eingabe {
	
	static Scanner sc = new Scanner(System.in);
	
	/**
	 * Manuelle Eingabe
	 * @return
	 */
	public static int Mapsize() {
		System.out.println("Bitte geben Sie die Größe des Spielbrettes an: ");
		try {
			return sc.nextInt(); //Größe des Spielbretts
		}catch(Exception e) {
			System.out.println("Bitte geben Sie eine gültige Zahl ein!");
			System.exit(0);
		}
		return 0;
	}
	
	public static int bSize() {
		return sc.nextInt();
	}
	
	public static String[] posObject() {
		String pwb = sc.next();//position with Battery
		String[] split = pwb.split(Pattern.quote(","));
		return (split.length==3)?split:null;
	}
}
