package rad.bin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import platform.Module;
import platform.Debug;
import java.util.*;

//import org.apache.log4j.Logger;

public class RunnerCorrect extends Runner {

	//static Logger localLogger = Logger.getLogger(RunnerCorrect.class);
	
	static Double Nmin;
	static long cnt=0;
	static HashMap<Double, Double> HMexp = null; 
	static Double[] ExpArr;
	
	
	@Override
	void runModule(Module modul) throws Exception {
		try {
		clearFile("correct");
		HashMap<String, Double[]> map = readFileToHashMapOfDoubleArrayWDesc("signal*.txt");
		Double[] alt = map.get("altitude");
		Double[] sig = map.get("signal");
		
		
		String  fileCorr = getOutFile("signal*.txt");
		Vector<Double[]> vect = new Vector<Double[]>();
		//vect.add(alt);
		//vect.add(sig);
		
		
		BufferedReader br = new BufferedReader (new FileReader(wrkPath+"/"+"count.txt"));
		
		int Nshots = Integer.parseInt(br.readLine());
		Double spar = getParDoubleByText("spatial_resolution"); 
		Double tslip = getParDoubleByText("tau_slip");
		Double[] corr_sig = new Double[sig.length];
		
		Double dT = 2*spar*1000/(300000000);
		
		/*int sigmin = (int) Math.round(sig[0]);
		int sigmax = sigmin;
		for (int i=0;i<=sig.length-1;i++){
			//corr_sig[i] = slip(sig[i], Nshots, tslip, dT);
			//corr_sig[i] = slipNew(sig[i], Nshots, tslip, dT);
			
			if (sig[i]<sigmin){
				sigmin = (int) Math.round(sig[i]);
			} else if (sig[i]>sigmax){
				sigmax = (int) Math.round(sig[i]);
			}
			//System.out.print("\r");
			//System.out.
		}
		if (ExpArr==null) {
			//HMexp = new HashMap<Double, Double>(sig.length);
			ExpArr = new Double[sigmax*10+10];
		}
		long expcnt=0;
		for(int j=sigmin;j<=sigmax*10;j++){
			Double razn =  (j * Math.exp((-j * tslip) / (Nshots * dT)));
			expcnt++;
			//HMexp.put(j,razn);
			ExpArr[j] = razn;
		}
		System.out.println("Exp count: " + expcnt);/**/
		
		for (int i=0;i<=sig.length-1;i++){
//			localLogger.info(" Correcting in progress, signal: "+i);
			corr_sig[i] = slipNew(sig[i], Nshots, tslip, dT); //slipNewHash
			//corr_sig[i] = slip(sig[i], Nshots, tslip, dT);
		}
		
		vect.removeAllElements();
		vect.add(alt);
		vect.add(corr_sig);
		
		writeVectorDoubleToTextFileWDesc(vect, wrkPath, "signal*.txt", "%12.8e");
		
		
	
		} catch (Exception e){
			//e.fillInStackTrace();
			
//			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
		
	}
	
	public static Double slipNew (Double M, int N ,Double TAU, Double deltaT){
		Double ns,  razn, RaznMin, upper;
		ns = M;	    
	    RaznMin = M - (ns * Math.exp((-ns * TAU) / (N * deltaT)));
		    if (Nmin == null||M>Nmin) {
			    upper = 10 * M;
		    } else {
		    	 upper = Math.min(M+((Nmin-M)*3.), 10 * M);
		    }
		    
		    Nmin = M;
		    
		    
		    do{
		      ns++;
		      cnt++;
		      razn = M - (ns * Math.exp((-ns * TAU) / (N * deltaT)));
		      
		      if (razn < 0) { 
		    	  ns = 20 * M;
		    	  } 
		      	else {
		         if (razn < RaznMin) {
			        RaznMin = razn;
			        Nmin = ns;
			      }
		      };
		    } while (!(ns >= upper)); 
		    return Nmin;
	}
	public static Double slipNewHash (Double M, int N ,Double TAU, Double deltaT){
		Double  razn, RaznMin, upper;
		//static Double Nmin;
			
		int ns = (int) Math.round(M);	    
	    RaznMin = M - ExpArr[ns];
		    if (Nmin == null||M>Nmin) {
			    upper = 10 * M;
		    } else {
		    	 upper = Math.min(M+((Nmin-M)*3.), 10 * M);
		    }
		    
		    Nmin = M;
		    
		    
		    do{
		      ns++;
		      cnt++;
		      razn = M - ExpArr[(int)Math.round(ns)];
		      
		      if (razn < 0) { 
		    	  ns = (int) Math.round(20 * M);
		    	  } 
		      	else {
		         if (razn < RaznMin) {
			        RaznMin = razn;
			        Nmin = new Double(ns);
			      }
		      };
		    } while (!(ns >= upper)); 
		    Debug.println("slipNewHash Signal: "+M+"; Nmin: "+Nmin+"; hash reading: "+cnt);
		    return Nmin;
	}
	public static Double slip (Double M, int N ,Double TAU, Double deltaT){
		Double ns, Nmin, razn, RaznMin;
		    ns = M;		    
		    RaznMin = M - (ns * Math.exp((-ns * TAU) / (N * deltaT)));
		    Nmin = M;
		    //long cnt=0;
		    do{
		      ns++;
		      cnt++;
		      razn = M - (ns * Math.exp((-ns * TAU) / (N * deltaT)));
		      if (razn < 0) { 
		    	  ns = 20 * M;
		    	  } 
		      	else {
		         if (razn < RaznMin) {
			        RaznMin = razn;
			        Nmin = ns;
			      }
		      };
		    } while (!(ns >= 10 * M)); 
		    Debug.println("slip Signal: "+M+"; Nmin: "+Nmin+"; razn count: "+cnt);
		    return Nmin;
	}
	public static Double slipHash (Double M, int N ,Double TAU, Double deltaT){
		Double ns, Nmin, razn, RaznMin;
		    ns = M;		    
		    RaznMin = M - HMexp.get(ns);
		    Nmin = M;
		    //long cnt=0;
		    do{
		      ns++;
		      cnt++;
		      razn = M - HMexp.get(ns);
		      if (razn < 0) { 
		    	  ns = 20 * M;
		    	  } 
		      	else {
		         if (razn < RaznMin) {
			        RaznMin = razn;
			        Nmin = ns;
			      }
		      };
		    } while (!(ns >= 2 * M)); 
		    Debug.println("slipHash Signal: "+M+"; Nmin: "+Nmin+"; hash reading: "+cnt);
		    return Nmin;
	}
}
