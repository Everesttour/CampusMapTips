// 김준
package com.skhu.tips.view.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.skhu.tips.controller.MapController;
import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;

/**
 * @class MapPanel
 * @brief 지도 표시 + 줌/이동 + 아이콘 그리기 + 클릭 감지 (건물 아이콘 확대 적용)
 */
public class MapPanel extends JPanel {

	// =======================================================================
	// --- 1. Fields & Constants ---
	// =======================================================================

	private static final String MAP_IMAGE_PATH = "src/resources/images/map/campus_map.jpg";

	// 시설 아이콘 표시 기준 (1.05배 이상 확대 시)
	private static final double FACILITY_VISIBLE_THRESHOLD = 1.05;

	private JLabel mapLabel;
	private Image originalImage;
	private MapController mapController;

	private List<Building> buildingList = new ArrayList<>();
	private List<Facility> facilityList = new ArrayList<>();

	private double zoomLevel = 1.0;
	private boolean showFacilities = false;

	private int mapX = 0;
	private int mapY = 0;
	private int lastMouseX = 0;
	private int lastMouseY = 0;

	private final int scrollSensitivity = 1; // 마우스 휠 스크롤 감도 조절
	private int flagScrol = 0;

	// [수정됨] 건물 아이콘 크기 30 -> 45로 확대
	private static final int BUILDING_ICON_SIZE = 35;

	private static final int FACILITY_ICON_SIZE = 14;
	private static final Color COLOR_BUILDING = new Color(65, 105, 225);
	private static final Color COLOR_FACILITY = new Color(255, 105, 180);

	private final int ICON_COUNT = 100; // 지도에 출력되는 모든 빌딩, 시설 아이콘 최대 갯수
	private int unUsableSpace[][] = new int[ICON_COUNT][3]; // new 아이콘의 x좌표, y좌표, 사이즈
	private final int pageWidth = 200;
	private final int pageHeight = 120;

	// =======================================================================
	// --- 2. Constructor & Public API ---
	// =======================================================================

	public MapPanel() {
		super(null);

		loadOriginalMapImage();
		mapLabel = new JLabel();
		mapLabel.setHorizontalAlignment(JLabel.CENTER);
		this.add(mapLabel);

		setupMouseWheelListener();
		setupMouseDragListener();
		setupMouseClickListener();
		setupResizeListener();

		resizeAndRepaintMap();
	}

	public void setupController(MapController controller) {
		this.mapController = controller;
	}

	public void setMapData(List<Building> buildings, List<Facility> facilities) {
		this.buildingList = buildings;
		this.facilityList = facilities;
		repaint();
	}

	public boolean isShowFacilities() {
		return showFacilities;
	}

	// =======================================================================
	// --- 3. Painting Logic ---
	// =======================================================================

