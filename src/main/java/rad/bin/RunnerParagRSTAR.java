package rad.bin;

import java.io.File;
import org.apache.log4j.Logger;
import platform.Module;
import platform.Stylizer;

public class RunnerParagRSTAR extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerParagRSTAR.class);

	@Override
	void runModule(Module modul) throws Exception {
		// TODO Auto-generated method stub

		try {
			String RSTAR_data_copy_path = (String) getParObjectByText(
					"RSTAR_data_copy_path", "parRunner");
			// Stylizer.Stylize("resources/hstar.xsl", FilPar
			// .getAbsolutePath(), RSTAR_data_copy_path + "/"
			// + RSTAR_data_name);
			Stylizer.Stylize("resources/transformWOconsole.xsl", wrkPath
					+ "/.service/" + modul.XMLParFileName, this.wrkPath + "/"
					+ modul.ParFileName);

			

		} catch (Exception e) {

			localLogger.fatal(e.toString());
			throw e;
		}
	}

}
