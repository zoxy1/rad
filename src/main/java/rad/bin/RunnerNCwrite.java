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
		  
		  // открываем существующий файл netCDF для записи 
		  NetcdfFileWriteable ncfile = NetcdfFileWriteable.openExisting(fname, true);
		  // определяем количество размерностей
		  int dim_size = ncfile.getDimensions().size();
		  
		  HashMap<String, Double[]> coord = readFileToHashMapOfDoubleArrayWDesc(trace);
			Double[] latArr = coord.get("Latitude");
			Double[] lonArr = coord.get("Longitude");
			Double[] timeArr = coord.get("JulianTime");
			
			if (latArr==null){
				throw new Exception("latArr == null");
			}
		  
		  // ищем главную размерность
		  Dimension timeDim = ncfile.findDimension("time"); 

		   //формируем массив для занесения в netCDF
		   ArrayDouble A = new ArrayDouble.D1(timeDim.getLength()); //, lonDim.getLength());
		   int i,j;
		   Index ima = A.getIndex();
		   for (i=0; i<timeArr.length; i++) {
		     // заполняем массив временем в миллисекундах
		       A.setDouble(ima.set(i), convertJulianToGregorianCalendar(timeArr[i]));
		   }
		   int[] origin = new int[1];
		   // записываем массив времени в файл
		   ncfile.write("time", origin, A);
		   ncfile.flush();
		   
		   // формируем и записываем массив высот
		   
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
		   
		   // формируем и записываем массив широт
		   
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
		   
		   // формируем и записываем массив долгот
		   
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
