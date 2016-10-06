package rad.bin;

import platform.Module;
//import sun.misc.Signal;

//import java.awt.geom.Arc2D.Double;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

public class RunnerSmooth extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerSmooth.class);
	@Override
	void runModule(Module modul) throws Exception {
		try {
		HashMap<String, Double[]> HMsignal = readFileToHashMapOfDoubleArrayWDesc("signal*.txt");
		Double[] sig = HMsignal.get("signal");
		Double[] alt = HMsignal.get("altitude");
		Double[] sig_smooth = new Double[sig.length];
		String isSmooth = getParStringByText("is_smooth");
		Double repeat = getParDoubleByText("repeat_number");
		long rn = Math.round(repeat);
		int IER=0;
		sig_smooth = sig;
		if (isSmooth.equalsIgnoreCase("none")) {
			sig_smooth = sig;
		} else if (isSmooth.equalsIgnoreCase("se13")) {
			for(int i=1; i<=rn; i++) {
				SE13(sig, sig_smooth, sig.length, IER);
				sig = sig_smooth;
			};
			//SE13fort(wrkPath, "signal.correct.txt", 2, sig_smooth, sig.length, IER, 5);
		} else if (isSmooth.equalsIgnoreCase("se15")) {
			//SE15(sig, sig_smooth, sig.length, IER);
			for(int i=1; i<=rn; i++) {
				SE15(sig, sig_smooth, sig.length, IER);
				sig = sig_smooth;
			};
		} else {
			sig_smooth = sig;
			System.out.println("RunnerSmooth: no such smoothing method - " + isSmooth);
			System.out.println("No smoothing at all");
		}
			
		
		Vector<Double[]> vect = new Vector<Double[]>();
		vect.add(alt);
		vect.add(sig_smooth);
		//vect.add(sig);
		//String outf = getOutFile("signal*.txt");
		//writeMapDoubleToTextFile()
		writeVectorDoubleToTextFileWDesc(vect, wrkPath, "signal*.txt", "%12.8e");
		} catch (Exception e){
			//e.fillInStackTrace();
			
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	}
	
	public void SE13fort (String wrkDir, String fname, int colnum, Double[] Z, int NDIM, int IER, int REPEAT) throws Exception {
		
		ArrayList<String> cmAL = new ArrayList<String>(); 
    	
    	cmAL.add( "rad/utils/se4java.exe");
    	cmAL.add( wrkDir+"/"+fname);
    	cmAL.add( ""+colnum);
    	cmAL.add( ""+NDIM);
    	cmAL.add( wrkDir+"/"+"utils.se13.txt");
    	cmAL.add( "(E24.10)");
    	cmAL.add( ""+REPEAT);
    	//Object[] cmd = cmAL.toArray();
    	String[] cmd = (String[])cmAL.toArray(new String[cmAL.size()]);
    	ProcessBuilder PB = new ProcessBuilder(cmd);
    	Process PR=null;
    	try {
    		PR = PB.start();
    	} catch (IOException e){
    		e.printStackTrace();
    	}
    	
    	boolean isRunning = true;
    	BufferedInputStream InpStream = new BufferedInputStream(PR .getErrorStream());
		BufferedInputStream ConStream = new BufferedInputStream(PR.getInputStream());
        FileOutputStream OutFile = new FileOutputStream((new File(this.mdWrkPath + "/" + "utils.se13")).getAbsolutePath()+"_Err.log");
		FileOutputStream OutConFile = new FileOutputStream((new File(this.mdWrkPath + "/" + "utils.se13")).getAbsolutePath()+"_Con.log");
		byte[] bt = new byte[1024];
		int c;
		int exitCode = -1;
		
    	while (isRunning) {
                    try {
                        Thread.sleep(200);
                        //cout.read(processInBuff);
                        //System.out.println(new String(processInBuff));
                        exitCode = PR.exitValue();
                        isRunning = false;
                    } catch (IllegalThreadStateException ite) {
                        System.out.println("Process still working... stand by...");
                        while(InpStream.available()!=0&&(c=InpStream.read(bt))>0){
                    		OutFile.write(bt,0,c);
                		}
    			
                		while(ConStream.available()!=0&&(c=ConStream.read(bt))>0){
                			//OutFile.write(bt,0,c);
                			//System.out.println(new String(bt));
                			OutConFile.write(bt,0,c);
                		}
                    }
                }
    	
    	
    	System.out.println("Module \"" + "rad/utils/se4java.exe" + "\" exited with code : " + exitCode);
        System.out.println();
    	   	
    	
    	Vector<Double[]> vect = readFileToVectorOfDoubleArray("utils.se13.txt", 1);
    	Double[] Z1 = vect.get(0);
    	for(int i=0;i<=Z1.length-1;i++){
    		Z[i]=Z1[i];
    	}
		return;
	}
	
	public void SE13 (Double[] Y, Double[] Z, int NDIM, int IER) {
		//
		//     ..................................................................
		//
		//        SUBROUTINE SE13
		//
		//        PURPOSE
		//           TO COMPUTE A VECTOR OF SMOOTHED FUNCTION VALUES GIVEN A
		//           VECTOR OF FUNCTION VALUES WHOSE ENTRIES CORRESPOND TO
		//           EQUIDISTANTLY SPACED ARGUMENT VALUES.
		//
		//        USAGE
		//           CALL SE13(Y,Z,NDIM,IER)
		//
		//        DESCRIPTION OF PARAMETERS
		//           Y     -  GIVEN VECTOR OF FUNCTION VALUES (DIMENSION NDIM)
		//           Z     -  RESULTING VECTOR OF SMOOTHED FUNCTION VALUES
		//                    (DIMENSION NDIM)
		//           NDIM  -  DIMENSION OF VECTORS Y AND Z
		//           IER   -  RESULTING ERROR PARAMETER
		//                    IER = -1  - NDIM IS LESS THAN 3
		//                    IER =  0  - NO ERROR
		//
		//        REMARKS
		//           (1)  IF IER=-1 THERE HAS BEEN NO COMPUTATION.
		//           (2)   Z CAN HAVE THE SAME STORAGE ALLOCATION AS Y.  IF Y
		//                 IS DISTINCT FROM Z, THEN IT IS NOT DESTROYED.
		//
		//        SUBROUTINES AND SUBPROGRAMS REQUIRED
		//           NONE
		//
		//        METHOD
		//           IF X IS THE (SUPPRESSED) VECTOR OF ARGUMENT VALUES, THEN
		//           EXCEPT AT THE ENDPOINTS X(1) AND X(NDIM), EACH SMOOTHED
		//           VALUE Z(I) IS OBTAINED BY EVALUATING AT X(I) THE LEAST-
		//           SQUARES POLYNOMIAL OF DEGREE 1 RELEVANT TO THE 3 SUCCESSIVE
		//           POINTS (X(I+K),Y(I+K)) K = -1,0,1.  (SEE HILDEBRAND, F.B.,
		//           INTRODUCTION TO NUMERICAL ANALYSIS, MC GRAW-HILL, NEW YORK/
		//           TORONTO/LONDON, 1956, PP. 295-302.)
		//
		//     ..................................................................
		//
		      //SUBROUTINE SE13(Y,Z,NDIM,IER)
			//!DEC$ ATTRIBUTES DLLEXPORT::SE13
			//implicit none
			int I;
			Double  A, B, C;
		//
		//
		      //DIMENSION Y(1),Z(1)
		//
		//        TEST OF DIMENSION
		//      IF(NDIM-3)3,1,1
		//
		//        PREPARE LOOP
		    if (Y.length>=3) {
		    	B=.1666667*(5.*Y[0]+Y[1]+Y[1]-Y[2]);
		    	C=.1666667*(5.*Y[NDIM-1]+Y[NDIM-2]+Y[NDIM-2]-Y[NDIM-3]);
		//
		//        START LOOP
		      for( I=2; I<=NDIM-1; I++) {
		    	  A=B;
			      B=.3333333*(Y[I-2]+Y[I-1]+Y[I]);
			      Z[I-2]=A;  
		      }
		      
		//        END OF LOOP
		//
		//        UPDATE LAST TWO COMPONENTS
		      Z[NDIM-2]=B;
		      Z[NDIM-1]=C;
		      IER=0;
		    } else {
		//        ERROR EXIT IN CASE NDIM IS LESS THAN 3
			   IER=-1;
		    }
			    
		//
      //RETURN
		      //END
		      return;
	}
	
	public void SE15 (Double[] Y,Double[] Z, int NNDIM, int IER) {
		//
		//     ..................................................................
		//
		//        SUBROUTINE SE15
		//
		//        PURPOSE
		//           TO COMPUTE A VECTOR OF SMOOTHED FUNCTION VALUES GIVEN A
		//           VECTOR OF FUNCTION VALUES WHOSE ENTRIES CORRESPOND TO
		//           EQUIDISTANTLY SPACED ARGUMENT VALUES.
		//
		//        USAGE
		//           CALL SE15(Y,Z,NDIM,IER)
		//
		//        DESCRIPTION OF PARAMETERS
		//           Y     -  GIVEN VECTOR OF FUNCTION VALUES (DIMENSION NDIM)
		//           Z     -  RESULTING VECTOR OF SMOOTHED FUNCTION VALUES
		//                    (DIMENSION NDIM)
		//           NDIM  -  DIMENSION OF VECTORS Y AND Z
		//           IER   -  RESULTING ERROR PARAMETER
		//                    IER = -1  - NDIM IS LESS THAN 5
		//                    IER =  0  - NO ERROR
		//
		//        REMARKS
		//           (1)  IF IER=-1 THERE HAS BEEN NO COMPUTATION.
		//           (2)   Z CAN HAVE THE SAME STORAGE ALLOCATION AS Y.  IF Y IS
		//                 DISTINCT FROM Z, THEN IT IS NOT DESTROYED.
		//
		//        SUBROUTINE AND FUNCTION SUBPROGRAMS REQUIRED
		//           NONE
		//
		//        METHOD
		//           IF X IS THE (SUPPRESSED) VECTOR OF ARGUMENT VALUES, THEN
		//           EXCEPT AT THE POINTS X(1),X(2),X(NDIM-1) AND X(NDIM), EACH
		//           SMOOTHED VALUE Z(I) IS OBTAINED BY EVALUATING AT X(I) THE
		//           LEAST-SQUARES POLYNOMIAL OF DEGREE 1 RELEVANT TO THE 5
		//           SUCCESSIVE POINTS (X(I+K),Y(I+K)) K = -2,-1,...,2.  (SEE
		//           HILDEBRAND, F.B., INTRODUCTION TO NUMERICAL ANALYSIS,
		//           MC GRAW-HILL, NEW YORK/TORONTO/LONDON, 1956, PP. 295-302.)
		//
		//     ..................................................................
		//
		      //SUBROUTINE SE15(Y,Z,NDIM,IER)
			//!DEC$ ATTRIBUTES DLLEXPORT::SE15
			//implicit none
			int I, NDIM = NNDIM-1;
			Double  A, B, C;
		//
		//
		      //DIMENSION Y(1),Z(1)
		//
		//        TEST OF DIMENSION
		      //IF(NDIM-5)3,1,1
			if (Y.length>=5) {
			      A=Y[0]+Y[0];
			      C=Y[1]+Y[1];
			      B=.2*(A+Y[0]+C+Y[2]-Y[4]);
			      C=.1*(A+A+C+Y[1]+Y[2]+Y[2]+Y[3]);
			//
			//        START LOOP
			      for (I=4;I<=NDIM;I++) {
				     A=B;
				     B=C;
				     C=.2*(Y[I-4]+Y[I-3]+Y[I-2]+Y[I-1]+Y[I]);
				     Z[I-4]=A;			    	  
			      }

			//        END OF LOOP
			//
			//        UPDATE LAST FOUR COMPONENTS
			      A=Y[NDIM]+Y[NDIM];
			     A=0.1*(A+A+Y[NDIM-1]+Y[NDIM-1]+Y[NDIM-1]+Y[NDIM-2]+Y[NDIM-2]+Y[NDIM-3]);
			      Z[NDIM-3]=B;
			      Z[NDIM-2]=C;
			      Z[NDIM-1]=A;
			      Z[NDIM]=A+A-C;
			      IER=0;
			} else {
				IER=-1;
			}
		//
		//        PREPARE LOOP

		      //RETURN
		//
		//        ERROR EXIT IN CASE NDIM IS LESS THAN 5
		    //3 IER=-1
		      return;
	}
}
