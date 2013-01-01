

public class State implements Comparable<State> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((spectrum == null) ? 0 : spectrum.hashCode());
		result = prime * result
				+ ((transmission == null) ? 0 : transmission.hashCode());
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
		if (transmission == null) {
			if (other.transmission != null)
				return false;
		} else if (!transmission.equals(other.transmission))
			return false;
		return true;
	}

	public Spectrum spectrum;
	public Transmission transmission;
	
	@Override
	public String toString() {
		return "State [spectrum=" + spectrum + "]";
	}
	
	public State(Spectrum spectrum, Transmission aTransmission) {
		this.spectrum = spectrum;
		transmission = aTransmission;
	}

	@Override
	public int compareTo(State arg0) {
		return spectrum.compareTo(arg0.spectrum);
	}

}
