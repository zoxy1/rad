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

public class RunnerFREQ extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerFREQ.class);
	@Override
	void runModule(Module modul) throws Exception {
		try {
		String HSTAR_data_path = getParStringByText("HSTAR_data_path");
		String HSTAR_parag_name = getParStringByText("HSTAR_parag_name");
		String HSTAR_mlatmd_name = getParStringByText("HSTAR_mlatmd_name");
		String FREQ_out_name = getParStringByText("FREQ_out_name");
		String HSTAR_parag_re_name = getParStringByText("HSTAR_parag_re_name");
		String PRESS_out_name = getParStringByText("PRESS_out_name");
		//File ParagFile = getFile(HSTAR_data_path+"/"+HSTAR_parag_name);
		//System.out.println(ParagFile.getAbsolutePath());
		//BufferedReader BR = new BufferedReader(new FileReader(ParagFile));
		String RL = "";
		Vector<Double[]> vect;
		/*RL=BR.readLine();
		while (RL.length()==0&&RL!=null){
			RL=BR.readLine();
		}*/
		
		Double[] Alt = readMLATMD_ALT(HSTAR_data_path+"/"+HSTAR_mlatmd_name);
		vect = new Vector<Double[]>();
		vect.add(Alt);
		writeVectorDoubleToTextFileWDescString(vect,wrkPath,"mlatmd_alt.dat","%12.8e","#1 altitude");
				
		//Scanner sc = new Scanner(ParagFile);
		//sc.useDelimiter("\\s+|[:].+");
		
		/*if (RL==null){
			System.out.println("Read error in PARAG. " + "File " + FREQ_out_name + " not created");
			return;
		}*/
		//String[] PARAG = RL.split("\\s+|[:].+");
		//System.out.println(PARAG);
		int i=0;
		Double begWL;
		begWL = getParDoubleByText("PARAG_WL_LO"); //sc.nextDouble();
		Double endWL;
		endWL =  getParDoubleByText("PARAG_WL_UP"); // sc.nextDouble();
		Double step;
		step = getParDoubleByText("PARAG_WL_STEP"); //sc.nextDouble();
		double INTWL = getParDoubleByText("INTWL"); // шаг берется из параметров DATA, а не из PARAG 
		step = INTWL;
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
		/*RL=BR.readLine();
		while (RL.length()==0&&RL!=null){
			RL=BR.readLine();
		}
		if (RL==null){
			System.out.println("Read error in PARAG. " + "File " + PRESS_out_name + " not created");
			return;
		}
		String[] PARAG_num_press = RL.split("\\s+|[:].+");*/
		//sc.nextLine();
		int numPressPoint = getParDoubleByText("PARAG_NUM_PRESS_GRIDS").intValue();//sc.nextInt();	
		System.out.println(numPressPoint);
		File PreF = getFile(PRESS_out_name);
		 if (PreF==null){
			 PreF = new File(PRESS_out_name);
		 }
		 
		BW = new BufferedWriter (new FileWriter(wrkPath+"/"+PreF),2048);
		double PressPoint;
		//sc.nextLine();
		String[] press_pts = (String[])getParObjectArrayByText("PARAG_PRESS", "par");
		if (press_pts.length!=numPressPoint) {
			throw new Exception("Number of pressure points do not match");
		}
		for (int j=1;j<=numPressPoint;j++){
			PressPoint = Double.parseDouble(press_pts[j-1]);
			Ln = String.format("%12.6f",PressPoint);
			BW.write(Ln.concat("\r\n"));
			//BW.write(PressPoint);
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
	
	public static Double[] readMLATMD_ALT (String Path_MLATMD){
		Double ALT[]=null;
		try {
		File MlatmdFile = new File(Path_MLATMD);
		Scanner sc = new Scanner(MlatmdFile);
		String header;
		
		header = sc.nextLine();
		
	     int NM1,NM2,NATM,NL;
	     NM1 = sc.nextInt();
	     NM2 = sc.nextInt();
	     NATM = sc.nextInt();
	     NL = sc.nextInt();
	    	 
	     int NM = NM1+NM2;
	     header = sc.nextLine(); //read header
	     sc.nextLine();
	     double AIRM = sc.nextDouble();
	     sc.nextLine();
	     header = sc.nextLine(); //read header
	     String[] IDM = new String[NM];
	     
	     for (int I=0;I<=NM-1;I++){
	    	 IDM[I] = sc.nextLine();
	    	 int NS=sc.nextInt();
	    	 for (int J=0;J<=NS-1;J++){
	    		 double IDMS = sc.nextDouble();
	    		 double WMOL = sc.nextDouble();
	    		 double RAMS = sc.nextDouble();
	    	 }
	    	 sc.nextLine();
	     }
	     
//	     DO 2 I=1,NM
//	       read IDM(I)
//	       read NS
//	       read (IDMS(I,J),J=1,NS)
//	       read (WMOL(I,J),J=1,NS)
//	   2   read (RAMS(I,J),J=1,NS)
	     
	     header = sc.nextLine(); //read header
	     
	     //read (ALT(I),I=1,NL)	
	     ALT = new Double[NL];
	     for (int I=0;I<=NL-1;I++){
	    	 ALT[I] = sc.nextDouble() ;
	     }
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	     
		return ALT;
	}
}
