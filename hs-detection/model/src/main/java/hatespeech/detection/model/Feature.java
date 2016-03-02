package hatespeech.detection.model;

public class Feature {

	private String nGram = "";

    public String getnGram() {
		return nGram;
	}

	public void setnGram(String nGram) {
		this.nGram = nGram;
	}

	@Override
    public String toString(){
        return nGram.toString();
    }
}
