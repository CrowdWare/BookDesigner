package at.crowdware.bookdesigner.model

import io.qt.core.QObject
import io.qt.qml.QQmlApplicationEngine
import io.qt.qml.QQmlComponent
import io.qt.qml.QQmlEngine


data class Book(
	val name: String = "",
	val author: String = "",
	val pages: Int = 0
) : QObject()

fun parseQMLString(qmlString: String): QObject? {
	val engine = QQmlApplicationEngine()
	val component = QQmlComponent(engine)
	component.setData(qmlString, "")

	if (component.isReady) {
		return component.create()
	} else {
		println("Fehler beim Parsen des QML-Strings: ${component.errorString()}")
		return null
	}
}

fun testQml() {
	println("testQml")
	val qml = "import com.example.models 1.0\n\nBook { name: \"title\" author: \"myself\" pages: 123 }"
	val book = parseQMLString(qml)
	if (book is Book) {
		println(book.name)
	} else {
		println("Error parsing book: $book")
	}
}

