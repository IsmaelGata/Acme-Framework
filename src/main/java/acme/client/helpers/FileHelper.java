/*
 * FileHelper.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.client.helpers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.io.FileUtils;

public abstract class FileHelper {

	// Constructors -----------------------------------------------------------

	protected FileHelper() {
	}

	// Business methods -------------------------------------------------------

	public static boolean exists(final String filepath) {
		assert !StringHelper.isBlank(filepath);

		boolean result;
		File file;

		file = new File(filepath);
		result = file.exists();

		return result;
	}

	public static void roll(final String filepath) {
		assert !StringHelper.isBlank(filepath);

		long currentTime;
		Timestamp timestamp;
		Path path;
		String suffix, newFilepath, folder, filename, stem, extension;
		int dotPos;
		File source, target;

		if (FileHelper.exists(filepath)) {
			path = Paths.get(filepath);
			folder = path.getParent().toString();
			filename = path.getFileName().toString();
			dotPos = filename.lastIndexOf(".");
			if (dotPos == -1) {
				stem = filename;
				extension = "";
			} else {
				stem = filename.substring(0, dotPos);
				extension = filename.substring(dotPos);
			}

			currentTime = System.currentTimeMillis();
			timestamp = new Timestamp(currentTime);
			suffix = timestamp.toString().replaceFirst("\\.\\d+$", "").replace(" ", "@").replace(":", ".");

			newFilepath = String.format("%s/%s%s;%s", folder, stem, extension, suffix);

			source = new File(filepath);
			target = new File(newFilepath);
			assert source.renameTo(target) : String.format("Could not roll '%s' to '%s'", filepath, newFilepath);
		}
	}

	public static List<File> listFiles(final String folderPath, final String extension) {
		assert !StringHelper.isBlank(folderPath);
		assert !StringHelper.isBlank(extension);

		List<File> result;
		File folder;
		String[] extensions;

		folder = new File(folderPath);
		extensions = new String[] {
			extension
		};
		result = (List<File>) FileUtils.listFiles(folder, extensions, true);

		return result;
	}

}
