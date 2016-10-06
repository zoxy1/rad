package rad.bin;

import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import platform.Module;

/**
 * ќсновна€ цель: чтение метеопараметров и концентрации газов из
 * моделей AFGL, CIRA, SEEBOR и др. <br>
 * и интерпол€ци€ на сетку высот из файла MLATMD программы HSTAR 
 * 
 *  
 */

public class RunnerInterpGases4HSTAR extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerInterpGases4HSTAR.class);
	@Override
	void runModule(Module module) throws Exception {
		try {
		//извлечение сетки высот из MLATMD_alt.dat
		HashMap<String, Double[]> mapMLATMD = readFileToHashMapOfDoubleArray("mlatmd_alt.dat");
		Double[] altMLATMD=mapMLATMD.get("altitude"); //press
		throwHashMapException(altMLATMD, "altitude");
		
		//Double[] pressMLATMD=mapMLATMD.get("pressure");
		
//		извлечение сетки высот и давлений из afgl.dat
		Vector<Double[]> ResultVect = new Vector<Double[]>();
		HashMap<String, Double[]> mapAFGL = readFileToHashMapOfDoubleArray("afgl.dat");
		
		Double[] altAFGL = mapAFGL.get("altitude");
		throwHashMapException(altAFGL, "altitude");
		
//		Double[] pressAFGL = mapAFGL.get("pressure");
		String DescString = "";

//		извлечение сетки высот, давлений и H2O из afgl.dat
		//HashMap<String, Double[]> mapSEEBOR = readFileToHashMapOfDoubleArray("seebor.dat");
		//Double[] altSEEBOR = mapSEEBOR.get("altitude");
		//throwHashMapException(altSEEBOR, "altitude");
		
		//Double[] pressSEEBOR = mapSEEBOR.get("pressure");
		//Double[] H2O_SEEBOR = mapSEEBOR.get("1");
		//throwHashMapException(H2O_SEEBOR, "1");
		
		//HashMap<String, Double[]> mapHSTAR = readFileToHashMapOfDoubleArray("press_hstar.dat");
		//Double[] pressHSTAR = mapHSTAR.get("pressure");

		//≈сли установлен параметр readCO2fromNetCDF, то читаем данные из соответствующего файла nc*.dat
		
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
		
//		извлечение сетки высот, давлений и температур из cira.dat
		HashMap<String, Double[]> mapCIRA = readFileToHashMapOfDoubleArray("apt*.dat");
		
		Double[] altCIRA = mapCIRA.get("altitude");
		throwHashMapException(altCIRA, "altitude");
		
		Double[] pressCIRA = mapCIRA.get("pressure");
		throwHashMapException(pressCIRA, "pressure");
		
		Double[] tempCIRA = mapCIRA.get("temperature");
		throwHashMapException(tempCIRA, "temperature");
		
		Double[] tempHSTAR_CIRA = new Double[altMLATMD.length];
		Double[] pressHSTAR_CIRA = new Double[altMLATMD.length];
		
		
		int IER=0;
		//RunnerUtils.lineInterp(pressCIRA.length, pressCIRA, tempCIRA, pressHSTAR.length, pressHSTAR, tempHSTAR_CIRA, IER);
		//интерпол€ци€ температуры и давлени€ CIRA на сетку высот MLATMD
		RunnerUtils.lineInterp(altCIRA.length, altCIRA, tempCIRA, altMLATMD.length, altMLATMD, tempHSTAR_CIRA, IER);
		RunnerUtils.lineInterp(altCIRA.length, altCIRA, pressCIRA, altMLATMD.length, altMLATMD, pressHSTAR_CIRA, IER);
		
//		≈сли установлен параметр readCO2fromNetCDF, то читаем данные из соответствующего файла nc*.dat
		Double[] co2_HSTAR_CIRA=null;
		if (readCO2fromNetCDF) {
			mapCO2 = readFileToHashMapOfDoubleArrayWDesc("nc*.dat");
			
			
			
			throwHashMapException(netCdfCO2 = mapCO2.get("co2"), "temperature");
			
			
			throwHashMapException(netCdfPress = mapCO2.get("pressure"), "pressure");
			
//			 интерпол€ци€ концентрации CO2 на сетку высот MLATMD
			co2_HSTAR_CIRA = new Double[pressHSTAR_CIRA.length];
			RunnerUtils.lineInterp(netCdfPress.length, netCdfPress, netCdfCO2, pressHSTAR_CIRA.length, pressHSTAR_CIRA, co2_HSTAR_CIRA, IER);
		}
		
		
		//извлечение номеров газов
		String[] gas_num = (String[])getParObjectArrayByText("gas_num", "par");

		//Double[] H2O_HSTAR = new Double[altMLATMD.length];	// подготовка массива дл€ концентрации H2O, 
															//интерполированной на сетку MLATMD 
//		собственно интерпол€ци€ H2O
		//RunnerUtils.lineInterp(altSEEBOR.length, altSEEBOR, H2O_SEEBOR, altMLATMD.length, altMLATMD, H2O_HSTAR, IER);
		
		
		// подготовка заголовка-дескриптора дл€ GASES
		ResultVect.add(altMLATMD);
		DescString = DescString + "# altitude";
		ResultVect.add(pressHSTAR_CIRA);
		DescString = DescString + "# pressure";
		ResultVect.add(tempHSTAR_CIRA);
		DescString = DescString + " # temperature";
//		ResultVect.add(H2O_HSTAR);
//		DescString = DescString + " # 1";
		
		//ƒалее блок дл€ извлечени€ газов из afgl.dat, интерпол€цию на сетку высот mlatmd
		//и одновременно формирование части строки дескриптора дл€ газов AFGL, попавших в GASES
		//газы отбираютс€ по номерам, указанным в пар. XML 
		//если номер в XML есть, то газ с этим номером попадает из AFGL в GASES
		int gascnt = 0; //1; Ёта 1 когда учитываютс€ данные SEEBOR
		String gas_num_str = ""; // " 1"; Ёта 1 когда учитываютс€ данные SEEBOR
		for(int i=0;i<=gas_num.length-1;i++){
			Double[] nGas;
			throwHashMapException((nGas = mapAFGL.get(gas_num[i].trim())), gas_num[i].trim());
			if (nGas==null) {
				System.out.println("Gas number "+gas_num[i]+" not found in AFGL db, skipped");
			} else {
				Double[] gasHSTAR_AFGL = new Double[altMLATMD.length];
				RunnerUtils.lineInterp(altAFGL.length, altAFGL,  nGas, altMLATMD.length, altMLATMD, gasHSTAR_AFGL, IER);
				if ((Integer.parseInt(gas_num[i])==2)&&readCO2fromNetCDF) { // если номер газа = 2 и нужен co2 из netCDF то ..., иначе читаем из AFGL
					ResultVect.add(co2_HSTAR_CIRA);
				} else {
					ResultVect.add(gasHSTAR_AFGL);
				}
				gascnt++;
				gas_num_str = gas_num_str + " " + gas_num[i];
				DescString = DescString + " # " + gas_num[i];
				gasHSTAR_AFGL = null;
			}
		}
//		«авершение блока дл€ извлечени€ газов из afgl.dat, и интерпол€цию на сетку высот mlatmd
		
		System.out.println("All Files Read and Interpolated");
		
		String newDString=" ";
		
		newDString = newDString + altMLATMD.length + " " + gascnt + " " + gas_num_str;
		
		writeVectorDoubleToTextFileWDescString(ResultVect, wrkPath, "GASES.dat", "%12.8e",newDString);
		
		//writeVectorDoubleToTextFileWDescString(ResultVect, "/ISIMG/hstar/DATA", "GASES.dat", "%12.8e", newDString);
		
		} catch (Exception e){
			//e.fillInStackTrace();
			
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}
	}
	
}
