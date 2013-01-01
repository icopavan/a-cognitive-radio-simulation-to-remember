
public abstract class AbstractAction {

	public static Action determineAction(AbstractAction action) {
		String className = action.getClass().getSimpleName();
		if (className.equals("PowerAction")) {
			return Action.JUMP_POWER;
		} else if (className.equals("SpectrumAction")) {
			return Action.JUMP_SPECTRUM;
		} else {
			return Action.DO_NOTHING;
		}
	}
	
	public static boolean compareActions(AbstractAction action1, AbstractAction action2) {
		if (determineAction(action1) == determineAction(action2)) {
			Action actionTypes = determineAction(action1);
			if (actionTypes == Action.DO_NOTHING) {
				return false;
			} else {
				if (actionTypes == Action.JUMP_POWER) {
					PowerAction powerAction1 = (PowerAction) action1;
					PowerAction powerAction2 = (PowerAction) action2;
					return powerAction1.newPower == powerAction2.newPower;
				} else {
					SpectrumAction spectrumAction1 = (SpectrumAction) action1;
					SpectrumAction spectrumAction2 = (SpectrumAction) action2;
					return spectrumAction1.newSpectrum == spectrumAction2.newSpectrum;
				}
			}
		} else {
			return false;
		}
	}
	
}
