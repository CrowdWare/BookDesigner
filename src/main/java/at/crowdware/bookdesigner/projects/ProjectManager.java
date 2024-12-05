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

/*
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of BookDesigner.
 *
 * BookDesigner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BookDesigner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BookDesigner.  If not, see <https://www.gnu.org/licenses/>.
 */

package at.crowdware.bookdesigner.projects;

import java.awt.print.Book;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import at.crowdware.bookdesigner.util.App;
import at.crowdware.bookdesigner.util.Ebook;
import at.crowdware.bookdesigner.util.Page;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import at.crowdware.bookdesigner.BookDesignerApp;
import at.crowdware.bookdesigner.Messages;
import at.crowdware.bookdesigner.util.Utils;
import org.jetbrains.annotations.Nullable;
/**
 * Project manager.
 *
 * @author Karl Tauber
 */
public class ProjectManager
{
	private static final String KEY_PROJECTS = "projects";
	private static final String KEY_PATH = "path";
	private static final String KEY_ACTIVE_PROJECT = "active";
	private static final String KEY_LAST_PROJECT_DIRECTORY = "lastDirectory";

	// 'activeProject' property
	private static final ObjectProperty<File> activeProject = new SimpleObjectProperty<>();
	public static File getActiveProject() { return activeProject.get(); }
	public static void setActiveProject(File activeProject) {
		ProjectManager.activeProject.set(activeProject);
		loadApp();
		loadBook();
	}
	public static ObjectProperty<File> activeProjectProperty() { return activeProject; }

	// 'projects' property
	private static final ObservableList<File> projects = FXCollections.observableArrayList();
	static ObservableList<File> getProjects() { return projects; }

	// sml
	@Nullable
	public static App app = null;

	@Nullable
	public static Ebook book = null;

	@Nullable
	public static Page page = null;

	// Getter und Setter für statische Felder (optional)
	@Nullable
	public static App getApp() {
		return app;
	}

	public static void setApp(@Nullable App app) {
		ProjectManager.app = app;
	}

	@Nullable
	public static Ebook getBook() {
		return book;
	}

	public static void setBook(@Nullable Ebook book) {
		ProjectManager.book = book;
	}

	@Nullable
	public static Page getPage() {
		return page;
	}

	public static void setPage(@Nullable Page page) {
		ProjectManager.page = page;
	}

	static {
		Preferences state = getProjectsState();

		// load recent projects
		projects.addAll(getRecentProjects());

		// save active project on change
		activeProjectProperty().addListener((observer, oldProject, newProject) -> {
			Utils.putPrefs(state, KEY_ACTIVE_PROJECT, (newProject != null) ? newProject.getAbsolutePath() : null, null);

			// add to recent projects
			if (newProject != null && !projects.contains(newProject))
				projects.add(newProject);
		});

		// save recent projects on change
		projects.addListener((ListChangeListener<File>) change -> {
			while (change.next()) {
				if (change.wasAdded()) {
					for (File f : change.getAddedSubList()) {
						getProjectState(f);
					}
				}
				if (change.wasRemoved()) {
					for (File f : change.getRemoved())
						removeProjectState(f);
				}
			}
		});

		// initialize active project
		String activeProjectName = state.get(KEY_ACTIVE_PROJECT, null);
		if (activeProjectName != null)
			setActiveProject(new File(activeProjectName));
		if (getActiveProject() == null && !projects.isEmpty())
			setActiveProject(projects.get(0));
	}

	public static void setProject(String folder) {
		Preferences state = getProjectsState();
		File selectedFolder = new File(folder);
		state.put(KEY_LAST_PROJECT_DIRECTORY, selectedFolder.getAbsolutePath());
		setActiveProject(selectedFolder);
	}

