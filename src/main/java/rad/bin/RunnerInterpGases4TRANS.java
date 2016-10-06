package rad.bin;

import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import platform.Module;

/**
 * �������� ����: ������ ��������������� � ������������ ����� ��
 * ������� AFGL, CIRA, SEEBOR � ��. <br>
 * � ������������ �� ����� ����� �� ����� MLATMD ��������� HSTAR 
 * 
 *  
 */

public class RunnerInterpGases4TRANS extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerInterpGases4HSTAR.class);
	@Override
	void runModule(Module module) throws Exception {
		try {
		//���������� ����� ����� �� MLATMD_alt.dat
		//HashMap<String, Double[]> mapMLATMD = readFileToHashMapOfDoubleArray("mlatmd_alt.dat");
		//Double[] altMLATMD=mapMLATMD.get("altitude"); //press
		//throwHashMapException(altMLATMD, "altitude");
		
		//Double[] pressMLATMD=mapMLATMD.get("pressure");
		
//		���������� ����� ����� � �������� �� afgl.dat
		Vector<Double[]> ResultVect = new Vector<Double[]>();
		HashMap<String, Double[]> mapAFGL = readFileToHashMapOfDoubleArray("afgl.dat");
		
		Double[] altAFGL = mapAFGL.get("altitude");
		throwHashMapException(altAFGL, "altitude");
		
//		Double[] pressAFGL = mapAFGL.get("pressure");
		String DescString = "";

//		���������� ����� �����, �������� � H2O �� afgl.dat
		//HashMap<String, Double[]> mapSEEBOR = readFileToHashMapOfDoubleArray("seebor.dat");
		//Double[] altSEEBOR = mapSEEBOR.get("altitude");
		//throwHashMapException(altSEEBOR, "altitude");
		
		//Double[] pressSEEBOR = mapSEEBOR.get("pressure");
		//Double[] H2O_SEEBOR = mapSEEBOR.get("1");
		//throwHashMapException(H2O_SEEBOR, "1");
		
		//HashMap<String, Double[]> mapHSTAR = readFileToHashMapOfDoubleArray("press_hstar.dat");
		//Double[] pressHSTAR = mapHSTAR.get("pressure");

		//���� ���������� �������� readCO2fromNetCDF, �� ������ ������ �� ���������������� ����� nc*.dat
		
		Boolean readCO2fromNetCDF = getParBooleanByText("readCO2fromNetCDF");
		HashMap<String, Double[]> mapCO2;
		Double[] netCdfCO2;
		Double[] netCdfPress;
//		if (readCO2fromNetCDF) {
//			mapCO2 = readFileToHashMapOfDoubleArrayWDesc("nc*.dat");
//			netCdfCO2 = mapCO2.get("co2");
//			netCdfPress = mapCO2.get("pressure");
//			//netCdfCO2 = mapCO2.get("co2");
//		}
		
//		���������� ����� �����, �������� � ���������� �� cira.dat
		HashMap<String, Double[]> mapCIRA = readFileToHashMapOfDoubleArray("apt*.dat");
		
		Double[] altCIRA = mapCIRA.get("altitude");
		throwHashMapException(altCIRA, "altitude");
		
		Double[] pressCIRA = mapCIRA.get("pressure");
		throwHashMapException(pressCIRA, "pressure");
		
		Double[] tempCIRA = mapCIRA.get("temperature");
		throwHashMapException(tempCIRA, "temperature");
		
		//Double[] tempHSTAR_CIRA = new Double[altMLATMD.length];
		//Double[] pressHSTAR_CIRA = new Double[altMLATMD.length];
		
		
		int IER=0;
		//RunnerUtils.lineInterp(pressCIRA.length, pressCIRA, tempCIRA, pressHSTAR.length, pressHSTAR, tempHSTAR_CIRA, IER);
		//������������ ����������� � �������� CIRA �� ����� ����� MLATMD
		//RunnerUtils.lineInterp(altCIRA.length, altCIRA, tempCIRA, altMLATMD.length, altMLATMD, tempHSTAR_CIRA, IER);
		//RunnerUtils.lineInterp(altCIRA.length, altCIRA, pressCIRA, altMLATMD.length, altMLATMD, pressHSTAR_CIRA, IER);
		
//		���� ���������� �������� readCO2fromNetCDF, �� ������ ������ �� ���������������� ����� nc*.dat
		Double[] co2_TRANS_CIRA=null;
		if (readCO2fromNetCDF) {
			mapCO2 = readFileToHashMapOfDoubleArrayWDesc("nc*.dat");
			
			
			
			throwHashMapException(netCdfCO2 = mapCO2.get("co2"), "temperature");
			
			
			throwHashMapException(netCdfPress = mapCO2.get("pressure"), "pressure");
			
//			 ������������ ������������ CO2 �� ����� ����� NCEP ��� CIRA (����� �����)
			co2_TRANS_CIRA = new Double[pressCIRA.length];
			RunnerUtils.lineInterp(netCdfPress.length, netCdfPress, netCdfCO2, pressCIRA.length, pressCIRA, co2_TRANS_CIRA, IER);
		}
		
		
		//���������� ������� �����
		String[] gas_num = (String[])getParObjectArrayByText("gas_num", "par");

		//Double[] H2O_HSTAR = new Double[altMLATMD.length];	// ���������� ������� ��� ������������ H2O, 
															//����������������� �� ����� MLATMD 
//		���������� ������������ H2O
		//RunnerUtils.lineInterp(altSEEBOR.length, altSEEBOR, H2O_SEEBOR, altMLATMD.length, altMLATMD, H2O_HSTAR, IER);
		
		
		// ���������� ���������-����������� ��� GASES
		ResultVect.add(altCIRA);
		DescString = DescString + "# altitude";
		ResultVect.add(pressCIRA);
		DescString = DescString + "# pressure";
		ResultVect.add(tempCIRA);
		DescString = DescString + " # temperature";
//		ResultVect.add(H2O_HSTAR);
//		DescString = DescString + " # 1";
		
		//����� ���� ��� ���������� ����� �� afgl.dat, ������������ �� ����� ����� mlatmd
		//� ������������ ������������ ����� ������ ����������� ��� ����� AFGL, �������� � GASES
		//���� ���������� �� �������, ��������� � ���. XML 
		//���� ����� � XML ����, �� ��� � ���� ������� �������� �� AFGL � GASES
		int gascnt = 0; //1; ��� 1 ����� ����������� ������ SEEBOR
		String gas_num_str = ""; // " 1"; ��� 1 ����� ����������� ������ SEEBOR
		for(int i=0;i<=gas_num.length-1;i++){
			Double[] nGas;
			throwHashMapException((nGas = mapAFGL.get(gas_num[i].trim())), gas_num[i].trim());
			if (nGas==null) {
				System.out.println("Gas number "+gas_num[i]+" not found in AFGL db, skipped");
			} else {
				Double[] gasHSTAR_AFGL = new Double[altCIRA.length];
				RunnerUtils.lineInterp(altAFGL.length, altAFGL,  nGas, altCIRA.length, altCIRA, gasHSTAR_AFGL, IER);
				if ((Integer.parseInt(gas_num[i])==2)&&readCO2fromNetCDF) { // ���� ����� ���� = 2 � ����� co2 �� netCDF �� ..., ����� ������ �� AFGL
					ResultVect.add(co2_TRANS_CIRA);
				} else {
					ResultVect.add(gasHSTAR_AFGL);
				}
				gascnt++;
				gas_num_str = gas_num_str + " " + gas_num[i];
				DescString = DescString + " # " + gas_num[i];
				gasHSTAR_AFGL = null;
			}
		}
//		���������� ����� ��� ���������� ����� �� afgl.dat, � ������������ �� ����� ����� mlatmd
		
		System.out.println("All Files Read and Interpolated");
		
		String newDString=" ";
		
		newDString = newDString + altCIRA.length + " " + gascnt + " " + gas_num_str;
		
		writeVectorDoubleToTextFileWDescString(ResultVect, wrkPath, "gases.dat", "%12.8e",newDString);
		
		//writeVectorDoubleToTextFileWDescString(ResultVect, "/ISIMG/hstar/DATA", "GASES.dat", "%12.8e", newDString);
		
		} catch (Exception e){
			//e.fillInStackTrace();
			
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	}
	
}
