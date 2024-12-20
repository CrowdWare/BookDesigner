/*
 * Copyright (c) 2016 Karl Tauber <karl at jformdesigner dot com>
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

package at.crowdware.bookdesigner.util;

import java.util.prefs.Preferences;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A string array property that loads/saves its value from/to preferences.
 *
 * @author Karl Tauber
 */
public class PrefsStringsProperty
	extends SimpleObjectProperty<String[]>
{
	private Preferences prefs;
	private String key;

	public PrefsStringsProperty() {
		// make sure that property is not null when used in JFormDesigner
		set( new String[0] );
	}

	public PrefsStringsProperty(Preferences prefs, String key) {
		init(prefs, key);
	}

	public void init(Preferences prefs, String key) {
		this.key = key;

		setPreferences(prefs);
		addListener((ob, o, n) -> {
			Utils.putPrefsStrings(this.prefs, this.key, get());
		});
	}

	public void setPreferences(Preferences prefs) {
		this.prefs = prefs;

		set(Utils.getPrefsStrings(prefs, key));
	}
}
