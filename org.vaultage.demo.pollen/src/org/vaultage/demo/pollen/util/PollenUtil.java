package org.vaultage.demo.pollen.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.vaultage.demo.pollen.User;

public class PollenUtil {

	private static final String PATH = "keys" + File.separator;

	public static String savePublicKey(User user) throws IOException {
		String pathString = PATH + user.getName();
		Path path = Paths.get(pathString);
		if (Files.notExists(Paths.get(PATH))) {
			Files.createDirectory(Paths.get(PATH));
		}
		Files.write(path, user.getPublicKey().getBytes());
		return getPublicKey(user.getName());
	}

	public static String getPublicKey(String username) throws IOException {
		String pathString = PATH + username;
		Path path = Paths.get(pathString);
		return new String(Files.readAllBytes(path));
	}

	/**
	 * We currently use everyone as participant but the poll originator
	 */
	public static List<String> getParticipants(String pollOriginator) throws IOException {
		Path path = Paths.get(PATH);
		List<String> names = Files.list(path)
				.map(p -> p.getFileName().toString())
				.collect(Collectors.toList());
		names.remove(pollOriginator);
		List<String> participants = new ArrayList<>();
		for (String name : names) {
			participants.add(getPublicKey(name));
		}
		return participants;
	}
}
