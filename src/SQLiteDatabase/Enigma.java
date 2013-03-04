package SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class Enigma {
	private int id;
	private List<String> clueList;
	private String enigmaSolution;

	public Enigma() {
		this.clueList = new ArrayList<String>();
		this.enigmaSolution = "solution";
	}

	public Enigma(List<String> clueList, String enigmaSolution) {
		if (clueList != null && clueList.size() > 0)
			this.clueList = clueList;

		if (enigmaSolution != null && enigmaSolution.isEmpty() == false)
			this.enigmaSolution = enigmaSolution;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<String> getClueList() {
		return clueList;
	}

	public void setClueList(ArrayList<String> clueList) {
		this.clueList = clueList;
	}

	public String getEnigmaSolution() {
		return enigmaSolution;
	}

	public void setEnigmaSolution(String enigmaSolution) {
		this.enigmaSolution = enigmaSolution;
	}

	public String toString() {
		String to_str = "";

		to_str += "Solution=" + this.enigmaSolution + "\n";
		to_str += "Clues\n{\n";

		for (String str : this.clueList)
			to_str += "\t" + str + "\n";

		to_str += "}";

		return to_str;
	}
}
