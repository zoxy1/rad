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
		
		clearFile("signal"); // ������� ��� ����� ����� � ����� �����, ����� �� �������
		String sigInputFile = null;
		sigInputFile = "count.txt";
		//copyInFilesToMdWrkPath();
		
			orSignalFile = getFile(sigInputFile); //�������� ���� �� �����
			map = readFileToHashMapOfDoubleArray(sigInputFile); //������ ������� ����� � ��� ������� ��������
			BufferedReader br = new BufferedReader((new FileReader(orSignalFile)));
			for(int i=1;i<=8;i++) {
				rStr = br.readLine();
			}
			//���������� ��������� ���� �� �����-������ (��� ������ �� �������)
			
			time = Double.valueOf(rStr.toLowerCase()); //�����������m ������
			double spar2 = (3e+8*time*10e-12)/2./1000.; //���������������� ����������
			
			spar = (3e+8)/(2.*time)/1000.;
			//��������� ��������� spatial_resolution (������ � radParams.xml)
			setParDoubleByText("spatial_resolution", spar);
			
			//��������� ��������� strob_duration (������ � radParams.xml)
			setParDoubleByText("strob_duration", time);
			 
			//������ ��������� initial_altitude (������ � radParams.xml)
			Double dblInitAlt = Double.valueOf(getParDoubleByText("initial_altitude"));
			Double stdSetka[];
			
			//��������� ������� ������������ �� ����� �� ��� �������
			Double[] dub = map.get("raw_signal");
			stdSetka = new Double[dub.length]; // �������� ������ �������
			for(int i = 0; i<=stdSetka.length-1; i++) {
				stdSetka[i] = dblInitAlt + spar*i; // ���������� �������
			}
			writeArrToTextFile(stdSetka, mdWrkPath + "/setka.txt", "%12.8e"); // ����� �������������� �������� � �����-����
			
			//������� ����� ������, ��������� � ���� �������
			vect = new Vector<Double []>(); 
			vect.add(stdSetka);
			vect.add(dub);
			
			// ������� ���������� ����� ������� � ��������� ����
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
