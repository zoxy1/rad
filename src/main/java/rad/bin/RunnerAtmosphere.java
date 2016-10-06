package rad.bin;

import java.util.HashMap;
import java.util.Vector;
import platform.Module;
import org.apache.log4j.*;
import org.apache.log4j.Logger;


public class RunnerAtmosphere extends Runner {

	static Logger localLogger = Logger.getLogger(RunnerAtmosphere.class); 
	@Override
	void runModule(Module modul) throws Exception {
		try {

		//Double Hcal = null;
		//int indexHcal;
		
		//Hcal = ProjectRS.Fields("RHcal").Value '30#
		
		//Hcal = getParDoubleByText("calibration_point");
	    
						/*	    SignalRS.MoveFirst
							    SignalRS.find "Altitude > " + Trim(Str(Hcal - 0.00001)), , adSearchForward
							     
							    If Not (SignalRS.BOF Or SignalRS.EOF) Then
							        hr = SignalRS.Fields("Altitude")
							        OFFsignal = SignalRS.Fields("Signal Off")
							        bmrk = SignalRS.BookMark
							        Else
							            bmrk = lbsig + Int((lbsig + ubsig) / 3#)
							            ProjectRS.Fields("RHcal").Value = hsig(bmrk)
							            ProjectRS.Update
							            OFFsignal = OFFSig(bmrk)
							    End If */
		
		HashMap<String, Double[]> hmSig = readFileToHashMapOfDoubleArrayWDesc("signal*.txt");
		Double[] hsig = hmSig.get("altitude");
		//Double[] OFFSig = hmSig.get("signal");
		
		HashMap<String, Double[]> hmMeteo = readFileToHashMapOfDoubleArrayWDesc("meteo_model.txt");
				
		Double[] Psig;// = hmMeteo.get("pressure_atm");
		
		throwHashMapException(Psig = hmMeteo.get("pressure_atm"), "pressure_atm");
		
		Double[] P0sig = hmMeteo.get("pressure_mbar");
		
		throwHashMapException(P0sig = hmMeteo.get("pressure_mbar"), "pressure_mbar");
		
		throwHashMapException(Psig = hmMeteo.get("pressure_atm"), "pressure_atm");
		
		Double[] Tsig = hmMeteo.get("temperature");
		//Double[] bkgr = readFileToHashMapOfDoubleArrayWDesc("background*.txt").get("background_wave1");
		//Double FonOff = bkgr[0];
		Vector<Double[]> vect = new Vector<Double[]>();
		//if (Hcal<=hsig[hsig.length-1]&&Hcal>=hsig[0]){
		//	indexHcal = searchClosestMinimalIndex(hsig,Hcal);
		//} else {
		//	indexHcal = Math.round(hsig.length / 3);
		//}
		//Double OFFsignal = OFFSig[indexHcal];
		//Double offSignalCal = OFFsignal; // offSignalCal = OFFsignal
		//int lbsig = 0, bmrk = indexHcal+1;
		//int ubsig = hsig.length-1;
		Double BpiOnlyMol[] = new Double[Psig.length];
		Double AlphaOnlyMol[] = new Double[Psig.length];
		
		countBpiAlphaMolecular(P0sig,Tsig,BpiOnlyMol,AlphaOnlyMol);
		
		Double TAUOnlyMol[] = countTau(hsig ,AlphaOnlyMol); //new Double[Psig.length];
		
		Double TransmittOnlyMolSig[] = countTransmittance(TAUOnlyMol);
		
		
							    
		//Double realHcal = hsig[indexHcal]; //realHcal = hsig(lbsig + bmrk - 1);
		
		//Double dH = realHcal - hsig[lbsig + bmrk - 2];
		//Double raznsigfon = OFFsignal - FonOff;
		
		//if (raznsigfon == 0) { //Then
		//	raznsigfon = 0.1;
							   //End If
		//}
							   //C = ((raznsigfon) * realHcal ^ 2) / (dH * TransmittOnlyMolSig(lbsig + bmrk - 1) * BpiOnlyMol(lbsig + bmrk - 1))
		//Double C = ((raznsigfon) * Math.pow(realHcal, 2)) / (dH * TransmittOnlyMolSig[indexHcal] * BpiOnlyMol[indexHcal]);
		
							  /* Dim th12 As Single, transsig2 As Single, transsig1 As Single, transrh1 As Single, transrh2 As Single, transBpiOMH1 As Single, _
							transBpiOMH2 As Single, realTransh2 As Single, realTransh1 As Single */
		//Double th12, transsig2, transsig1, transrh1, transrh2, transBpiOMH1, transBpiOMH2, realTransh2, realTransh1; 
		//Double Rsig[] = new Double[hsig.length];
		//Double RsigmaMinus[] = new Double[hsig.length];
		//Double RsigmaPlus[] = new Double[hsig.length];
		
							   
							    //lPrWave.MoveFirst
							//    dH = hsig(lbsig)
		/*for (int J=lbsig;J<=ubsig;J++){
			if(J>lbsig){
				dH = hsig[J] - hsig[J - 1];
			}
			Rsig[J]=((OFFSig[J] - FonOff) * Math.pow(hsig[J],2)) / (dH * TransmittOnlyMolSig[J] * BpiOnlyMol[J] * C);
			Double sigmar = 0.;
			if ((OFFSig[J] - FonOff) == 0) {//Then
				sigmar = 0.;
			} else { //Else
                //sigmar = Sqr(((OFFSig(J)) / ((OFFSig(J) - FonOff) * (OFFSig(J) - FonOff))) + ((offSignalCal) / ((offSignalCal - FonOff) * (offSignalCal - FonOff))) + ((3 * (0.01)) * (3 * (0.01))));
				sigmar = Math.sqrt( (((OFFSig[J]) / ((OFFSig[J] - FonOff) * (OFFSig[J] - FonOff))) + ((offSignalCal) / ((offSignalCal - FonOff) * (offSignalCal - FonOff))) + ((3 * (0.01)) * (3 * (0.01)))));
			}
			RsigmaMinus[J] = Rsig[J] - sigmar;
			RsigmaPlus [J] = Rsig[J] + sigmar;
           
		}	*/						
									//For J = lbsig To ubsig
		
							       /* If J > lbsig Then
							            dH = hsig(J) - hsig(J - 1)
							        End If
							        lPrWave.Fields("Bpi").Value = BPIsig(J) 'Format(BPIsig(J), "0.000000")
							        lPrWave.Fields("Tau").Value = TAUSig(J) + TauGASsig(J) 'Format(TAUSig(J) + TauGASsig(J), "0.000000") '+ TauGAS(j)
							        lPrWave.Fields("Alpha").Value = ALPHAsig(J) + AlphaGASsig(J)
							        lPrWave.Fields("Tau AM").Value = TAUSig(J) 'Format(TAUSig(J), "0.000000") '+ TauGAS(j)
							        lPrWave.Fields("Tau Gas").Value = TauGASsig(J) 'Format(TauGASsig(J), "0.000000")
							        lPrWave.Fields("Alpha Gas").Value = AlphaGASsig(J) 'Format(AlphaGASsig(J), "0.000000")
							        lPrWave.Fields("Alpha AM").Value = ALPHAsig(J) 'Format(ALPHAsig(J), "0.000000")
							        lPrWave.Fields("Transmittance").Value = TransmittSig(J) 'Format(TransmittSig(J), "0.000000")
							        lPrWave.Fields("T").Value = Tsig(J)
							        lPrWave.Fields("Alpha Mol").Value = AlphaOnlyMol(J)
							        lPrWave.Fields("Bpi Mol").Value = BpiOnlyMol(J)
							        lPrWave.Fields("Tau Mol").Value = TAUOnlyMol(J)
							        lPrWave.Fields("Transmittance Mol").Value = TransmittOnlyMolSig(J)
							        Rsig(J) = ((OFFSig(J) - FonOff) * hsig(J) ^ 2) / (dH * TransmittOnlyMolSig(J) * BpiOnlyMol(J) * C)  'Rsig(J)
							        If (OFFSig(J) - FonOff) = 0 Then
							            sigmar = 0
							            Else
							                sigmar = Sqr(((OFFSig(J)) / ((OFFSig(J) - FonOff) * (OFFSig(J) - FonOff))) + ((offSignalCal) / ((offSignalCal - FonOff) * (offSignalCal - FonOff))) + ((3 * (0.01)) * (3 * (0.01))))
							        End If
							        lPrWave.Fields("R").Value = Rsig(J)
							        lPrWave.Fields("R+sigma").Value = Rsig(J) + (sigmar * Rsig(J))
							        lPrWave.Fields("R-sigma").Value = Rsig(J) - (sigmar * Rsig(J))
							        lPrWave.MoveNext
							    Next J */
		
		vect.add(hsig);
		//vect.add(TAUOnlyMol); 
		//vect.add(TransmittOnlyMolSig);
		vect.add(BpiOnlyMol);
		vect.add(AlphaOnlyMol);
		vect.add(TAUOnlyMol);
		vect.add(TransmittOnlyMolSig);
		
		
		//Double extra[] ={0.012};
		//vect.add(RsigmaMinus);
		//vect.add(RsigmaPlus);
		//writeVectorDoubleToTextFileWDesc(vect,"scat_ratio.txt","%7.4e");
		
		writeVectorDoubleToTextFileWDesc(vect,"atmosphere_model.txt","%7.4e");
		
		} catch (Exception e){
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			//localLogger.fatal(e.getMessage());
			throw e;
		}
		
	}
	
