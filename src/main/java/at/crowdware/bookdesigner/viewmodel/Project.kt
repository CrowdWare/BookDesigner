/*
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeDesigner.
 *
 *  NoCodeDesigner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeDesigner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeDesigner.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.bookdesigner.viewmodel

import at.crowdware.bookdesigner.model.*
import at.crowdware.bookdesigner.utils.parseApp
import at.crowdware.bookdesigner.utils.parseBook
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/*
fun getNodeType(path: String): NodeType {
	val file = File(path)
	return when {
		file.isDirectory -> NodeType.DIRECTORY
		else -> extensionToNodeType[file.extension.lowercase()] ?: NodeType.OTHER
	}
}

fun loadFileContent(path: String, uuid: String, pid: String): String {
	val file = File(path)
	return try {
		file.readText()
	} catch (e: IOException) {
		throw IOException("Error reading file: ${e.message}", e)
	}
}

fun saveFileContent(path: String, uuid: String, pid: String, content: String) {
	val file = File(path)
	try {
		file.writeText(content)
		file.setLastModified(System.currentTimeMillis())
	} catch (e: IOException) {
		throw IOException("Error writing to file: ${e.message}", e)
	}
}
*/
//class DesktopProjectState : ProjectState() {
	/*
	fun loadProjectFiles(path: String, uuid: String, pid: String) {
		val file = File(path)

		fun getNodeType(file: File): NodeType {
			return if (file.isDirectory) {
				NodeType.DIRECTORY
			} else {
				val extension = file.extension.lowercase()
				extensionToNodeType[extension] ?: NodeType.OTHER
			}
		}

		fun mapFileToTreeNode(file: File): TreeNode {
			val allowedFolderNames = listOf("images", "videos", "sounds", "pages", "parts", "models","pages-en", "pages-de", "pages-es", "pages-pt", "pages-fr", "pages-eo")
			val nodeType = getNodeType(file)
			val children = if (file.isDirectory) {
				file.listFiles()
					?.filter { it.name != ".DS_Store" }
					?.flatMap {
						if (it.isDirectory && allowedFolderNames.contains(it.name)) {
							it.listFiles()?.filter { file -> file.name != ".DS_Store" }?.map { mapFileToTreeNode(it) } ?: emptyList()
						} else if (!it.isDirectory) {
							listOf(mapFileToTreeNode(it))
						} else {
							emptyList()
						}
					} ?: emptyList()
			} else {
				emptyList()
			}
			val statefulChildren = ArrayList<TreeNode>(children)

			val node = TreeNode(
				title = file.name,
				path = file.path,
				type = nodeType,
				children = statefulChildren
			)
			if (node.title == "pages" || node.title == "pages-en" || node.title == "pages-es" || node.title == "pages-pt" || node.title == "pages-fr" || node.title == "pages-eo") {
				pageNode = node
			} else if (node.title == "images") {
				imagesNode = node
			} else if (node.title == "videos") {
				videosNode = node
			} else if (node.title == "sounds") {
				soundsNode = node
			} else if (node.title == "parts") {
				partsNode = node
			} else if (node.title == "models") {
				modelsNode = node
			} else if (node.title == "textures") {
				texturesNode = node
			}
			return node
		}

		val nodes = file.listFiles()
			// Python 3 server.py runs the webserver for NoCodeBrowser testing
			?.filter {
				it.name != ".DS_Store" &&
					!it.name.endsWith(".py") &&
					(it.isDirectory && it.name in listOf("pages", "parts", "images", "sounds", "videos", "models", "textures", "pages-en", "pages-de", "pages-es", "pages-pt", "pages-fr", "pages-eo" )) ||
					(it.isFile && it.name in listOf("app.sml", "book.sml"))
			}
			?.map { mapFileToTreeNode(it) }
			?: emptyList()

		val sortedNodes = nodes.sortedWith(
			compareBy<TreeNode> { it.type != NodeType.DIRECTORY }
				.thenBy { it.title }
		)

		for (node in sortedNodes) {
			val sortedChildren = node.children.sortedWith(
				compareBy<TreeNode> { it.type != NodeType.DIRECTORY }
					.thenBy { it.title }
			)

			//node.children.clear()
			//node.children.addAll(sortedChildren)
			node.children = sortedChildren
		}

		treeData = sortedNodes.toList()
		folder = path

		// app.sml load and parse
		val appFile =  File("$folder/app.sml")
		if (appFile.exists()) {
			loadApp()
			LoadFile("$folder/pages/home.sml")
		}

		// book.sml load and parse
		val bookFile = File("$folder/book.sml")
		if(bookFile.exists()) {
			loadBook()
			LoadFile("$folder/parts/home.md")
		}
	}

	fun loadApp() {
		val appFile = File("$folder/app.sml")
		try {
			val uiSml = appFile.readText()
			val result = parseApp(uiSml)
			app = result.first
		} catch (e: Exception) {
			println("Error parsing app.sml: ${e.message}")
		}
	}

	fun loadBook() {
		val bookFile = File("$folder/book.sml")
		try {
			val uiSml = bookFile.readText()
			val result = parseBook(uiSml)
			book = result.first
		} catch (e: Exception) {
			println("Error parsing book.sml: ${e.message}")
		}
	}*/
