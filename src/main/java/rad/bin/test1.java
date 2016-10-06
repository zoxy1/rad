package rad.bin;


import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import platform.Module;

public class test1 extends Runner{
	static Logger localLogger = Logger.getLogger(RunnerAfterCount.class);

	@Override
	void runModule(Module modul) throws Exception {
		try {
			Double[] a = new Double[3];
			Double[] b = new Double[3];
			for (int i=0; i<3; i++)
				a[i]= i+2.0;
			for (int i=0; i<3; i++)
				b[i]= i+3.1;
				
			Vector<Double[]> vect = new Vector<Double[]>();
			vect.add(a);
			vect.add(b);
			
			writeVectorDoubleToTextFile(vect, "output.new.txt", "%12.8e");
			
			//File newF  = getFile("output*.txt");
			
			HashMap<String, Double[]> map = readFileToHashMapOfDoubleArray("output*.txt");
						
			Double[] d=map.get("d"); //press
			throwHashMapException(d, "d");
			
			Vector<Double[]> vect1 = new Vector<Double[]>();
			vect1.add(d);
			writeVectorDoubleToTextFile(vect1, "output1.txt", "%12.8e");
		} catch (Exception e) {

			localLogger.fatal(e.toString());
			throw e;
		}
		 
		
		    
	} 
		    }
		    
		    
		  
		    
		