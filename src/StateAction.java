
public class StateAction implements Comparable<StateAction> {

	public State state;
	
	public AbstractAction action;

	public StateAction(State aState, AbstractAction anAction) {
		this.state = aState;
		this.action = anAction;
	}
	
	@Override
	public String toString() {
		return "StateAction [state=" + state + ", action=" + action + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (this.compareTo((StateAction) obj) == 0)
			return true;
		StateAction other = (StateAction) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public int compareTo(StateAction arg0) {
		if (!state.equals(arg0.state)) {
			return state.compareTo(arg0.state);
		} else {
			return action.compareTo(arg0.action);
		}
	}
	
}