//}

/*
fun writeDarkTheme(): String {
	var content = "\n"
	content += "    Theme {\n"
	content += "        primary: \"#FFB951\"\n"
	content += "        onPrimary: \"#452B00\"\n"
	content += "        primaryContainer: \"#633F00\"\n"
	content += "        onPrimaryContainer: \"#FFDDB3\"\n"
	content += "        secondary: \"#DDC2A1\"\n"
	content += "        onSecondary: \"#3E2D16\"\n"
	content += "        secondaryContainer: \"#56442A\"\n"
	content += "        onSecondaryContainer: \"#FBDEBC\"\n"
	content += "        tertiary: \"#B8CEA1\"\n"
	content += "        onTertiary: \"#243515\"\n"
	content += "        tertiaryContainer: \"#3A4C2A\"\n"
	content += "        onTertiaryContainer: \"#D4EABB\"\n"
	content += "        error: \"#FFB4AB\"\n"
	content += "        errorContainer: \"#93000A\"\n"
	content += "        onError: \"#690005\"\n"
	content += "        onErrorContainer: \"#FFDAD6\"\n"
	content += "        background: \"#1F1B16\"\n"
	content += "        onBackground: \"#EAE1D9\"\n"
	content += "        surface: \"#1F1B16\"\n"
	content += "        onSurface: \"#EAE1D9\"\n"
	content += "        surfaceVariant: \"#4F4539\"\n"
	content += "        onSurfaceVariant: \"#D3C4B4\"\n"
	content += "        outline: \"#9C8F80\"\n"
	content += "        inverseOnSurface: \"#1F1B16\"\n"
	content += "        inverseSurface: \"#EAE1D9\"\n"
	content += "        inversePrimary: \"#825500\"\n"
	content += "        surfaceTint: \"#FFB951\"\n"
	content += "        outlineVariant: \"#4F4539\"\n"
	content += "        scrim: \"#000000\"\n"
	content += "    }\n\n"
	return content
}

fun writeLightTheme(): String {
	var content = "\n"
	content += "    Theme {\n"
	content += "        primary: \"#825500\"\n"
	content += "        onPrimary: \"#FFFFFF\"\n"
	content += "        primaryContainer: \"#FFDDB3\"\n"
	content += "        onPrimaryContainer: \"#291800\"\n"
	content += "        secondary: \"#6F5B40\"\n"
	content += "        onSecondary: \"#FFFFFF\"\n"
	content += "        secondaryContainer: \"#FBDEBC\"\n"
	content += "        onSecondaryContainer: \"#271904\"\n"
	content += "        tertiary: \"#51643F\"\n"
	content += "        onTertiary: \"#FFFFFF\"\n"
	content += "        tertiaryContainer: \"#D4EABB\"\n"
	content += "        onTertiaryContainer: \"#102004\"\n"
	content += "        error: \"#BA1A1A\"\n"
	content += "        errorContainer: \"#FFDAD6\"\n"
	content += "        onError: \"#FFFFFF\"\n"
	content += "        onErrorContainer: \"#410002\"\n"
	content += "        background: \"#FFFBFF\"\n"
	content += "        onBackground: \"#1F1B16\"\n"
	content += "        surface: \"#FFFBFF\"\n"
	content += "        onSurface: \"#1F1B16\"\n"
	content += "        surfaceVariant: \"#F0E0CF\"\n"
	content += "        onSurfaceVariant: \"#4F4539\"\n"
	content += "        outline: \"#817567\"\n"
	content += "        inverseOnSurface: \"#F9EFE7\"\n"
	content += "        inverseSurface: \"#34302A\"\n"
	content += "        inversePrimary: \"#FFB951\"\n"
	content += "        surfaceTint: \"#825500\"\n"
	content += "        utlineVariant: \"#D3C4B4\"\n"
	content += "        scrim: \"#000000\"\n"
	content += "    }\n\n"
	return content
}
*/


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
