package at.crowdware.bookdesigner.model;

public class ProjectData {
	private final String name;
	private final String path;

	public ProjectData(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}
}
