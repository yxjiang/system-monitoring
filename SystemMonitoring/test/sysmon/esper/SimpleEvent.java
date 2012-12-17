package sysmon.esper;

public class SimpleEvent {
	
	private String itemName;
	private double price;
	
	public SimpleEvent(String itemName, double price) {
		super();
		this.itemName = itemName;
		this.price = price;
	}
	
	public String getItemName() {
		return itemName;
	}
	
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
}
