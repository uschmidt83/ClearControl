package clearcontrol.microscope.lightsheet.adaptation.modules;

import java.util.function.Function;

import clearcontrol.microscope.lightsheet.adaptation.Adaptator;

public interface AdaptationModuleInterface extends
																					Function<Void, Boolean>
{

	void setAdaptator(Adaptator pLSMAdaptator);

	Adaptator getAdaptator();

	void setPriority(int pPriority);

	int getPriority();

	@Override
	Boolean apply(Void pVoid);

	boolean isReady();

	int getNumberOfSteps();

	void reset();

}