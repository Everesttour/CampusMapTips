// 김주환
package com.skhu.tips.model.entity;

public class Facility {

	private int id;
    private double xLocation;
    private double yLocation;
    private int floor;
    private String name;
    private String buildingName;
    private String overview;
    private String description;
    private String notice;
    private String operatingHours;
    private String[] tips;
    private int priority;
    private int currentX;
    private int currentY;

    public Facility() {}

	public Facility(int id, double xLocation, double yLocation, int floor, String name, String buildingName,
			String overview, String description, String notice, String operatingHours, String[] tips, int priority, int currentX, int currentY) {
		super();
		this.id = id;
		this.xLocation = xLocation;
		this.yLocation = yLocation;
		this.floor = floor;
		this.name = name;
		this.buildingName = buildingName;
		this.overview = overview;
		this.description = description;
		this.notice = notice;
		this.operatingHours = operatingHours;
		this.tips = tips;
		this.priority = priority;
		this.currentX = currentX;
		this.currentY = currentY;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getxLocation() {
		return xLocation;
	}

	public void setxLocation(double xLocation) {
		this.xLocation = xLocation;
	}

	public double getyLocation() {
		return yLocation;
	}

	public void setyLocation(double yLocation) {
		this.yLocation = yLocation;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBuildingName() {
		return buildingName;
	}

	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getOperatingHours() {
		return operatingHours;
	}

	public void setOperatingHours(String operatingHours) {
		this.operatingHours = operatingHours;
	}

	public String[] getTips() {
		return tips;
	}

	public void setTips(String[] tips) {
		this.tips = tips;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
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
