package rad.bin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

import org.apache.log4j.Logger;

import platform.Module;
import org.apache.log4j.Logger;

public class RunnerFreqTrans extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerFREQ.class);
	@Override
	void runModule(Module modul) throws Exception {
		try {
		
		
		
//		int i=0;
		Double begWL;
		begWL = getParDoubleByText("trans_wl_low"); //sc.nextDouble();
		Double endWL;
		endWL =  getParDoubleByText("trans_wl_up"); // sc.nextDouble();
		Double step;
		step = getParDoubleByText("trans_wl_step"); //sc.nextDouble();
		String FREQ_out_name = getParStringByText("trans_freq_file_name");
//		double INTWL = getParDoubleByText("INTWL"); // ��� ������� �� ���������� DATA, � �� �� PARAG 
//		step = INTWL;
		/*try {
			while (PARAG[i].length()==0){
				i++;
			}
			begWL = new Double(PARAG[i]);
			endWL = new Double(PARAG[i+1]);
			step = new Double(PARAG[i+2]);
		} catch (Exception e) {
			System.out.println("Parse error in PARAG. " + "File " + FREQ_out_name + " not created");
			throw e;
		}*/
		
		Long lines = new Long( new Double(Math.floor((endWL - begWL)/step)).intValue() + 1);
		 File FreF = getFile(FREQ_out_name);
		 if (FreF==null){
			 FreF = new File(FREQ_out_name);
		 }
				 
		BufferedWriter BW = new BufferedWriter (new FileWriter(wrkPath+"/"+FreF),2048);
		//BW.is
		BW.write(lines+"\r\n");
		//BW.write(String.format("%12.6e",begWL)+"\r\n");
		String Ln;
		for(long k=0;k<=lines-1;k++){
			Ln = String.format("%12.6f",begWL+(k*step));
			BW.write(Ln.concat("\r\n"));
		}
		BW.flush();
		BW.close();
		} catch (Exception e){
			//e.fillInStackTrace();
			
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	} 
	
}
