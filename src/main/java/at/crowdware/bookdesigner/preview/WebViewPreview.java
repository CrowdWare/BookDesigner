/*
 * Copyright (c) 2015 Karl Tauber <karl at jformdesigner dot com>
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

package at.crowdware.bookdesigner.preview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.BiConsumer;
import javafx.concurrent.Worker.State;
import javafx.scene.control.IndexRange;
import javafx.scene.web.WebView;
import at.crowdware.bookdesigner.options.Options;
import at.crowdware.bookdesigner.preview.MarkdownPreviewPane.PreviewContext;
import at.crowdware.bookdesigner.preview.MarkdownPreviewPane.Renderer;
import at.crowdware.bookdesigner.util.Utils;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.Visitor;

/**
 * WebView preview.
 *
 * @author Karl Tauber
 */
class WebViewPreview
	implements MarkdownPreviewPane.Preview
{
	private static final HashMap<String, String> prismLangDependenciesMap = new HashMap<>();

	private WebView webView;
	private final ArrayList<Runnable> runWhenLoadedList = new ArrayList<>();
	private int lastScrollX;
	private int lastScrollY;
	private IndexRange lastEditorSelection;

	WebViewPreview() {
	}

	private void createNodes() {
		webView = new WebView();
		webView.setFocusTraversable(false);

		// disable WebView default drag and drop handler to allow dropping markdown files
		webView.setOnDragEntered(null);
		webView.setOnDragExited(null);
		webView.setOnDragOver(null);
		webView.setOnDragDropped(null);
		webView.setOnDragDetected(null);
		webView.setOnDragDone(null);

		webView.getEngine().getLoadWorker().stateProperty().addListener((ob,o,n) -> {
			if (n == State.SUCCEEDED && !runWhenLoadedList.isEmpty()) {
				ArrayList<Runnable> runnables = new ArrayList<>(runWhenLoadedList);
				runWhenLoadedList.clear();

				for (Runnable runnable : runnables)
					runnable.run();
			}
		});
	}

	private void runWhenLoaded(Runnable runnable) {
		if (webView.getEngine().getLoadWorker().isRunning())
			runWhenLoadedList.add(runnable);
		else
			runnable.run();
	}

	@Override
	public javafx.scene.Node getNode() {
		if (webView == null)
			createNodes();
		return webView;
	}

	@Override
	public void update(PreviewContext context, Renderer renderer) {
		if (!webView.getEngine().getLoadWorker().isRunning()) {
			// get window.scrollX and window.scrollY from web engine,
			// but only if no worker is running (in this case the result would be zero)
			Object scrollXobj = webView.getEngine().executeScript("window.scrollX");
			Object scrollYobj = webView.getEngine().executeScript("window.scrollY");
			lastScrollX = (scrollXobj instanceof Number) ? ((Number)scrollXobj).intValue() : 0;
			lastScrollY = (scrollYobj instanceof Number) ? ((Number)scrollYobj).intValue() : 0;
		}
		lastEditorSelection = context.getEditorSelection();

		Path path = context.getPath();
		String base = (path != null)
				? ("<base href=\"" + path.getParent().toUri().toString() + "\">\n")
				: "";
		String scrollScript = (lastScrollX > 0 || lastScrollY > 0)
				? ("  onload='window.scrollTo("+lastScrollX+", "+lastScrollY+");'")
				: "";

		webView.getEngine().loadContent(
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "<head>\n"
			+ "<link rel=\"stylesheet\" href=\"" + getClass().getResource("markdownpad-github.css") + "\">\n"
			+ "<style>\n"
			+ Utils.defaultIfEmpty(Options.getAdditionalCSS(), "") + "\n"
			+ ".mwfx-editor-selection {\n"
			+ "  border-right: 5px solid #f47806;\n"
			+ "  margin-right: -5px;\n"
			+ "  background-color: rgb(253, 247, 241);\n"
			+ "}\n"
			+ "</style>\n"
			+ "<script src=\"" + getClass().getResource("preview.js") + "\"></script>\n"
			+ prismSyntaxHighlighting(context.getMarkdownAST())
			+ base
			+ "</head>\n"
			+ "<body" + scrollScript + ">\n"
			+ renderer.getHtml(false)
			+ "<script>" + highlightNodesAt(lastEditorSelection) + "</script>\n"
			+ "</body>\n"
			+ "</html>");
	}

	@Override
	public void scrollY(PreviewContext context, double value) {
		runWhenLoaded(() -> {
			webView.getEngine().executeScript("preview.scrollTo(" + value + ");");
		});
	}

	@Override
	public void editorSelectionChanged(PreviewContext context, IndexRange range) {
		if (range.equals(lastEditorSelection))
			return;
		lastEditorSelection = range;

		runWhenLoaded(() -> {
			webView.getEngine().executeScript(highlightNodesAt(range));
		});
	}

	private String highlightNodesAt(IndexRange range) {
		return "preview.highlightNodesAt(" + range.getEnd() + ")";
	}

	private String prismSyntaxHighlighting(Node astRoot) {
		initPrismLangDependencies();

		// check whether markdown contains fenced code blocks and remember languages
		ArrayList<String> languages = new ArrayList<>();
		NodeVisitor visitor = new NodeVisitor(Collections.emptyList()) {
			@Override
			protected void processNode(Node node, boolean withChildren, BiConsumer<Node, Visitor<Node>> processor) {
				if (node instanceof FencedCodeBlock) {
					String language = ((FencedCodeBlock)node).getInfo().toString();
					if (language.contains(language))
						languages.add(language);

					// dependencies
					while ((language = prismLangDependenciesMap.get(language)) != null) {
						if (language.contains(language))
							languages.add(0, language); // dependencies must be loaded first
					}
				} else
					if(node != null)
						visitChildren(node);
			}
		};
		visitor.visit(astRoot);

		if (languages.isEmpty())
			return "";

		// build HTML (only load used languages)
		// Note: not using Prism Autoloader plugin because it lazy loads/highlights, which causes flicker
		//       during fast typing; it also does not work with "alias" languages (e.g. js, html, xml, svg, ...)
		StringBuilder buf = new StringBuilder();
		buf.append("<link rel=\"stylesheet\" href=\"").append(getClass().getResource("prism/prism.css")).append("\">\n");
		buf.append("<script src=\"").append(getClass().getResource("prism/prism-core.min.js")).append("\"></script>\n");
		for (String language : languages) {
			URL url = getClass().getResource("prism/components/prism-"+language+".min.js");
			if (url != null)
				buf.append("<script src=\"").append(url).append("\"></script>\n");
		}
		return buf.toString();
	}

	/**
	 * load and parse prism/lang_dependencies.txt
	 */
	private static void initPrismLangDependencies() {
		if (!prismLangDependenciesMap.isEmpty())
			return;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				WebViewPreview.class.getResourceAsStream("prism/lang_dependencies.txt"))))
		{
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("{"))
					continue;

				line = line.replaceAll("\\[([^\\]]+)\\]", "[not supported]");
				line = trimDelim(line, "{", "}");
				for (String str : line.split(",")) {
					String[] parts = str.split(":");
					if (parts[1].startsWith("["))
						continue; // not supported

					String key = trimDelim(parts[0], "\"", "\"");
					String value = trimDelim(parts[1], "\"", "\"");
					prismLangDependenciesMap.put(key, value);
				}
			}
		} catch (IOException e) {
			// ignore
		}
	}

	private static String trimDelim(String str, String leadingDelim, String trailingDelim) {
		str = str.trim();
		if (!str.startsWith(leadingDelim) || !str.endsWith(trailingDelim))
			throw new IllegalArgumentException(str);
		return str.substring(leadingDelim.length(), str.length() - trailingDelim.length());
	}
}
