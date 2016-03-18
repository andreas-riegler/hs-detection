package hatespeech.detection.model;

public enum PostType {
	NEGATIVE(0),
	POSITIVE(1);
	
	private int value;
	private PostType(int value)
	{
		this.value=value;
	}
	public int getValue()
	{
		return value;
	}
}
