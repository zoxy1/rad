package rad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import rad.misc.Zipper;

public class ZipZip extends Zipper {

	/**
	 * @param args
	 */
	static File inputPath, outputPath;

	static int niZ;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		if (args.length != 3) {
			System.out.println("Usage: <inputPath> <number> <outputPath>");
		} else if (!(inputPath = new File(args[0])).isDirectory()) {
			System.out.println("Usage: <inputPath> <number> <outputPath>");
			System.out.println("<inputPath> must be Directory");
		} else if (!(outputPath = new File(args[2])).isDirectory()) {
			System.out.println("Usage: <inputPath> <number> <outputPath>");
			System.out.println("<outputPath> must be Directory");
		} else if ((niZ = Integer.parseInt(args[1])) <= 0) {
			System.out.println("Usage: <inputPath> <number> <outputPath>");
			System.out.println("<number> must be positive Integer");
		}
		visitAllFilesAndRun();

	}

	public static void visitAllFilesAndRun() {

		FilenameFilter fileFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				boolean ac;
				// String name = file.getName();
				ac = (new File(dir.getAbsolutePath() + "/" + name)
						.isDirectory())
						&& (!name.startsWith("."));
				return ac;
			}
		};

		try {
			String[] children = inputPath.list(fileFilter);
			if (children.length < niZ) {
				// for (int i = 0; i < children.length; i++) {
				// visitAllFilesAndRun(data, new File(dir, children[i]));
				String currentName = formName(outputPath, 1, children.length);
				zip(inputPath.getAbsolutePath(), currentName, null);
				File prj = new File("resources/project.ini");
				if (prj.exists()) {
					// zip("resources/project.ini", currentName);
					File[] files = new File[1];
					files[0] = new File("resources/project.ini");
					addFilesToExistingZip(new File(currentName), files);
				}

				// }
			} else {
				int hol = children.length / niZ;
				int las = children.length % niZ;
				boolean ilnnull = (las != 0);
				if (ilnnull) {
					hol++;
				}
				for (int i = 1; i <= hol; i++) {
					int low = (i-1)*niZ;
					int upp  = i*niZ;
					if (ilnnull&&(i==hol)) 
						upp  = (i-1)*niZ+las;
					
					String currentName = formName(outputPath, low+1,
							upp);
					//zip("resources/project.ini", currentName, null);
					File files[] = new File[upp-low];	
					for (int j = 0; j <=upp-low-1; j++ ){
						files[j] = new File(inputPath+File.separator+children[low+j]);
					}
					//addFilesToExistingZip(new File(currentName), files);
					FileExpFilter FEF = new FileExpFilter(files);
					zip(inputPath.getAbsolutePath(), currentName, FEF);
					File prj = new File("resources/project.ini");
					if (prj.exists()) {
						// zip("resources/project.ini", currentName);
						files = new File[1];
						files[0] = new File("resources/project.ini");
						addFilesToExistingZip(new File(currentName), files);
					}				
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String formName(File outputPath, int beg, int end) {
		String name = outputPath.getName();

		return outputPath.getAbsolutePath() + "/" + name + "_" + beg + "_"
				+ end + ".zip";
	}

	public static void addFilesToExistingZip(File zipFile, File[] files)
			throws IOException {
		// get a temp file
		File tempFile = File.createTempFile(zipFile.getName(), null);
		// delete it, otherwise you cannot rename your existing zip to it.
		tempFile.delete();

		boolean renameOk = zipFile.renameTo(tempFile);
		if (!renameOk) {
			throw new RuntimeException("could not rename the file "
					+ zipFile.getAbsolutePath() + " to "
					+ tempFile.getAbsolutePath());
		}
		byte[] buf = new byte[1024];

		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			boolean notInFiles = true;
			for (File f : files) {
				if (f.getName().equals(name)) {
					notInFiles = false;
					break;
				}
			}
			if (notInFiles) {
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(name));
				// Transfer bytes from the ZIP file to the output file
				int len;
				while ((len = zin.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		// Close the streams
		zin.close();
		// Compress the files
		for (int i = 0; i < files.length; i++) {
			InputStream in = new FileInputStream(files[i]);
			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(files[i].getName()));
			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			// Complete the entry
			out.closeEntry();
			in.close();
		}
		// Complete the ZIP file
		out.close();
		tempFile.delete();
	}

}
class FileExpFilter implements FilenameFilter {
	HashMap<String, File> HMF = new HashMap<String, File>();
	
	public FileExpFilter(File files[]) {
		
		for (int i=0;i<files.length;i++){
			HMF.put(files[i].getName(), files[i]);
		}
	}
	
	public boolean accept(File dir, String name) {
		boolean ac;
		// String name = file.getName();
		ac = (HMF.get(name)!=null);
		return ac;
	}
};


