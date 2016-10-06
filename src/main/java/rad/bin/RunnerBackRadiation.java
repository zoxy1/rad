package rad.bin;

import platform.Module;
import java.util.*;

import org.apache.log4j.Logger;

public class RunnerBackRadiation extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerBackRadiation.class);
	@Override
	void runModule(Module modul) throws Exception {
		try {
		Double[] dsig = readFileToHashMapOfDoubleArrayWDesc("signal.txt").get("signal");
		Double[] glbFon = new Double[2];
		Double[] glbFon1 = new Double[1];
		Double[] glbFon2 = new Double[1];
		
		br(dsig, dsig, glbFon);
		
		glbFon1[0] = glbFon[0];
		glbFon2[0] = glbFon[1];
		
		Vector<Double[]> vect = new Vector<Double[]>();  
		
		vect.add(glbFon1);
		vect.add(glbFon2);
		
		//background.auto
		String br_mode = getParStringByText("br_mode");
		if (br_mode.startsWith("m")||br_mode.startsWith("M")) {
			setParDoubleByText("br_auto_value",glbFon1[0]);
			glbFon1[0] = getParDoubleByText("br_man_value");
			glbFon2[0] = glbFon1[0];
			writeVectorDoubleToTextFileWDesc(vect, "background.manual.txt", "%12.8e");
		} else {
			setParDoubleByText("br_auto_value",glbFon1[0]);
			writeVectorDoubleToTextFileWDesc(vect, "background.auto.txt", "%12.8e");
		}
			
		} catch (Exception e){
			//e.fillInStackTrace();
			
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
		
	
	}
	
	public void br(Double[] SigOn, Double[] SigOff, Double[] glbFon) {
		//Sub Fon(SigOn() As Single, SigOff() As Single, glbFon() As Single)
		/* Dim K, _
		    I, _
		    del1, _
		    del2, _
		    del3, _
		    f1 As Single, _
		    f2 As Single, _
		    F3 As Single, _
		    o1 As Single, _
		    o2 As Single, _
		    o3 As Single, _
		    s1(9) As Single, _
		    s2(9) As Single, _
		    s3(9) As Single, _
		    Fon As Single, _
		    chan As Boolean */
		    
		//Dim lb As Long
		//Dim ub As Long
		//Dim Row As Long
		
		
		int K, I, del1, del2, del3, lb, ub, Row; 
		Double f1,	f2,	 F3, o1 = 0. , o2 = 0. , o3 = 0., Fon;
		Double[] s1 = new Double[10], s2 = new Double[10], s3 = new Double[10];
		boolean chan;
		
		
		

		    lb = 0; //LBound(SigOff)
		    ub = SigOff.length;  //UBound(SigOff)
		        
		    //With N
		    //For K = 0 To 1
		    
		    for(K=0;K<=1;K++) {

		        del1 = 10;
		        del2 = 10;
		        del3 = 10;

		        switch (K) {
		        	case 0:
		        		 for( I = 1;I<=10;I++) {
		                    	Row = ub - I;
		                        s1[I - 1] = SigOn[Row];
		                        Row = ub - I - 10;
		                        s2[I - 1] = SigOn[Row];
		                        Row = ub - I - 20;
		                        s3[I - 1] = SigOn[Row];
		                    //Next I
		                    }
		        		break;
		        	case 1:
		        		for( I = 1;I<=10;I++) {
	                		Row = ub - I;
	                		s1[I - 1] = SigOff[Row];
	                		Row = ub - I - 10;
	                		s2[I - 1] = SigOff[Row];
	                		Row = ub - I - 20;
	                		s3[I - 1] = SigOff[Row];
	                		//Next I
	                	}
		        		break;
		        }
		        
		            f1 = 0.;
		            f2 = 0.;
		            F3 = 0.;

		            for(I=1;I<=10;I++){
		            	f1 = f1 + s1[I - 1] / del1;
		                f2 = f2 + s2[I - 1] / del2;
		                F3 = F3 + s3[I - 1] / del3;
		            }

		     
		            if (f2!=0) {
		            	o1 = f1 / f2;
			            //End If
		            }
		                
		            if (F3!=0) {
		                o2 = f1 / F3;
		            }
		            
		            if (F3!=0) {
		                o3 = f2 / F3;
		            }
		            
		            Fon = (f1 + f2 + F3) / 3;
		            
		            if (o1 > 1.45) {
		                if (o2 > 1.45 & o3 < 1.45) {
		                   Fon = F3;
		                }
		                if (o2 < 1.45 & o3 < 1.45) {
		                    Fon = (F3 + F3) / 2;
		                }
		            }
		            if (o1 < 1.45 & o2 < 1.45) {
		                Fon = (f1 + f2) / 2;
		            }

		            glbFon[K] = Fon;
		    //Next K
	}
		
	}

}
