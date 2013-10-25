package test.objects;

import java.util.List;

public class Source {
	private String sourceName;
	private String sourceSurname;
	private List<SubSource> subSources;
	
	private ListContainer subListContainer;

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceSurname() {
		return sourceSurname;
	}

	public void setSourceSurname(String sourceSurname) {
		this.sourceSurname = sourceSurname;
	}

	public List<SubSource> getSubSources() {
		return subSources;
	}

	public void setSubSources(List<SubSource> subSources) {
		this.subSources = subSources;
	}

	public ListContainer getSubListContainer() {
		return subListContainer;
	}

	public void setSubListContainer(ListContainer subListContainer) {
		this.subListContainer = subListContainer;
	}
	
}
