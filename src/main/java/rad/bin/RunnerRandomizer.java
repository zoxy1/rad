package rad.bin;

import java.io.File;

import org.apache.log4j.Logger;

import platform.Module;

public class RunnerRandomizer extends Runner {
	static Logger localLogger = Logger.getLogger(RunnerAfterCount.class);

	@Override
	void runModule(Module modul) throws Exception {
		// TODO Auto-generated method stub
		try {
			
			//System.out.println(modul.XMLParFileName);
			//System.out.println(Conf.getProperty(Conf.run_iter_file_path));
			//System.out.println(super.ITER_XML);
			
			double rnd = Math.random();
			double rnd_wait = getParDoubleByText("rnd_wait");
			System.out.println("Waiting, ms: "+ new Double(rnd*rnd_wait).longValue());
			localLogger.info("Waiting, ms: "+ new Double(rnd*rnd_wait).longValue());
			Thread.sleep(new Double(rnd*rnd_wait).longValue());
			
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
