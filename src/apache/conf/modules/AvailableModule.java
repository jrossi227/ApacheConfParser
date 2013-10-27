package apache.conf.modules;

public class AvailableModule extends Module {

	private String filename;
	
	public AvailableModule(String name, String filename) {
		super(name, Type.AVAILABLE);
		
		this.filename=filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}

