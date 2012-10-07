
public class State implements Comparable<State> {

	public Spectrum spectrum;
	
	public double power;

	public State() {
		
	}
	
	@Override
	public String toString() {
		return "State [spectrum=" + spectrum + ", power=" + power + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + power);
		result =
				prime * result + ((spectrum == null) ? 0 : spectrum.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.compareTo((State) obj) == 0)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (power != other.power)
			return false;
		if (spectrum == null) {
			if (other.spectrum != null)
				return false;
		} else if (!spectrum.equals(other.spectrum))
			return false;
		return true;
	}

	public State(Spectrum spectrum, double power) {
		this.spectrum = spectrum;
		this.power = power;
	}

	@Override
	public int compareTo(State o) {
		if (!spectrum.equals(o)) {
			return spectrum.compareTo(o.spectrum);
		} else {
			return (int) (power - o.power);
		}
	}
}
