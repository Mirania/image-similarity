package oracle;

import java.awt.Color;

public class Image {

	private String name;
	private Color[][] signature;
	
	public Image(String n, Color[][] sig) {
		name = n;
		signature = sig;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Color[][] getSignature() {
		return signature;
	}

	public void setSignature(Color[][] signature) {
		this.signature = signature;
	}
	
	
}
