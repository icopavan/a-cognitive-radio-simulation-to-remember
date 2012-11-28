
public class State implements Comparable<State> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(probabilityForTransmission);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (Double.doubleToLongBits(probabilityForTransmission) != Double
				.doubleToLongBits(other.probabilityForTransmission))
			return false;
		if (spectrum == null) {
			if (other.spectrum != null)
				return false;
		} else if (!spectrum.equals(other.spectrum))
			return false;
		return true;
	}

	public Spectrum spectrum;
	
	public double probabilityForTransmission;
	
	public State() {
		
	}
	
	@Override
	public String toString() {
		return "State [spectrum=" + spectrum + ", probabilityForTransmission="
				+ probabilityForTransmission + "]";
	}
	
	public State(Spectrum spectrum, double transmissionProbability) {
		this.spectrum = spectrum;
		probabilityForTransmission = transmissionProbability;
	}

	@Override
	public int compareTo(State arg0) {
		if (probabilityForTransmission != arg0.probabilityForTransmission) {
			return (int) Math.ceil(probabilityForTransmission
					- arg0.probabilityForTransmission);
		} else {
			return spectrum.compareTo(arg0.spectrum);
		}
	}

}
