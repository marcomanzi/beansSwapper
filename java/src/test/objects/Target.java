package test.objects;

import java.util.List;

public class Target {
	private String targetName;
	private String targetSurname;
	private List<SubTarget> subTargets;

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getTargetSurname() {
		return targetSurname;
	}

	public void setTargetSurname(String targetSurname) {
		this.targetSurname = targetSurname;
	}

	public List<SubTarget> getSubTargets() {
		return subTargets;
	}

	public void setSubTargets(List<SubTarget> subTargets) {
		this.subTargets = subTargets;
	}
	
	public static void main(String[] args) {
		Target test = new Target();
		
	}

}
