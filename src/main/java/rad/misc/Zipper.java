package rad.misc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author Sergey V. Korytnikov
 * @version Zipper 1.0
 * 
 */

public class Zipper {
	private int bufferSize = 512;

	// private String sourcePath;
	// private String destPath;
	// private String zipComments;
	// private File fSourcePath;
	// private File fDestPath;

	// public Zipper(String sourcePath, String destPath) {
	// this.sourcePath = new String(sourcePath);
	// this.destPath = new String(destPath);
	// this.zipComments = new String("Zipped with JAVA Zipper Class");
	// }
	//	
	// public Zipper(String sourcePath, String destPath, String zipComments) {
	// this.sourcePath = new String(sourcePath);
	// this.destPath = new String(destPath);
	// this.zipComments = new String(zipComments);
	// }

	// public static void main(String[] args) {
	// Zipper myZipper = new Zipper(args[0], args[1]);
	// myZipper.zip();
	// }

	public static boolean zip(String inputPath, String outputPath, FilenameFilter ff) {
		return zip(inputPath, outputPath, "Java Zipper", ff);
	}

	public static boolean zip(String inputPath, String outputPath,
			String zipComments, FilenameFilter ff) {
		File fInputPath = new File(inputPath);
		File fOutputPath = new File(outputPath);
		ZipOutputStream zipOutputArchive;
		boolean result = false;

		if (!fInputPath.exists())
			return false;
//		if (fOutputPath.exists())
//			return false;

		try {
			FileOutputStream fos = new FileOutputStream(outputPath);
			CheckedOutputStream csum = new CheckedOutputStream(fos, new CRC32());

			zipOutputArchive = new ZipOutputStream(new BufferedOutputStream(
					csum));
			zipOutputArchive.setComment(zipComments);

			if (fInputPath.isDirectory()) {
				result = makeZip(zipOutputArchive, inputPath, null, ff);
			} else {
				result = makeZip(zipOutputArchive, inputPath, null, ff);
			}
			/*
			 * else if(fInputPath.isFile()) makeZip(sourcePath, "");
			 */

			zipOutputArchive.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return result;
	}

	private static boolean makeZip(ZipOutputStream zipOutputArchive,
			String inputPath, String entryPath, FilenameFilter ff) {
		File fInputPath = new File(inputPath);
		System.out.println("mz: " + inputPath + ", " + entryPath);
		try {
			if (fInputPath.isDirectory()) {
				String[] srcFileList;
				if (ff==null) {
				srcFileList = fInputPath.list();
				} else {
					srcFileList = fInputPath.list(ff); 
				}
				
				for (int i = 0; i < srcFileList.length; i++) {
					File fSourcePath = new File(inputPath + File.separator
							+ srcFileList[i]);

					if (fSourcePath.isDirectory()) {
						if (entryPath == null)
							makeZip(zipOutputArchive, inputPath
									+ File.separator + srcFileList[i],
									srcFileList[i], null);
						else
							makeZip(zipOutputArchive, inputPath
									+ File.separator + srcFileList[i],
									entryPath + File.separator + srcFileList[i], null);
					} else if (fSourcePath.isFile()) {
						FileInputStream in = new FileInputStream(fSourcePath
								.getPath());
						if (entryPath == null) {
							zipOutputArchive.putNextEntry(new ZipEntry(
									fSourcePath.getName()));
							System.out.println("entry: "
									+ fSourcePath.getName());
						} else {
							zipOutputArchive.putNextEntry(new ZipEntry(
									entryPath + File.separator
											+ fSourcePath.getName()));
							System.out.println("entry: " + entryPath
									+ File.separator + fSourcePath.getName());
						}

						int cnt;
						byte buf[] = new byte[512];
						while ((cnt = in.read(buf)) != -1)
							zipOutputArchive.write(buf, 0, cnt);
						zipOutputArchive.flush();
						in.close();
					}
				}
			} else {
				File fSourcePath = fInputPath;
				FileInputStream in = new FileInputStream(fSourcePath.getPath());
				if (entryPath == null) {
					zipOutputArchive.putNextEntry(new ZipEntry(fSourcePath
							.getName()));
					System.out.println("entry: " + fSourcePath.getName());
				}; 
//				else
//				{
//					zipOutputArchive.putNextEntry(new ZipEntry(entryPath
//							+ File.separator + fSourcePath.getName()));
//					System.out.println("entry: " + entryPath + File.separator
//							+ fSourcePath.getName());
//				}
				int cnt;
				byte buf[] = new byte[512];
				//zipOutputArchive.getClass()
				while ((cnt = in.read(buf)) != -1)
					
					zipOutputArchive.write (buf, 0, cnt);
				zipOutputArchive.flush();
				in.close();

			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// public int unzip() {
	// FileOutputStream fOutStream;
	//		
	// this.fSourcePath = new File(this.sourcePath);
	//		
	// try {
	// /*Создаем объекты - потоки для чтения и записи файла*/
	// BufferedOutputStream outStream = null;
	// BufferedInputStream inStream = null;
	//			
	// /*получаем список элементов zip-файла*/
	// ZipEntry entry;
	// ZipFile zFile = new ZipFile(fSourcePath);
	// Enumeration entryList = zFile.entries();
	//			
	// /*Пока не пройден весь список*/
	// while(entryList.hasMoreElements()) {
	// /*Получаем очередной элемент списка*/
	// entry = (ZipEntry) entryList.nextElement();
	// inStream = new BufferedInputStream (zFile.getInputStream(entry));
	// int count;
	// byte data[] = new byte[512];
	// /*Создаем новый объект класса File и проверяем, находится ли он
	// * в поддиректории*/
	// File g = new File(entry.getName());
	// if (g.getParent() != null) {
	// /*Пытаемся создать дерево поддиректорий для текущего файла*/
	// File f = new File(destPath + File.separator + g.getParent());
	// if (! f.mkdirs()) {
	// return -1;
	// }
	// }
	// /*Создаем поток для записи в файл*/
	// fOutStream = new FileOutputStream(destPath + File.separator +
	// entry.getName());
	// outStream = new BufferedOutputStream(fOutStream, 512);
	// /*Считываем из zip-файла данные в буфер и записываем их в
	// * выходной файл*/
	// while((count = inStream.read(data, 0, 512)) != -1) {
	// outStream.write(data, 0, count);
	// }
	// outStream.flush();
	// outStream.close();
	// inStream.close();
	// }
	// } catch(Exception e) {
	// return -1;
	// }
	// return 0;
	// }

	public void unzip(String inputZip, String outputPath) {
		unzip(new File(inputZip), outputPath);
	}

	public void unzip(File inputZip, String outputPath) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		File fOutputPath = new File(outputPath);
		if (!fOutputPath.exists()) {
			fOutputPath.mkdir();
		}

		try {
			ZipEntry zipEntry = null;
			ZipFile zipFile = new ZipFile(inputZip);

			Enumeration entryList = zipFile.entries();
			while (entryList.hasMoreElements()) {
				zipEntry = (ZipEntry) entryList.nextElement();
				bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));

				File fZipOutput = new File(outputPath + File.separator
						+ zipEntry.getName());
				if (zipEntry.isDirectory()) {
					if (!fZipOutput.isDirectory()) {
						fZipOutput.mkdir();
					}
				} else {
					File parent = new File(fZipOutput.getParent());
					if (!parent.isDirectory()) {
						parent.mkdirs();
					}
					if (fZipOutput.isFile()) {
						fZipOutput.delete();
					}

					// System.out.println("." + fZipOutput.getAbsolutePath() +
					// ".");
					bos = new BufferedOutputStream(new FileOutputStream(
							fZipOutput), bufferSize);
					int countBytes;
					byte[] buffer = new byte[bufferSize];
					while ((countBytes = bis.read(buffer, 0, bufferSize)) != -1) {
						bos.write(buffer, 0, countBytes);
					}
					bos.flush();
					bos.close();
					bis.close();
				}
			}
			zipFile.close();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
