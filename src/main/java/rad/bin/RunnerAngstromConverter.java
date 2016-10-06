package rad.bin;

import java.util.HashMap;
import java.util.Vector;

import platform.Module;

public class RunnerAngstromConverter extends Runner {

	@Override
	void runModule(Module modul) throws Exception {
		// TODO преобразование данных на заданную длину волныя
		
		String freqInputFile = getParStringByText("freq_file_name");
		HashMap<String, Double[]> freqMap = readFileToHashMapOfDoubleArray(freqInputFile );
		Double[] freqArr = freqMap.get("frequency");
		
		String alfaInputFile = getParStringByText("sprintars_alfa_file_name_prefix");
		HashMap<String, Double[]> alfaMap = readFileToHashMapOfDoubleArrayWDesc(alfaInputFile.trim()+"*");
		Double[] alfaArr =  alfaMap.get(getParStringByText("sprintars_alfa_var_name"));
		
		String tauInputFile = getParStringByText("sprintars_tau_file_name_prefix");
		HashMap<String, Double[]> alfaTauae = readFileToHashMapOfDoubleArrayWDesc(tauInputFile.trim()+"*");
		Double[] tauArr =  alfaTauae.get(getParStringByText("sprintars_tau_var_name"));
		
		long tauae550 = new Double(tauArr[0]*1e7).longValue();
		double mlt = 1e7;
		long alfa = new Double(alfaArr[0]*1e7).longValue();
		long pwr = (long)(1e7*1e7);
		double w500 =  550;
		//double 
		Double tauF[] = new Double[freqArr.length];
		
		for (int i = 0; i < freqArr.length; i++) {
			long freq = new Double(freqArr[i]*1e7).longValue();
//			long wl = pwr / freq;
//			System.out.println("WaveLength: " + wl+" "+freqArr[i]);
			 //new Double( 550*1e7).longValue();
			tauF[i] = tauae550 * Math.pow((pwr/(w500*freq)), (-alfa/mlt))/1e7;
		}
		Vector vect = new Vector<Double[]>();
		vect.add(freqArr);
		vect.add(tauF);
		writeVectorDoubleToTextFileWDesc(vect, "angstrom_tau.dat", "%20.16e");
		
	}

}