	@Override
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		drawIcons(g2);
	}

	private void drawIcons(Graphics2D g2) {
		int currentMapWidth = mapLabel.getWidth();
		int originalMapWidth = originalImage.getWidth(null);
		double scaleRatio = (double) currentMapWidth / originalMapWidth;

		// 시설 아이콘
		int j = 13; // new
		if (showFacilities) {
			g2.setColor(COLOR_FACILITY);
			for (Facility f : facilityList) {
				int x = mapX + (int) (f.getxLocation() * scaleRatio);
				int y = mapY + (int) (f.getyLocation() * scaleRatio);
				int offset = FACILITY_ICON_SIZE / 2;
				g2.fillOval(x - offset, y - offset, FACILITY_ICON_SIZE, FACILITY_ICON_SIZE);
				g2.setColor(Color.WHITE);
				g2.drawOval(x - offset, y - offset, FACILITY_ICON_SIZE, FACILITY_ICON_SIZE);
				g2.setColor(COLOR_FACILITY);
			}
		}
		// 건물 아이콘
		int i = 0; // new
		for (Building b : buildingList) {
			g2.setColor(COLOR_BUILDING);
			int x = mapX + (int) (b.getxLocation() * scaleRatio);
			int y = mapY + (int) (b.getyLocation() * scaleRatio);
			int offset = BUILDING_ICON_SIZE / 2;

			g2.fillRoundRect(x - offset, y - offset, BUILDING_ICON_SIZE, BUILDING_ICON_SIZE, 12, 12);
			g2.setColor(Color.WHITE);
			g2.setStroke(new java.awt.BasicStroke(2));
			g2.drawRoundRect(x - offset, y - offset, BUILDING_ICON_SIZE, BUILDING_ICON_SIZE, 12, 12);

			String idText = String.format("%02d", b.getId() % 100);
			// [수정됨] 폰트 크기도 14 -> 16으로 살짝 키움
			g2.setFont(new Font("SansSerif", Font.BOLD, 16));
			FontMetrics fm = g2.getFontMetrics();
			int textX = x - (fm.stringWidth(idText) / 2);
			int textY = y + (fm.getAscent() - fm.getDescent() + fm.getLeading()) / 2;
			g2.drawString(idText, textX, textY);
		}
	}

	// =======================================================================
	// --- 4. Setup Methods ---
	// =======================================================================

	private void setupMouseClickListener() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleIconClick(e.getX(), e.getY());
				System.out.println("좌표 : " + e.getX() + ", " + e.getY());

                repaint(); // 페이지를 그리거나 지우기 위해 화면 갱신을 요청합니다.
                System.out.println("출력완료");                // --- [테스트 로직 종료] ---
			}
		});
	}

	private void handleIconClick(int mouseX, int mouseY) {
		int currentMapWidth = mapLabel.getWidth();
		int originalMapWidth = originalImage.getWidth(null);
		double scaleRatio = (double) currentMapWidth / originalMapWidth;

		// 1. 시설 아이콘 클릭 확인
		if (showFacilities) {
			int radius = FACILITY_ICON_SIZE / 2;
			for (Facility f : facilityList) {
				int cx = mapX + (int) (f.getxLocation() * scaleRatio);
				int cy = mapY + (int) (f.getyLocation() * scaleRatio);

				if (mouseX >= cx - radius && mouseX <= cx + radius && mouseY >= cy - radius && mouseY <= cy + radius) {

					mapController.onFacilityClicked(f);
					return;
				}
			}
		}

		// 2. 건물 아이콘 클릭 확인
		int radius = BUILDING_ICON_SIZE / 2;
		for (Building b : buildingList) {
			int cx = mapX + (int) (b.getxLocation() * scaleRatio);
			int cy = mapY + (int) (b.getyLocation() * scaleRatio);

			if (mouseX >= cx - radius && mouseX <= cx + radius && mouseY >= cy - radius && mouseY <= cy + radius) {

				mapController.onBuildingClicked(b);
				return;
			}
		}
	}

	private void setupMouseWheelListener() {
		this.addMouseWheelListener(e -> {
			if (flagScrol++ == scrollSensitivity) {
				flagScrol = 0;
				int mouseX = e.getX();
				int mouseY = e.getY();

				double prevZoom = zoomLevel;
				if (e.getWheelRotation() < 0) {
					zoomLevel *= 1.1;
				} else {
					zoomLevel /= 1.1;
				}

				if (zoomLevel < 1.0) {
					zoomLevel = 1.0;
				} else if (zoomLevel > 3.0) {
					zoomLevel = 3.0;
				}
				if (zoomLevel == prevZoom) {
					return;
				}

				setSizeMap(mouseX, mouseY);
			}
		});
	}

	private void setupMouseDragListener() {
		MouseAdapter mouseHandler = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lastMouseX = e.getX();
				lastMouseY = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				int dx = e.getX() - lastMouseX;
				int dy = e.getY() - lastMouseY;
				mapX += dx;
				mapY += dy;
				lastMouseX = e.getX();
				lastMouseY = e.getY();
				constrainMapPosition();
				mapLabel.setLocation(mapX, mapY);
				repaint();
			}
		};
		this.addMouseListener(mouseHandler);
		this.addMouseMotionListener(mouseHandler);
	}

	private void setupResizeListener() {
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				resizeAndRepaintMap();
			}
		});
	}

	// =======================================================================
	// --- 5. Core Logic ---
	// =======================================================================

	private void resizeAndRepaintMap() {
		if (getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

		int panelWidth = getWidth();
		int panelHeight = getHeight();
		int origW = originalImage.getWidth(null);
		int origH = originalImage.getHeight(null);

		double baseRatio = Math.min((double) panelWidth / origW, (double) panelHeight / origH);
		double finalRatio = baseRatio * zoomLevel;

		int newWidth = (int) (origW * finalRatio);
		int newHeight = (int) (origH * finalRatio);

		Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
		mapLabel.setIcon(new ImageIcon(resizedImage));
		mapLabel.setSize(newWidth, newHeight);

		constrainMapPosition();
		mapLabel.setLocation(mapX, mapY);
		repaint();
	}

	private void constrainMapPosition() {
		int panelWidth = getWidth();
		int panelHeight = getHeight();
		int imgWidth = mapLabel.getWidth();
		int imgHeight = mapLabel.getHeight();

		if (imgWidth > panelWidth) {
			if (mapX > 0) {
				mapX = 0;
			}
			if (mapX < panelWidth - imgWidth) {
				mapX = panelWidth - imgWidth;
			}
		} else {
			mapX = (panelWidth - imgWidth) / 2;
		}

		if (imgHeight > panelHeight) {
			if (mapY > 0) {
				mapY = 0;
			}
			if (mapY < panelHeight - imgHeight) {
				mapY = panelHeight - imgHeight;
			}
		} else {
			mapY = (panelHeight - imgHeight) / 2;
		}
	}

	private void checkVisibilityMode() {
		boolean previousState = showFacilities;
		if (zoomLevel >= FACILITY_VISIBLE_THRESHOLD) {
			showFacilities = true;
		} else {
			showFacilities = false;
		}

		if (previousState != showFacilities) {
			System.out.println("[View] 시설 표시 모드: " + (showFacilities ? "ON" : "OFF"));
			repaint();
		}
	}

	private void loadOriginalMapImage() {
		try {
			File file = new File(MAP_IMAGE_PATH);
			if (file.exists()) {
				originalImage = ImageIO.read(file);
			}
		} catch (IOException e) {
			// 무시
		}
	}

	public void setSizeMap(int mouseX, int mouseY) { //

		double currentMapX = mapLabel.getX();
		double currentMapY = mapLabel.getY();
		double currentMapW = mapLabel.getWidth();
		double currentMapH = mapLabel.getHeight();

		double relX = (mouseX - currentMapX) / currentMapW;
		double relY = (mouseY - currentMapY) / currentMapH;

		int panelWidth = getWidth();
		int panelHeight = getHeight();
		int origW = originalImage.getWidth(null);
		int origH = originalImage.getHeight(null);

		double baseRatio = Math.min((double) panelWidth / origW, (double) panelHeight / origH);
		double newScale = baseRatio * zoomLevel;

		int newMapW = (int) (origW * newScale);
		int newMapH = (int) (origH * newScale);

		mapX = (int) (mouseX - (relX * newMapW));
		mapY = (int) (mouseY - (relY * newMapH));

		checkVisibilityMode();
		resizeAndRepaintMap();
	}

	public void setMapFocusOn(Building building) {
		final double focus_zoom = 3;
		this.zoomLevel = focus_zoom;

		int panelWidth = getWidth();
		int panelHeight = getHeight();
		int origW = originalImage.getWidth(null);
		int origH = originalImage.getHeight(null);

		double baseRatio = Math.min((double) panelWidth / origW, (double) panelHeight / origH);
		double finalScale = baseRatio * this.zoomLevel;

		mapX = (panelWidth / 2) - (int) (building.getxLocation() * finalScale);
		mapY = (panelHeight / 2) - (int) (building.getyLocation() * finalScale);

		checkVisibilityMode();
		resizeAndRepaintMap();
	}

	public void setMapFocusOn(Facility facility) {
		final double focus_zoom = 3;
		this.zoomLevel = focus_zoom;

		int panelWidth = getWidth();
		int panelHeight = getHeight();
		int origW = originalImage.getWidth(null);
		int origH = originalImage.getHeight(null);

		double baseRatio = Math.min((double) panelWidth / origW, (double) panelHeight / origH);
		double finalScale = baseRatio * this.zoomLevel;

		mapX = (panelWidth / 2) - (int) (facility.getxLocation() * finalScale);
		mapY = (panelHeight / 2) - (int) (facility.getyLocation() * finalScale);

		checkVisibilityMode();
		resizeAndRepaintMap();
	}

	public void setZoomLevel(double level) {
		this.zoomLevel = level;
	}

	public void addUnUsableSpace(int index, int x, int y, int size) {
		unUsableSpace[index][0] = x;
		unUsableSpace[index][1] = y;
		unUsableSpace[index][2] = size;
	} // new unUsableSpace에 빌딩, 시설 아이콘 추가 메소드

	public boolean checkUnUsableSpace(int x, int y) {
		for (int[] a : unUsableSpace) {
			if (a[2] == 0) {
				break; // size가 0인 것, 즉 더이상 객체가 없으면 순회 종료
			}
			if (a[0] <= x + pageWidth && x <= (a[0] + a[2])) {
				if (a[1] <= y + pageHeight && y <= (a[1] + a[2])) {
					return false;
				}
			}
		}
		return true;
	}


}