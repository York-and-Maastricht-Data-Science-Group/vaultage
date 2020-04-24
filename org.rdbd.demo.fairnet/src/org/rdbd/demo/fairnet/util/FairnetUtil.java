package org.rdbd.demo.fairnet.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FairnetUtil {

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
