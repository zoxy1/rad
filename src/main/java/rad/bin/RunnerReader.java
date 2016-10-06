package rad.bin;



import java.io.File;
import java.util.HashMap;

import platform.Module;

public class RunnerReader extends Runner {

	@Override
	void runModule(Module modul) throws Exception {
		// TODO Auto-generated method stub
		File nFile = getFile("GASES.dat");
		HashMap<String, Double[]> mapGAS = readFileToHashMapOfDoubleArray("GASES.dat");
		Double[] alt = mapGAS.get("altitude");
		Double[] so2 = mapGAS.get("SO2");
		System.out.println("Read");
		
	}

}
