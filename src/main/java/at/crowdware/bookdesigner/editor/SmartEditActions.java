/*
 * Copyright (c) 2016 Karl Tauber <karl at jformdesigner dot com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright
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

package at.crowdware.bookdesigner.editor;

import javafx.scene.control.ContextMenu;
import at.crowdware.bookdesigner.Messages;
import at.crowdware.bookdesigner.util.Action;
import at.crowdware.bookdesigner.util.ActionUtils;

/**
 * Smart Markdown text edit actions.
 *
 * @author Karl Tauber
 */
class SmartEditActions
{
	static void initContextMenu(MarkdownEditorPane editor, ContextMenu contextMenu, int characterIndex) {
		Action cutAction = new Action(Messages.get("MainWindow.editCutAction"), "Shortcut+X", null,
				e -> editor.cut());
		Action copyAction = new Action(Messages.get("MainWindow.editCopyAction"), "Shortcut+C", null,
				e -> editor.copy());
		Action pasteAction = new Action(Messages.get("MainWindow.editPasteAction"), "Shortcut+V", null,
				e -> editor.paste());

		contextMenu.getItems().addAll(ActionUtils.createMenuItems(
				cutAction,
				copyAction,
				pasteAction));
	}
}
