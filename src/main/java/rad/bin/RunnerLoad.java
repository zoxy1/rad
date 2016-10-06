 package rad.bin;

import platform.Module;
import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import platform.Debug;

/*import platform.Splitter;
import platform.Charger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.xpath.XPathAPI;*/
//import org.ap

public class RunnerLoad extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerLoad.class);
	void runModule(Module module) throws Exception {
		try {
		File orSignalFile = null;
		HashMap<String, Double[]> map = new HashMap<String, Double[]>();
		Vector<Double []> vect = null;
		String rStr = null;
		Double time = null, spar = null;
		
		String lidar_data_file = getParStringByText("lidar_data_file");
		copyFile(lidar_data_file, wrkPath+"/count.txt");
		
		clearFile("signal"); // удал€ем все файлы начин с этого слова, нужно на будущее
		String sigInputFile = null;
		sigInputFile = "count.txt";
		//copyInFilesToMdWrkPath();
		
			orSignalFile = getFile(sigInputFile); //получаем файл по имени
			map = readFileToHashMapOfDoubleArray(sigInputFile); //читаем столбцы файла в хэш таблицу массивов
			BufferedReader br = new BufferedReader((new FileReader(orSignalFile)));
			for(int i=1;i<=8;i++) {
				rStr = br.readLine();
			}
			//дочитываем некоторые пол€ из файла-данных (дл€ данных из якутска)
			
			time = Double.valueOf(rStr.toLowerCase()); //длительностm строба
			double spar2 = (3e+8*time*10e-12)/2./1000.; //пространственное разрешение
			
			spar = (3e+8)/(2.*time)/1000.;
			//установка параметра spatial_resolution (пример в radParams.xml)
			setParDoubleByText("spatial_resolution", spar);
			
			//установка параметра strob_duration (пример в radParams.xml)
			setParDoubleByText("strob_duration", time);
			 
			//чтение параметра initial_altitude (пример в radParams.xml)
			Double dblInitAlt = Double.valueOf(getParDoubleByText("initial_altitude"));
			Double stdSetka[];
			
			//получение массива прочитанного из файла из хэш таблицы
			Double[] dub = map.get("raw_signal");
			stdSetka = new Double[dub.length]; // создание нового массива
			for(int i = 0; i<=stdSetka.length-1; i++) {
				stdSetka[i] = dblInitAlt + spar*i; // заполнение массива
			}
			writeArrToTextFile(stdSetka, mdWrkPath + "/setka.txt", "%12.8e"); // сброс промежуточного значени€ в текст-файл
			
			//создаем новый вектор, добавл€ем в него массивы
			vect = new Vector<Double []>(); 
			vect.add(stdSetka);
			vect.add(dub);
			
			// выводим содержимое этого массива в текстовый файл
			//writeVectorDoubleToTextFile(vect, wrkPath, "signal.txt", "%10.4f");
			writeVectorDoubleToTextFileWDesc(vect,"signal.txt","%12.8e");
		} catch (Exception e){
			//e.fillInStackTrace();
			
			localLogger.fatal(e.toString());
//			proceesException(e, localLogger, this.getClass().getName());
			throw e;
		}

					
	}

}
