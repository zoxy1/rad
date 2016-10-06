package rad.bin;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import platform.Module;

public class RunnerTemperature extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerTemperature.class);
	@Override
	void runModule(Module modul) throws Exception {
		try{
		
		//Dim HTmax As Double, HTindex As Integer, realHTmax As Double, sigHTmax As Double
	    //Dim Tr() As Double
		
		HashMap<String, Double[]> hmSig = readFileToHashMapOfDoubleArrayWDesc("signal*.txt");
		Double[] hsig = hmSig.get("altitude");
		Double altlow = hsig[0];
		Double althig = hsig[hsig.length-1];
		Double[] OFFSig = hmSig.get("signal");
		HashMap<String, Double[]> hmMeteo = readFileToHashMapOfDoubleArrayWDesc("meteo_model.txt");
		Double[] Tsig = hmMeteo.get("temperature");
		
	    Double HTmax, realHTmax, sigHTmax;
	    int HTindex;
		
		
	    //HTmax = ProjectRS("HTmax")
	    HTmax = getParDoubleByText("height_t_max");
	    
	    /*Select Case True
	        Case HTmax < altlow
	            HTmax = altlow
	        Case HTmax > althig
	            HTmax = althig
	    End Select*/
	    
	    if (HTmax < altlow) {
	    	HTmax = altlow;
	    	HTindex = 0;
	    } else if (HTmax > althig) {
	    	HTmax = althig;
	    	HTindex = hsig.length-2;
	    } else {
	    	HTindex = RunnerRatScatter.searchClosestMinimalIndex(hsig, HTmax);
	    	if (HTindex==hsig.length-1) {
	    		HTindex--;
	    	}
	    }	
	    
	    realHTmax = hsig[HTindex];
	    sigHTmax = OFFSig[HTindex];
	    
	    /*SignalRS.MoveFirst
	    SignalRS.find "Altitude >= " + Trim(Str(HTmax - 0.00001)), , adSearchForward
	    If Not (SignalRS.BOF Or SignalRS.EOF) Then
	        realHTmax = SignalRS.Fields("Altitude")
	        sigHTmax = SignalRS.Fields("Signal Off")
	        HTindex = SignalRS.BookMark - 2
	        'bmh2 = bmrk
	    End If */
	    
	    /*ReDim Tr(lbsig + 1 To HTindex)
	    Dim mult1 As Double, add1 As Double, intgft As Double
	    Dim TempErr() As Double, A As Double, B As Double, NplusF As Double, NH As Double
	    ReDim TempErr(lbsig + 1 To HTindex)
	    Dim Z As Integer*/
	    
	    Double Tr[] = new Double[hsig.length]; //Double[HTindex+1];
	    Double mult1, add1, intgft;
	    Double TempErr[], A, B, NplusF, NH;
	    TempErr = new Double[hsig.length];
	    int Z;
	    int lbsig = 0;
	    
	    HashMap<String, Double[]> AtmModel = readFileToHashMapOfDoubleArrayWDesc("atmosphere_model.txt");
	    Double BpiOnlyMol[] = AtmModel.get("bpi_molecular");		
		Double TransmittOnlyMolSig[] = AtmModel.get("transmittance_molecular"); 
	    
		Double[] bkgr = readFileToHashMapOfDoubleArrayWDesc("background*.txt").get("background_wave1");
		Double FonOff = bkgr[0];
		Double[] TsigmaMinus = new Double[hsig.length];
		Double[] TsigmaPlus = new Double[hsig.length];
	    
	    for(int J = lbsig + 1;J<=HTindex;J++){
	        mult1 = (Math.pow(TransmittOnlyMolSig[J], 2.)) / ((OFFSig[J] - FonOff) * (Math.pow(hsig[J], 2.)));
	        add1 = ((OFFSig[HTindex] - FonOff) * Math.pow(hsig[HTindex],2)) / Math.pow(TransmittOnlyMolSig[HTindex], 2.) * Tsig[HTindex];
	        intgft = IntgrateForTemper(hsig, OFFSig, TransmittOnlyMolSig, 62., HTindex, J, FonOff);
	        Tr[J] = mult1 * (add1 + intgft);
	        A = OFFSig[J] / (Math.pow((OFFSig[J] - FonOff), 2.)) + OFFSig[HTindex] / (Math.pow((OFFSig[HTindex] - FonOff), 2.)) + (0.01);
	        NplusF = 0.;
	        NH = 0.;
	        for( Z = J + 1;Z<=(HTindex - 1);Z++) {
	            NplusF = NplusF + Math.pow((Math.sqrt(Math.pow(OFFSig[Z], 3.)) * (Math.pow(hsig[Z],2.)) / (OFFSig[Z] - FonOff)),2.);
	            NH = NH + (OFFSig[Z] - FonOff) * Math.pow(hsig[Z],2.);
	        }
	        //Next Z
	        
	        NH = NH * NH;
	        if (NH != 0) {
	            B = OFFSig[J] / (Math.pow((OFFSig[J] - FonOff) , 2.)) + (NplusF / NH);
	            }
	            else {
	                B = OFFSig[J] / Math.pow((OFFSig[J] - FonOff), 2.);
	        }
	        
	        TempErr[J] = Math.sqrt((mult1 * mult1) * (((add1 * add1) * A) + ((intgft * intgft) * B)));
	        TsigmaMinus[J] = Tr[J] - TempErr[J];
	        TsigmaPlus[J] = Tr[J] + TempErr[J];
	    }
	    
	    Vector<Double[]> vect = new Vector<Double[]>();
	 
	    vect.add(hsig);
	    vect.add(Tr);
	    vect.add(TsigmaMinus);
	    vect.add(TsigmaPlus);
	    writeVectorDoubleToTextFileWDesc(vect, "temp_inverse.txt", "%12.8e");
		} catch (Exception e){
			//e.fillInStackTrace();
			
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	    //lPrWave.MoveFirst
	    //lPrWave.MoveNext
	    
	    /*For J = lbsig + 1 To HTindex
	        mult1 = (TransmittOnlyMolSig(J) ^ 2) / ((OFFSig(J) - FonOff) * (hsig(J) ^ 2))
	        add1 = ((OFFSig(HTindex) - FonOff) * hsig(HTindex) ^ 2) / TransmittOnlyMolSig(HTindex) ^ 2 * Tsig(HTindex)
	

	        intgft = IntgrateForTemper(hsig, OFFSig, TransmittOnlyMolSig, 62, HTindex, J, FonOff)
	        Tr(J) = mult1 * (add1 + intgft)
	        A = OFFSig(J) / ((OFFSig(J) - FonOff) ^ 2) + OFFSig(HTindex) / ((OFFSig(HTindex) - FonOff) ^ 2) + (0.01)
	        NplusF = 0
	        NH = 0
	        For Z = J + 1 To HTindex - 1
	            NplusF = NplusF + (Sqr(OFFSig(Z) ^ 3) * (hsig(Z) ^ 2) / (OFFSig(Z) - FonOff)) ^ 2
	            NH = NH + (OFFSig(Z) - FonOff) * (hsig(Z) ^ 2)
	        Next Z
	        
	        NH = NH ^ 2
	        If NH <> 0 Then
	            B = OFFSig(J) / ((OFFSig(J) - FonOff) ^ 2) + (NplusF / NH)
	            Else
	                B = OFFSig(J) / ((OFFSig(J) - FonOff) ^ 2)
	        End If
	        
	        TempErr(J) = Sqr((mult1 ^ 2) * (((add1 ^ 2) * A) + ((intgft ^ 2) * B)))
	        lPrWave("Tr") = Tr(J)
	        lPrWave("Tr+sigma") = Tr(J) + TempErr(J)
	        lPrWave("Tr-sigma") = Tr(J) - TempErr(J)
	        lPrWave.MoveNext
	    Next*/

	}
 public static Double IntgrateForTemper (Double[] Alt ,Double[] Sig,Double[] TransMol, Double Fi, int HTindex, int curHindex, Double FonOff) {
	    //int lbsig = 0; //LBound(Alt)
	 
	    if( curHindex > HTindex ) {
	       //IntgrateForTemper = -1.;
	       //Exit Function
	    	return -1.;
	    }
	    Double Sum = new Double(0), G = 9.8;
	    for(int J = curHindex;J<=HTindex;J++) {
	        Sum = Sum + (((Sig[J] - FonOff) / (TransMol[J] * TransMol[J]) * (Alt[J] * Alt[J])) * G) * (Alt[J + 1] - Alt[J]);
	    }
	    Sum = 3.484 * Sum;
	    
	 return Sum;
 }
}
