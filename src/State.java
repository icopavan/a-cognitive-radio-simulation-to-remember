
public class State implements Comparable<State> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((spectrum == null) ? 0 : spectrum.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (spectrum == null) {
			if (other.spectrum != null)
				return false;
		} else if (!spectrum.equals(other.spectrum))
			return false;
		return true;
	}

	public Spectrum spectrum;
	
	public double transmissionProbability;
	
	public State() {
		
	}
	
	@Override
	public String toString() {
		return "State [spectrum=" + spectrum + "]";
	}
	
	public State(Spectrum spectrum, double transmissionProbability) {
		this.spectrum = spectrum;
		this.transmissionProbability = transmissionProbability;
	}

	@Override
	public int compareTo(State arg0) {
		return spectrum.compareTo(arg0.spectrum);
	}

}
