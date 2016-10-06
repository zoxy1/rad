package rad.bin;

import java.io.File;
import org.apache.log4j.Logger;

import platform.Conf;
import platform.Module;
import platform.Stylizer;


public class RunnerAfterCount extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerAfterCount.class);
	@Override
	void runModule(Module modul) throws Exception {
		// TODO Auto-generated method stub
		try {
			
			//System.out.println(modul.XMLParFileName);
			//System.out.println(Conf.getProperty(Conf.run_iter_file_path));
			//System.out.println(super.ITER_XML);
			
			File IX = getFile(super.ITER_XML);
			File lCopy = getFile(IX.getName());
			lCopy.renameTo(new File(lCopy.getParent()+"/"+".rename."+lCopy.getName()));
			File fromCp = new File(getParStringByText("from_copy_path"));
			File toCp = new File(wrkPath +"/"+ (getParStringByText("to_copy_name")));
			copyFile(fromCp.getAbsolutePath(), toCp.getAbsolutePath());
			
//			String RSTAR_data_copy_path = (String) getParObjectByText(
//					"RSTAR_data_copy_path", "parRunner");
//			// Stylizer.Stylize("resources/hstar.xsl", FilPar
//			// .getAbsolutePath(), RSTAR_data_copy_path + "/"
//			// + RSTAR_data_name);
//			Stylizer.Stylize("resources/transformWOconsole.xsl", wrkPath
//					+ "/.service/" + modul.XMLParFileName, this.wrkPath + "/"
//					+ modul.ParFileName);

			

		} catch (Exception e) {

			localLogger.fatal(e.toString());
			throw e;
		}
	}

}