	public static void openProject(Window ownerWindow) {
		DirectoryChooser fileChooser = new DirectoryChooser();
		fileChooser.setTitle(Messages.get("ProjectManager.openChooser.title"));

		Preferences state = getProjectsState();
		String lastProjectDirectory = state.get(KEY_LAST_PROJECT_DIRECTORY, null);
		File file = new File((lastProjectDirectory != null) ? lastProjectDirectory : ".");
		if (!file.isDirectory())
			file = new File(".");
		fileChooser.setInitialDirectory(file);

		File selectedFile = fileChooser.showDialog(ownerWindow);
		if (selectedFile == null)
			return;

		state.put(KEY_LAST_PROJECT_DIRECTORY, selectedFile.getAbsolutePath());

		setActiveProject(selectedFile);

	}

	public static Preferences getActiveProjectState() {
		return (getActiveProject() != null) ? getProjectState(getActiveProject()) : null;
	}

	public static Preferences getProjectState(File project) {
		return getProjectState(project, true);
	}

	private static Preferences getProjectState(File project, boolean create) {
		Preferences state = getProjectsState();

		try {
			String[] childrenNames = state.childrenNames();
			for (String childName : childrenNames) {
				Preferences child = state.node(childName);
				String path = child.get(KEY_PATH, null);
				if (path != null && project.equals(new File(path)))
					return child;
			}

			if (!create)
				return null;

			int lastID = 0;
			for (String childName : childrenNames) {
				try {
					int childID = Integer.parseInt(childName);
					if (childID > lastID)
						lastID = childID;
				} catch (NumberFormatException ex) {
					// ignore
				}
			}

			Preferences newNode = state.node(String.valueOf(lastID + 1));
			newNode.put(KEY_PATH, project.getAbsolutePath());
			return newNode;
		} catch (BackingStoreException ex) {
			// ignore
			ex.printStackTrace();
			return null;
		}
	}

	private static void removeProjectState(File project) {
		Preferences projectState = getProjectState(project, false);
		if (projectState != null) {
			try {
				projectState.removeNode();
			} catch (BackingStoreException ex) {
				// ignore
				ex.printStackTrace();
			}
		}
	}

	private static List<File> getRecentProjects() {
		Preferences state = getProjectsState();
		ArrayList<File> projects = new ArrayList<>();

		try {
			String[] childrenNames = state.childrenNames();
			for (String childName : childrenNames) {
				Preferences child = state.node(childName);
				String path = child.get(KEY_PATH, null);
				if (path != null)
					projects.add(new File(path));
			}
		} catch (BackingStoreException ex) {
			// ignore
			ex.printStackTrace();
		}

		return projects;
	}

