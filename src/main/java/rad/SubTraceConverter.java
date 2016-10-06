package rad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;

import platform.Debug;
import rad.bin.Runner;

public class SubTraceConverter {

	private static String subOutName;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		PropertyConfigurator.configure(System.getProperty("user.dir")
				+ "/log4j/log4j.properties");

		Locale.setDefault(Locale.US);
		TimeZone tz = TimeZone.getTimeZone("GMT");
		TimeZone.setDefault(tz);

		HashMap<String, Double[]> HM = readFileToHashMapOfDoubleArrayWDesc("/share/home/biv/RAD/resources/subTraceProp.out");
		Double[] timearr = HM.get("JulianTime");
		subOutName = "/share/home/biv/RAD/resources/subTraceProp4NCF-oct.out";
		for (int i = 0; i < timearr.length; i++) {
			timearr[i] = timearr[i]+61.;
		}
		
		Double[] timearr2 = new Double[timearr.length];
		GregorianCalendar gc = new GregorianCalendar();
		// System.out.println(timearr[0]);
		for (int i = 0; i < timearr.length; i++) {
			timearr2[i] = new Double(Runner
					.convertJulianToGregorianCalendar(timearr[i]));
			gc.setTimeInMillis(timearr2[i].longValue());
			System.out.println(gc.getTime());
		}
		Vector<Double[]> vect = new Vector<Double[]>();
		vect.add(timearr2);
		
		vect.add(timearr);

		{
			Double[] latarr = HM.get("Latitude");
			vect.add(latarr);
		}

		{
			Double[] lonarr = HM.get("Longitude");
			vect.add(lonarr);
		}

		{
			Double[] ALTarr = HM.get("Altitude");
			vect.add(ALTarr);
		}
		{
			Double[] hemarr = HM.get("Hemisphere");
			vect.add(hemarr);
		}

		{
			Double[] hemarr = HM.get("SolarAzimut(CW)");
			vect.add(hemarr);
		}

		{
			Double[] hemarr = HM.get("SolarElevAngle");
			vect.add(hemarr);
		}
		{
			Double[] hemarr = HM.get("TimeZone");
			vect.add(hemarr);
		}
		try {
			FileWriter fw = new FileWriter(subOutName);
			// fw.sy
			BufferedWriter fil = new BufferedWriter(fw);
			
			Double[] arr = (Double[]) vect.get(0);
			String str = "#0Time #1JulianTime	#2Latitude	#3Longitude	#4Altitude	#5Hemisphere	#6SolarAzimut(CW)	#7SolarElevAngle	#8TimeZone" + "\r\n";
			fw.write(str);
			str = "";
			for (int j = 0; j <= arr.length - 1; j++) {
				String wrStr = "";
				str = "";
				for (int i = 0; i <= vect.size() - 1; i++) {
					arr = vect.get(i);
					wrStr = wrStr + "\t" + String.format("%14.10f", arr[j]);
				}
				wrStr = wrStr.trim() + "\r\n";
				fil.write(wrStr);
			}
			fil.flush();
			fil.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static HashMap<String, Double[]> readFileToHashMapOfDoubleArrayWDesc(
			String fName) {
		ArrayList<String[]> al;
		ArrayList<String> nl = null;
		Node inFileNode = null;
		HashMap<String, Double[]> hm = null;
		File file = null;

		try {
			// mdNode.normalize();
			// inFileNode = XPathAPI.selectSingleNode(mdNode,
			// ".//inFile/name[normalize-space(./text())=\""
			// + fName.trim() + "\"]");// .getParentNode();
			// NodeList NL = XPathAPI.selectNodeList(mdNode,
			// ".//inFile/*[normalize-space(./text())=\""+fName.trim()+"\"]");
			file = new File(fName.trim());
			// if (Debug.USE_CASHE && CasheOfHmap != null) {
			// if ((hm = CasheOfHmap.get(file.getAbsolutePath().trim()
			// .toLowerCase())) != null) {
			// return hm;
			// }
			//
			// }
			// String beginRowText;
			Node beginRowNode = null;
			Node reverseNode = null;
			/*
			 * if (inFileNode != null) { inFileNode =
			 * inFileNode.getParentNode(); beginRowNode = XPathAPI
			 * .selectSingleNode(inFileNode, "startRow"); reverseNode = XPathAPI
			 * .selectSingleNode(inFileNode, "isReverse"); }
			 */
			Boolean isReverse = (reverseNode != null) ? true : false;
			String descString;
			String[] namesString;
			BufferedReader BR = new BufferedReader(new FileReader(file));
			descString = BR.readLine();
			// namesString = descString.trim().split("#\\d\\s");
			// namesString = descString.trim().split("\\d\\s+");
			namesString = descString.trim().split("\\s*#\\d\\s*");
			nl = new ArrayList<String>();
			for (int i = 0; i < namesString.length; i++) {
				if (namesString[i].length() != 0) {
					nl.add(namesString[i].trim());
				}

			}

			if (beginRowNode != null) {
				al = Runner.readAndSplitTextFileToArrayList(file, Integer
						.parseInt(beginRowNode.getTextContent().trim()));
			} else {
				al = Runner.readAndSplitTextFileToArrayList(file, 1);
			}
			// System.cl

			// nl = getColArrayListForInFileNodeByName(inFileNode);

			hm = Runner.convertArrayListOfStringToHashMapOfDoubleArrays(al, nl,
					isReverse);
			// writeArrayListOfStringToFile(nl, file.getAbsolutePath()+".dsc" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * if (Debug.USE_CASHE) {
		 * 
		 * if (CasheOfHmap == null) { CasheOfHmap = new HashMap<String, HashMap<String,
		 * Double[]>>(); }
		 * 
		 * //CasheOfHmap.put(file.getName(), hm); }
		 */
		return hm;
	};

}
