package rad.bin;

import java.util.Date;
import java.util.HashMap;

import platform.Module;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class RunnerNCwrite extends Runner {

	@Override
	void runModule(Module modul) throws Exception {
		// TODO Auto-generated method stub
		try {
			writeExisting(getParStringByText("allTrackNCname"), getParStringByText("coordinatesListFile"));
		} catch (Exception e) {
			// e.fillInStackTrace();
			runnerLogger.fatal(e.toString());
			proceesException(e, runnerLogger, this.getClass().getName());
			throw e;
		}
	}
	 void writeExisting(String fname, String trace) throws Exception {
		  
		  // ��������� ������������ ���� netCDF ��� ������ 
		  NetcdfFileWriteable ncfile = NetcdfFileWriteable.openExisting(fname, true);
		  // ���������� ���������� ������������
		  int dim_size = ncfile.getDimensions().size();
		  
		  HashMap<String, Double[]> coord = readFileToHashMapOfDoubleArrayWDesc(trace);
			Double[] latArr = coord.get("Latitude");
			Double[] lonArr = coord.get("Longitude");
			Double[] timeArr = coord.get("JulianTime");
			
			if (latArr==null){
				throw new Exception("latArr == null");
			}
		  
		  // ���� ������� �����������
		  Dimension timeDim = ncfile.findDimension("time"); 

		   //��������� ������ ��� ��������� � netCDF
		   ArrayDouble A = new ArrayDouble.D1(timeDim.getLength()); //, lonDim.getLength());
		   int i,j;
		   Index ima = A.getIndex();
		   for (i=0; i<timeArr.length; i++) {
		     // ��������� ������ �������� � �������������
		       A.setDouble(ima.set(i), convertJulianToGregorianCalendar(timeArr[i]));
		   }
		   int[] origin = new int[1];
		   // ���������� ������ ������� � ����
		   ncfile.write("time", origin, A);
		   ncfile.flush();
		   
		   // ��������� � ���������� ������ �����
		   
		   ArrayFloat F = new ArrayFloat.D1(timeDim.getLength()); //, lonDim.getLength());
		   //int i,j;
		    ima = F.getIndex();
		   for (i=0; i<timeArr.length; i++) {
		     //for (j=0; j<lonDim.getLength(); j++) {
		       F.setFloat(ima.set(i), (float) (i*0.));
		     //}
		   }
		   float[] originF = new float[1];
		   ncfile.write("altitude", origin, F);
		   ncfile.flush();
		   
		   // ��������� � ���������� ������ �����
		   
		   ArrayFloat Lat = new ArrayFloat.D1(timeDim.getLength()); //, lonDim.getLength());
		   //int i,j;
		    ima = Lat.getIndex();
		   for (i=0; i<timeArr.length; i++) {
		     //for (j=0; j<lonDim.getLength(); j++) {
		       Lat.setFloat(ima.set(i),  latArr[i].floatValue());
		     //}
		   }
		   originF = new float[1];
		   ncfile.write("latitude", origin, Lat);
		   ncfile.flush();
		   
		   // ��������� � ���������� ������ ������
		   
		   ArrayFloat Lon = new ArrayFloat.D1(timeDim.getLength()); //, lonDim.getLength());
		    ima = Lon.getIndex();
		   for (i=0; i<timeArr.length; i++) {
		     
			   Lon.setFloat(ima.set(i), lonArr[i].floatValue());
		
		   }
		   originF = new float[1];
		   ncfile.write("longitude", origin, Lon);
		   ncfile.flush();
		  
	  }
}