	public static void createProjectFiles(String path, String name, String theme) {
		File dir = new File(path + "/" + name);
		dir.mkdirs();

		File pages = new File(path + "/" + name + "/pages");
		pages.mkdirs();
		File videos = new File(path + "/" + name + "/videos");
		videos.mkdirs();
		File sounds = new File(path + "/" + name + "/sounds");
		sounds.mkdirs();
		File images = new File(path + "/" + name + "/images");
		images.mkdirs();
		File models = new File(path + "/" + name + "/models");
		models.mkdirs();
		File textures = new File(path + "/" + name + "/textures");
		textures.mkdirs();
		File app = new File(path + "/" + name + "/app.sml");

		StringBuilder appContent = new StringBuilder();
		appContent.append("App {\n    smlVersion: \"1.1\"\n    name: \"").append(name).append("\"\n    version: \"1.0\"\n    icon: \"icon.png\"\n\n");

		if ("Light".equals(theme)) {
			appContent.append(writeLightTheme());
		} else {
			appContent.append(writeDarkTheme());
		}

		appContent.append("// deployment start - don't edit here\n\n// deployment end\n}\n\n");

		try {
			Files.write(Paths.get(app.getPath()), appContent.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		File home = new File(path + "/" + name + "/pages/home.sml");
		try {
			Files.write(Paths.get(home.getPath()), "Page {\n    padding: \"8\"\n\n    Column {\n        padding: \"8\"\n\n        Text { text: \"Home\" }\n    }\n}\n".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("create: " + path + ", " + name);

		copyResourceToFile("python/server.py", path + "/" + name + "/server.py");
		copyResourceToFile("python/upd_deploy.py", path + "/" + name + "/upd_deploy.py");
		copyResourceToFile("icons/default.icon.png", path + "/" + name + "/images/icon.png");

		File parts = new File(path + "/" + name + "/parts");
		parts.mkdirs();
		File homemd = new File(path + "/" + name + "/parts/home.md");

		try {
			Files.write(Paths.get(homemd.getPath()), "# BookTitle\nLorem ipsum dolor\n".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		File book = new File(path + "/" + name + "/ebook.sml");
		String bookContent = "Ebook {\n    smlVersion: \"1.1\"\n    name: \"" + name + "\"\n    version: \"1.0\"\n    theme: \"Epub3\"\n    creator: \"\"\n    language: \"en\"\n\n    Part {\n        src: \"home.md\"\n    }\n}\n";
		try {
			Files.write(Paths.get(book.getPath()), bookContent.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		setProject(path + "/" + name);
		loadApp();
		loadBook();
	}

	public static void copyResourceToFile(String resourcePath, String outputPath) {
		System.out.println("copyResourceToFile: " + resourcePath + ", " + outputPath);

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(resourcePath);

		if (inputStream != null) {
			try {
				Path targetPath = Paths.get(outputPath);
				Files.createDirectories(targetPath.getParent());
				Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Ressource " + resourcePath + " konnte nicht gefunden werden.");
		}
	}

	public static String writeDarkTheme() {
		StringBuilder content = new StringBuilder("\n");
		content.append("    Theme {\n");
		content.append("        primary: \"#FFB951\"\n");
		content.append("        onPrimary: \"#452B00\"\n");
		content.append("        primaryContainer: \"#633F00\"\n");
		content.append("        onPrimaryContainer: \"#FFDDB3\"\n");
		content.append("        secondary: \"#DDC2A1\"\n");
		content.append("        onSecondary: \"#3E2D16\"\n");
		content.append("        secondaryContainer: \"#56442A\"\n");
		content.append("        onSecondaryContainer: \"#FBDEBC\"\n");
		content.append("        tertiary: \"#B8CEA1\"\n");
		content.append("        onTertiary: \"#243515\"\n");
		content.append("        tertiaryContainer: \"#3A4C2A\"\n");
		content.append("        onTertiaryContainer: \"#D4EABB\"\n");
		content.append("        error: \"#FFB4AB\"\n");
		content.append("        errorContainer: \"#93000A\"\n");
		content.append("        onError: \"#690005\"\n");
		content.append("        onErrorContainer: \"#FFDAD6\"\n");
		content.append("        background: \"#1F1B16\"\n");
		content.append("        onBackground: \"#EAE1D9\"\n");
		content.append("        surface: \"#1F1B16\"\n");
		content.append("        onSurface: \"#EAE1D9\"\n");
		content.append("        surfaceVariant: \"#4F4539\"\n");
		content.append("        onSurfaceVariant: \"#D3C4B4\"\n");
		content.append("        outline: \"#9C8F80\"\n");
		content.append("        inverseOnSurface: \"#1F1B16\"\n");
		content.append("        inverseSurface: \"#EAE1D9\"\n");
		content.append("        inversePrimary: \"#825500\"\n");
		content.append("        surfaceTint: \"#FFB951\"\n");
		content.append("        outlineVariant: \"#4F4539\"\n");
		content.append("        scrim: \"#000000\"\n");
		content.append("    }\n\n");

		return content.toString();
	}

	public static String writeLightTheme() {
		StringBuilder content = new StringBuilder("\n");
		content.append("    Theme {\n");
		content.append("        primary: \"#825500\"\n");
		content.append("        onPrimary: \"#FFFFFF\"\n");
		content.append("        primaryContainer: \"#FFDDB3\"\n");
		content.append("        onPrimaryContainer: \"#291800\"\n");
		content.append("        secondary: \"#6F5B40\"\n");
		content.append("        onSecondary: \"#FFFFFF\"\n");
		content.append("        secondaryContainer: \"#FBDEBC\"\n");
		content.append("        onSecondaryContainer: \"#271904\"\n");
		content.append("        tertiary: \"#51643F\"\n");
		content.append("        onTertiary: \"#FFFFFF\"\n");
		content.append("        tertiaryContainer: \"#D4EABB\"\n");
		content.append("        onTertiaryContainer: \"#102004\"\n");
		content.append("        error: \"#BA1A1A\"\n");
		content.append("        errorContainer: \"#FFDAD6\"\n");
		content.append("        onError: \"#FFFFFF\"\n");
		content.append("        onErrorContainer: \"#410002\"\n");
		content.append("        background: \"#FFFBFF\"\n");
		content.append("        onBackground: \"#1F1B16\"\n");
		content.append("        surface: \"#FFFBFF\"\n");
		content.append("        onSurface: \"#1F1B16\"\n");
		content.append("        surfaceVariant: \"#F0E0CF\"\n");
		content.append("        onSurfaceVariant: \"#4F4539\"\n");
		content.append("        outline: \"#817567\"\n");
		content.append("        inverseOnSurface: \"#F9EFE7\"\n");
		content.append("        inverseSurface: \"#34302A\"\n");
		content.append("        inversePrimary: \"#FFB951\"\n");
		content.append("        surfaceTint: \"#825500\"\n");
		content.append("        outlineVariant: \"#D3C4B4\"\n");
		content.append("        scrim: \"#000000\"\n");
		content.append("    }\n\n");

		return content.toString();
	}

	public static void loadApp() {
		File appFile = new File(getActiveProject() + "/app.sml");
		try {
			String uiSml = Files.readString(appFile.toPath());
			Pair<App, ?> result = parseApp(uiSml);
			ProjectManager.app = (App)result.getFirst();
			System.out.println("App loaded");
		} catch (Exception e) {
			System.out.println("Error parsing app.sml: " + e.getMessage());
		}
	}

	public static void loadBook() {
		File bookFile = new File(getActiveProject() + "/ebook.sml");
		try {
			String uiSml = Files.readString(bookFile.toPath());
			Pair<Ebook, ?> result = parseBook(uiSml);
			ProjectManager.book = (Ebook)result.getFirst();
			System.out.println("Book loaded");
		} catch (Exception e) {
			System.out.println("Error parsing book.sml: " + e.getMessage());
		}
	}

	private static Preferences getProjectsState() {
		return BookDesignerApp.getState().node(KEY_PROJECTS);
	}

	private static Pair<App, ?> parseApp(String uiSml) {
		return new Pair<>(new App(), null);
	}

	private static Pair<Ebook, ?> parseBook(String uiSml) {
		return new Pair<>(new Ebook(), null);
	}

	public static class Pair<F, S> {
		private final F first;
		private final S second;

		public Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}

		public F getFirst() {
			return first;
		}

		public S getSecond() {
			return second;
		}
	}

/*
	fun fileExists(path: String): Boolean {
	return File(path).exists()
}

	fun deleteFile(path: String) {
		File(path).delete()
	}

	fun createPage(path: String, title: String) {
		val file = File(path)
		file.createNewFile()
		file.writeText("Page {\n    title:\"$title\"\n}")
	}

	fun createPart(path: String) {
		val file = File(path)
		file.createNewFile()
		file.writeText("# Header\nLorem ipsum dolor\n")
	}

	fun renameFile(pathBefore: String, pathAfter: String) {
		File(pathBefore).renameTo(File(pathAfter))
	}

	fun copyAssetFile(path: String, target: String) {
		val sourceFile = File(path)
		val targetFile = File(target)
		sourceFile.copyTo(targetFile, overwrite = true)
	}
*/
}
