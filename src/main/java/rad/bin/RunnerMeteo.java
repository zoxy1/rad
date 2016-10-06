package rad.bin;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import platform.*;
import java.util.*;

public class RunnerMeteo extends Runner {

	static Logger localLogger = Logger.getLogger(RunnerMeteo.class);
	
	void runModule(Module module) throws Exception {
		try {
		clearFile("meteo");
		HashMap<String, Double[]> HMsignal = readFileToHashMapOfDoubleArrayWDesc("signal*.txt"); 
		//Double[] bkgr = HM.get("background_wave1");
		//System.out.println(bkgr);
		//Math.r
		
			
		Double lat = getParDoubleByText("latitude");
		Debug.println(lat);
		
		String hs;
		
		if (lat>=0.) {
			hs = "n";
		} else {
			hs = "s";
		}
		
		String month = getParStringByText("month").trim().toLowerCase();
		 //getParStringByText("hemisphere").trim().toLowerCase();

		
		if (month.equalsIgnoreCase("jan")||month.equalsIgnoreCase("1")) {
			month = "jan";
		} else if (month.equalsIgnoreCase("feb")||month.equalsIgnoreCase("2")) {
			month = "feb";
		} else if (month.equalsIgnoreCase("mar")||month.equalsIgnoreCase("3")) {
			month = "mar";
		} else if (month.equalsIgnoreCase("apr")||month.equalsIgnoreCase("4")) {
			month = "apr";
		} else if (month.equalsIgnoreCase("may")||month.equalsIgnoreCase("5")) {
			month = "may";
		} else if (month.equalsIgnoreCase("jun")||month.equalsIgnoreCase("6")) {
			month = "jun";
		} else if (month.equalsIgnoreCase("jul")||month.equalsIgnoreCase("7")) {
			month = "jul";
		} else if (month.equalsIgnoreCase("aug")||month.equalsIgnoreCase("8")) {
			month = "aug";
		} else if (month.equalsIgnoreCase("sep")||month.equalsIgnoreCase("9")) {
			month = "sep";
		} else if (month.equalsIgnoreCase("oct")||month.equalsIgnoreCase("10")) {
			month = "oct";
		} else if (month.equalsIgnoreCase("nov")||month.equalsIgnoreCase("11")) {
			month = "nov";
		} else if (month.equalsIgnoreCase("dec")||month.equalsIgnoreCase("12")) {
			month = "dec";
		}
		
		
		String model_path = getParStringByText("model_path");
		HashMap<String, Double[]> Hmodel = readFileToHashMapOfDoubleArray(model_path+"/cira86/" + hs+ "ht_" + month + ".txt");
		Double[] altModel;//  = Hmodel.get("geom_height2");
		throwHashMapException(altModel  = Hmodel.get("geom_height"), "geom_height");
		Double[] pressModelmbar  = Hmodel.get("pressure");
		Double[] pressModelatm  = new Double[pressModelmbar.length];
		
		Double[] altSignal  = HMsignal.get("altitude");
		double div = Math.floor( lat / 5);
		long idivLow = Math.round(div*5);
		long idivHig = Math.round((div+1)*5);
		Double[] tempModelHig = Hmodel.get(""+idivHig);
		Double[] tempModelLow = Hmodel.get(""+idivLow);
		Double[] tempModel = new Double[tempModelHig.length];
		for (int i=0;i<tempModelHig.length;i++){
			
			tempModel[i] = tempModelLow[i] + ((lat - idivLow)*(tempModelHig[i]-tempModelLow[i]))/(idivHig-idivLow);
			pressModelatm[i] = pressModelmbar[i]/1013;
			//System.out.println(" " + tempModelLow[i] + " " + tempModel[i] + " "  + tempModelHig[i]);
			
		}
		
		Double[] tempModelInterp = new Double[altSignal.length];
		Double[] pressModelInterpMbar = new Double[altSignal.length];
		Double[] pressModelInterpAtm = new Double[altSignal.length];
		
		int IER=0;
		RunnerUtils.lineInterp(altModel.length, altModel, tempModel, altSignal.length, altSignal, tempModelInterp, IER);
		RunnerUtils.lineInterp(altModel.length, altModel, pressModelmbar, altSignal.length, altSignal, pressModelInterpMbar, IER);
		RunnerUtils.lineInterp(altModel.length, altModel, pressModelatm, altSignal.length, altSignal, pressModelInterpAtm, IER);
		
		//Double[] tempModel  = Hmodel.get("temperature");
		
		Vector<Double[]> vect = new  Vector<Double[]>();
		
		//vect.add(altModel);
		//vect.add(tempModel);
		
		vect.add(altSignal);
		vect.add(pressModelInterpMbar);
		vect.add(pressModelInterpAtm);
		vect.add(tempModelInterp);
		
		writeVectorDoubleToTextFileWDesc(vect,"meteo_model.txt", "%12.8e");
		} catch (Exception e){
			//e.fillInStackTrace();
			
			//e.printStackTrace();
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
		
		
	} 
	

}
