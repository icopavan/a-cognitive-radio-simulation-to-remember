import java.util.ArrayList;
import java.util.List;


public class Spectrum implements Comparable<Spectrum> {

	public List<CognitiveRadio> occupyingSecondaryUsers;
	
	public List<CognitiveRadio> occupyingAgents;
	
	public double frequency;
	
	public boolean containsPrimaryUser;
	
	public Spectrum(double frequency) {
		this.frequency = frequency;
		occupyingSecondaryUsers = new ArrayList<CognitiveRadio>();
		occupyingAgents = new ArrayList<CognitiveRadio>();
	}
	
	@Override
	public String toString() {
		return frequency + " MHz";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + frequency);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (this.compareTo((Spectrum) obj) == 0)
			return true;
		Spectrum other = (Spectrum) obj;
		if (frequency != other.frequency)
			return false;
		return true;
	}

	@Override
	public int compareTo(Spectrum o) {
		return (int) (this.frequency - o.frequency); 
	}
	
}
