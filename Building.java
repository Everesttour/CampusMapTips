// 김주환
package com.skhu.tips.model.entity;

public class Building {

	private int id;
	private double xLocation;
	private double yLocation;
	private String name;
	private String description;
	private String rooms;
	private int currentX;
	private int currentY;

	public Building() {
	}

	public Building(int id, double xLocation, double yLocation, String name, String description, String rooms) {
		super();
		this.id = id;
		this.xLocation = xLocation;
		this.yLocation = yLocation;
		this.name = name;
		this.description = description;
		this.rooms = rooms;
	}

	public int getId() {
		return id;
	}

	public double getxLocation() {
		return xLocation;
	}

	public double getyLocation() {
		return yLocation;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getRooms() {
		return rooms;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setxLocation(double xLocation) {
		this.xLocation = xLocation;
	}

	public void setyLocation(double yLocation) {
		this.yLocation = yLocation;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setRooms(String rooms) {
		this.rooms = rooms;
	}

	public int getCurrentX() {
		return currentX;
	}
	
	public void setCurrentX(int currentX) {
		this.currentX = currentX;
	}
	
	public int getCurrentY() {
		return currentY;
	}

	public void setCurrentY(int currentY) {
		this.currentY = currentY;
	}
}