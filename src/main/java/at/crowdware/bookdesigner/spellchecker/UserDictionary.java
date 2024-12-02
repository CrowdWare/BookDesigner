/*
 * Copyright (c) 2018 Karl Tauber <karl at jformdesigner dot com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package at.crowdware.bookdesigner.spellchecker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import at.crowdware.bookdesigner.options.Options;
import at.crowdware.bookdesigner.projects.ProjectManager;

/**
 * A user dictionary for spell checking.
 *
 * @author Karl Tauber
 */
class UserDictionary
{
	private final File file;
	private List<String> lines;
	private List<String> words;
	private boolean isSorted;

	UserDictionary() {
		String userDictionary = Options.getUserDictionary();
		File f = new File(userDictionary);

		// user dictionary path may be relative to project folder
		if (!f.isAbsolute() && ProjectManager.getActiveProject() != null)
			f = new File(ProjectManager.getActiveProject(), userDictionary);

		file = f;

		load();
	}

	UserDictionary(File file) {
		this.file = file;

		load();
	}

	private void load() {
		try {
			if (file.isFile())
				lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (lines == null)
			lines = new ArrayList<>();

		words = new ArrayList<>(lines.size());
		for (String line : lines) {
			if (!line.startsWith("#"))
				words.add(line);
		}

		isSorted = lines.equals(words) && lines.equals(sort(lines));
	}

	private void save() {
		try {
			Files.write(file.toPath(), isSorted ? sort(lines) : lines, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	List<String> getWords() {
		return words;
	}

	void addWord(String word) {
		if (words.contains(word))
			return;

		lines.add(word);
		words.add(word);

		save();
	}

	private List<String> sort(List<String> list) {
		List<String> sortedList = new ArrayList<>(list);
		sortedList.sort((l1, l2) -> {
			int r = l1.compareToIgnoreCase(l2);
			return (r == 0) ? l1.compareTo(l2) : r;
		});
		return sortedList;
	}
}
