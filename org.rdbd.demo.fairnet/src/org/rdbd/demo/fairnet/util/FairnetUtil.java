package org.rdbd.demo.fairnet.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FairnetUtil {

	public static String getTimestamp() {
		SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return gmtDateFormat.format(new Date()) + gmtDateFormat.getTimeZone().getID();
	}

	public static Date timestampToDate(String timestampString) throws ParseException {
		String dateString = timestampString.substring(0, timestampString.length() - 2);
		Date date = new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(dateString);
		return date;
	}

	public static String readFile(String path) {
		try {
			File f = new File(path);
			byte[] fileBytes = Files.readAllBytes(f.toPath());
			return new String(fileBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> getFiles(String path) {
		File file = new File(path);
		List<String> result = Arrays.asList(file.list());
		return result;
	}

	public static void saveFile(String path, String text) {
		try {
			Files.write(Paths.get(path), text.getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
