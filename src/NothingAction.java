
public class NothingAction extends AbstractAction {
	
	Action action = Action.DO_NOTHING;

	@Override
	public String toString() {
		return "NothingAction [action=" + action + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@SuppressWarnings("unused")
	@Override
	public int compareTo(AbstractAction arg0) {
		try {
			NothingAction o = (NothingAction) arg0;
			return 0;
		} catch (ClassCastException e) {
			try {
				SpectrumAction o = (SpectrumAction) arg0;
				return -2;
			} catch (ClassCastException ex) {
				return -1;
			}
		}
	}
	
}
