package hatespeech.detection.model;

public class SpellCheckedMessage {

	private double weightedMistakes;
	private int mistakes, exclMarkMistakes;
	
	public SpellCheckedMessage(double weightedMistakes, int mistakes, int exclMarkMistakes)
	{
		this.weightedMistakes=weightedMistakes;
		this.mistakes=mistakes;
		this.exclMarkMistakes=exclMarkMistakes;
	}
	
	public double getWeightedMistakes() {
		return weightedMistakes;
	}
	public void setWeightedMistakes(double weightedMistakes) {
		this.weightedMistakes = weightedMistakes;
	}
	public int getMistakes() {
		return mistakes;
	}
	public void setMistakes(int mistakes) {
		this.mistakes = mistakes;
	}
	public int getExclMarkMistakes() {
		return exclMarkMistakes;
	}
	public void setExclMarkMistakes(int exclMarkMistakes) {
		this.exclMarkMistakes = exclMarkMistakes;
	}
	
	
}
