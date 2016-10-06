package rad.bin;
import platform.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;


import platform.Module;

public class SplitterTrace extends Runner {
	static Logger localLogger = Logger.getLogger(SplitterTrace.class);
	@Override
	void runModule(Module modul) throws Exception {
		try {
		File trace = getFile("subTraceInfo.out"); 
		BufferedReader traceReader= new BufferedReader(new FileReader(trace));
		
		String headerStr = traceReader.readLine();
		String headerNames[] = headerStr.split("\\s+#\\d*\\s*");
		HashMap<String, Double[]> map = readFileToHashMapOfDoubleArrayWDesc("subTraceInfo.out");;
		//Debug.println(headerStr);
		Double[] Lat = map.get("Latitude");
		Double[] Lon = map.get("Longitude");
		Double[] SolEA = map.get("SolarElevAngle");
		
		for (int i=0; i< Lat.length; i++){
			
			String argStr[];
			String parStr = traceReader.readLine();
			Vector<String> LS = new Vector<String>();
			
			//LS.add(Debug.getCmdKeyString());
			//LS.add(Conf.getProperty("initial.cmd.arg.string"));
			for (int j = 0; j < rad.Main2.initialArgs.size(); j++) {
				LS.add(rad.Main2.initialArgs.get(j));
			}
			LS.add("-Drun.par.file.path="+ITER_XML); //LS.add(ITER_XML);
			String newWrkPath = wrkPath+ "/point(" + i + ")_"+"Lat="+String.format("%5.3f", Lat[i]) + 
											   "_"+"Lon="+String.format("%5.3f", Lon[i])+
											   "_"+"SolElev="+String.format("%5.3f", SolEA[i]);
			
			LS.add("-Dworker.dir.name="+newWrkPath.trim()); //LS.add("-wrk_name="+newWrkPath.trim());
			LS.add("-Dtmp.dir.name="+wrkPath); //LS.add("-tmp_dir="+wrkPath);
			argStr = (String[])LS.toArray(new String[LS.size()]);
			File newWrkDir = new File(newWrkPath);
			newWrkDir.mkdirs();
			BufferedWriter BW = new BufferedWriter(new FileWriter(newWrkPath+"/"+"subTraceInfo.out"));
			BW.write(headerStr+"\r\n");
			BW.write(parStr+"\r\n");
			BW.flush();
			//rad.Main2.main(argStr);
			//copyFile(Conf.getProperty(Conf.run_iter_file_path), newWrkPath+"/"+(new File(Conf.getProperty(Conf.run_iter_file_path))).getName());
			//copyFile(System.getProperty("config.file"), newWrkPath+"/"+(new File(System.getProperty("config.file"))).getName());
			
			copyFile(Conf.getProperty(Conf.run_iter_file_path), newWrkPath+"/" + "POINT.xml");
			//copyFile("mass2.properties", newWrkPath+"/" + "mass2.properties");
			
		}
		} catch (Exception e){
			//e.fillInStackTrace();
			
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	}

}