	static public int searchClosestMinimalIndex (Double[] sArr, Double sVal) {
		int index = sArr.length-1;
		for (int i=0;i<sArr.length;i++) {
			if (sArr[i]>=(sVal-1.E-8)) {
				index = i;
				i = sArr.length+1;
			}
		}
		return index;
	}
	
	static public Double[] countTau (Double H[], Double F[]) {
		//Sub TAUCOUNT(N As Long, H, F, rez As Variant)
		//Dim n1 As Integer, hag As Single
		//int n1;
		Double hag;
		Double rez[] = new Double[H.length];
		//n1 = N - 1;
		Double S = 0.;
		//'rez(1) = 0
		//Dim hlb As Long, ulb As Long
		int hlb, ulb;
		hlb = 0; //LBound(H)
		ulb = H.length-1; // UBound(H)
		rez[hlb] = 0.;
		for(int I = hlb;I <= ulb-1;I++) //For I = hlb To ulb - 1
		{
			hag = H[I + 1] - H[I]; //hag = H(I + 1) - H(I)
			S = S + hag * (F[I + 1] + F[I]) / 2; //S = S + hag * (F(I + 1) + F(I)) / 2
		    rez[I + 1] = S;
		}
		    
		//Next
		return rez; //End Sub
	}
	
	static public void countBpiAlphaMolecular(Double[] P, Double[] T, Double[] BpiOnlyMol, Double[] AlphaOnlyMol) {
		for(int I=0;I<P.length;I++) {
			BpiOnlyMol[I] = 0.000462 * (P[I] / T[I]); //'alfamr * Cst * const1
			AlphaOnlyMol[I] = BpiOnlyMol[I] * 4 * Math.PI / 1.5;
		}
        return;
	}
	
	static public Double[] countTransmittance(Double[] Tau) {
		Double[] TransmittN = new Double[Tau.length];
		for(int I=0;I<Tau.length;I++) {
		    //For I = hlb To ulb
	        TransmittN[I] = Math.exp(-2.*(Tau[I]));
	    //Next
		}
        return TransmittN;
	}

}
