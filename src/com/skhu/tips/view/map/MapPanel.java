// 김준
package com.skhu.tips.view.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
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
 * @brief 지도 표시 + 줌/이동 + 아이콘 그리기 + 모든 시설 간략 정보 팝업 표시
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

	private static final int BUILDING_ICON_SIZE = 35;
	private static final int FACILITY_ICON_SIZE = 14;

	private static final Color COLOR_BUILDING = new Color(65, 105, 225);
	private static final Color COLOR_FACILITY = new Color(255, 105, 180);

	// =======================================================================
	// --- 2. Constructor & Public API ---
	// =======================================================================

	public MapPanel() {
		super(null);
		setBackground(new Color(240, 240, 240));

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
		super.paintChildren(g); // 지도(JLabel) 그리기

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		drawIcons(g2);

		// 모든 시설의 간략 정보 팝업 그리기 (시설 표시 모드일 때)
		if (showFacilities) {
			for (Facility f : facilityList) {
				drawBriefPopup(g2, f);
			}
		}

		// 우측 상단 이용 가이드 표시 (항상 맨 위에 표시)
				drawUsageGuide(g2);
	}

	private void drawIcons(Graphics2D g2) {
		int currentMapWidth = mapLabel.getWidth();
		int originalMapWidth = originalImage != null ? originalImage.getWidth(null) : 1;
		double scaleRatio = (double) currentMapWidth / originalMapWidth;

		// 1. 시설 아이콘
		if (showFacilities) {
			for (Facility f : facilityList) {
				int x = mapX + (int) (f.getxLocation() * scaleRatio);
				int y = mapY + (int) (f.getyLocation() * scaleRatio);
				int offset = FACILITY_ICON_SIZE / 2;

				g2.setColor(COLOR_FACILITY);
				g2.fillOval(x - offset, y - offset, FACILITY_ICON_SIZE, FACILITY_ICON_SIZE);
				g2.setColor(Color.WHITE);
				g2.setStroke(new BasicStroke(1));
				g2.drawOval(x - offset, y - offset, FACILITY_ICON_SIZE, FACILITY_ICON_SIZE);
			}
		}

		// 2. 건물 아이콘
		for (Building b : buildingList) {
			g2.setColor(COLOR_BUILDING);
			int x = mapX + (int) (b.getxLocation() * scaleRatio);
			int y = mapY + (int) (b.getyLocation() * scaleRatio);
			int offset = BUILDING_ICON_SIZE / 2;

			g2.fillRoundRect(x - offset, y - offset, BUILDING_ICON_SIZE, BUILDING_ICON_SIZE, 12, 12);
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(2));
			g2.drawRoundRect(x - offset, y - offset, BUILDING_ICON_SIZE, BUILDING_ICON_SIZE, 12, 12);

			String idText = String.format("%02d", b.getId() % 100);
			g2.setFont(new Font("SansSerif", Font.BOLD, 16));
			FontMetrics fm = g2.getFontMetrics();
			int textX = x - (fm.stringWidth(idText) / 2);
			int textY = y + (fm.getAscent() - fm.getDescent() + fm.getLeading()) / 2;
			g2.drawString(idText, textX, textY);
		}
	}

	/**
	 * @brief 시설 아이콘 우측에 연결된 간략 정보 팝업을 그립니다.
	 */
	private void drawBriefPopup(Graphics2D g2, Facility f) {
		int currentMapWidth = mapLabel.getWidth();
		int originalMapWidth = originalImage != null ? originalImage.getWidth(null) : 1;
		double scaleRatio = (double) currentMapWidth / originalMapWidth;

		// 아이콘 중심 좌표
		int iconX = mapX + (int) (f.getxLocation() * scaleRatio);
		int iconY = mapY + (int) (f.getyLocation() * scaleRatio);

		// 팝업 영역 계산 (크기 변경됨)
		Rectangle bounds = calculatePopupBounds(iconX, iconY);
		int popupX = bounds.x;
		int popupY = bounds.y;
		int popupWidth = bounds.width;
		int popupHeight = bounds.height;
		int arc = 15;

		// --- 1. 연결 선 그리기 ---
		// [수정됨] 선 색상을 아이콘과 동일한 핑크색으로 변경
		g2.setColor(COLOR_FACILITY);
		g2.setStroke(new BasicStroke(2));
		g2.drawLine(iconX + (FACILITY_ICON_SIZE/2), iconY, popupX, iconY);

		// --- 2. 팝업 배경 그리기 ---
		g2.setColor(new Color(0, 0, 0, 50)); // 그림자
		g2.fillRoundRect(popupX + 3, popupY + 3, popupWidth, popupHeight, arc, arc);

		g2.setColor(Color.WHITE); // 배경
		g2.fillRoundRect(popupX, popupY, popupWidth, popupHeight, arc, arc);

		g2.setColor(new Color(200, 200, 200)); // 테두리
		g2.setStroke(new BasicStroke(1));
		g2.drawRoundRect(popupX, popupY, popupWidth, popupHeight, arc, arc);

		// --- 3. 텍스트 내용 그리기 ---
		int padding = 10;
		int currentY = popupY + padding;

		// 3-1. ID (원형 배지) & 이름
		g2.setColor(COLOR_FACILITY);
		g2.fillOval(popupX + padding, currentY, 20, 20);

		g2.setColor(Color.WHITE);
		g2.setFont(new Font("SansSerif", Font.BOLD, 11));
		String idStr = String.valueOf(f.getId());
		FontMetrics fmId = g2.getFontMetrics();
		g2.drawString(idStr,
				popupX + padding + 10 - (fmId.stringWidth(idStr) / 2),
				currentY + 14);

		g2.setColor(Color.BLACK);
		g2.setFont(new Font("SansSerif", Font.BOLD, 15));
		g2.drawString(f.getName(), popupX + padding + 28, currentY + 15);

		// [수정됨] 이름과 Overview 사이 간격 넓힘 (30 -> 35)
		currentY += 38;

		// 3-2. Overview
		g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
		g2.setColor(Color.DARK_GRAY);
		String overview = f.getOverview();
		// 너비가 줄어들었으므로 글자수 제한을 조금 더 타이트하게 조정 (18 -> 15)
		if (overview != null && overview.length() > 15) {
			overview = overview.substring(0, 15) + "...";
		}
		g2.drawString(overview != null ? overview : "-", popupX + padding, currentY);

		// [수정됨] Overview와 Notice 사이 간격 넓힘 (20 -> 25)
		currentY += 10;

		// 3-3. Notice
		g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
		g2.setColor(new Color(220, 20, 60));
		String notice = f.getNotice();
		if (notice != null && notice.length() > 18) {
			notice = notice.substring(0, 18) + "...";
		}
		g2.drawString("📢 " + (notice != null ? notice : "-"), popupX + padding - 4, currentY + 5);

		// 3-4. Detail Hint
		g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
		g2.setColor(Color.GRAY);
		g2.drawString("Detail >", popupX + popupWidth - 45, popupY + popupHeight - 8);
	}

	/**
	 * @brief 아이콘 좌표를 기준으로 팝업의 영역을 계산합니다.
	 */
	private Rectangle calculatePopupBounds(int iconX, int iconY) {
		// [수정됨] 크기 조정: 너비(200), 높이 축소(115 -> 95) - Notice 아래 여백 최소화
		int popupWidth = 190;
		int popupHeight = 80;
		int lineLength = 30;   // 연결 선 길이

		int popupX = iconX + lineLength;
		int popupY = iconY - (popupHeight / 2);

		return new Rectangle(popupX, popupY, popupWidth, popupHeight);
	}

	/**
	 * @brief [추가됨] 지도 우측 상단에 이용 가이드 문구를 표시합니다.
	 */
	private void drawUsageGuide(Graphics2D g2) {
		// 안내 문구 내용
		String title = "💡 이용 가이드";
		String line1 = "- 스크롤하여 확대하면 시설 정보 확인 가능";
		String line2 = "- 아이콘을 클릭하여 상세 정보 확인";

		// 폰트 설정
		g2.setFont(new Font("SansSerif", Font.BOLD, 12));
		FontMetrics fm = g2.getFontMetrics();

		// 박스 크기 계산
		int maxWidth = Math.max(fm.stringWidth(title), Math.max(fm.stringWidth(line1), fm.stringWidth(line2)));
		int padding = 15;
		int lineHeight = fm.getHeight() + 5;

		int boxWidth = maxWidth + (padding * 2);
		int boxHeight = (lineHeight * 3) + (padding * 2) - 10;

		// 위치 계산 (우측 상단, 마진 20px)
		int x = getWidth() - boxWidth - 20;
		int y = 20;

		// 1. 반투명 배경 박스
		g2.setColor(new Color(0, 0, 0, 180)); // 검은색, 투명도 180
		g2.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);

		// 2. 텍스트 그리기
		g2.setColor(Color.WHITE);
		int textX = x + padding;
		int textY = y + padding + fm.getAscent();

		// 타이틀 (노란색 강조)
		g2.setColor(new Color(255, 215, 0)); // Gold
		g2.drawString(title, textX, textY);

		// 내용 (흰색)
		g2.setColor(Color.WHITE);
		g2.setFont(new Font("SansSerif", Font.PLAIN, 12)); // 내용은 일반 굵기

		textY += lineHeight;
		g2.drawString(line1, textX, textY);

		textY += lineHeight;
		g2.drawString(line2, textX, textY);
	}


	// =======================================================================
	// --- 4. Setup Methods ---
	// =======================================================================

	private void setupMouseClickListener() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleIconClick(e.getX(), e.getY());
			}
		});
	}

	private void handleIconClick(int mouseX, int mouseY) {
		int currentMapWidth = mapLabel.getWidth();
		int originalMapWidth = originalImage != null ? originalImage.getWidth(null) : 1;
		double scaleRatio = (double) currentMapWidth / originalMapWidth;

		// 1. [수정됨] 모든 시설 팝업 클릭 확인
		if (showFacilities) {
			// Z-order를 고려하여 역순(나중에 그려진 것이 위)으로 체크하는 것이 좋음
			for (int i = facilityList.size() - 1; i >= 0; i--) {
				Facility f = facilityList.get(i);
				int iconX = mapX + (int) (f.getxLocation() * scaleRatio);
				int iconY = mapY + (int) (f.getyLocation() * scaleRatio);

				Rectangle bounds = calculatePopupBounds(iconX, iconY);

				if (bounds.contains(mouseX, mouseY)) {
					System.out.println("[MapPanel] 시설 팝업 클릭됨: " + f.getName());
					mapController.onFacilityClicked(f);
					return;
				}
			}
		}

		// 2. 시설 아이콘 클릭 확인
		if (showFacilities) {
			int radius = FACILITY_ICON_SIZE;
			for (Facility f : facilityList) {
				int cx = mapX + (int) (f.getxLocation() * scaleRatio);
				int cy = mapY + (int) (f.getyLocation() * scaleRatio);

				if (mouseX >= cx - radius && mouseX <= cx + radius &&
					mouseY >= cy - radius && mouseY <= cy + radius) {

					System.out.println("[MapPanel] 시설 아이콘 클릭됨: " + f.getName());
					mapController.onFacilityClicked(f);
					return;
				}
			}
		}

		// 3. 건물 아이콘 클릭 확인
		int radius = BUILDING_ICON_SIZE / 2;
		for (Building b : buildingList) {
			int cx = mapX + (int) (b.getxLocation() * scaleRatio);
			int cy = mapY + (int) (b.getyLocation() * scaleRatio);

			if (mouseX >= cx - radius && mouseX <= cx + radius &&
				mouseY >= cy - radius && mouseY <= cy + radius) {

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
		if (getWidth() <= 0 || getHeight() <= 0 || originalImage == null) {
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
			e.printStackTrace();
		}
	}

	public void setSizeMap(int mouseX, int mouseY) {
		double currentMapX = mapLabel.getX();
		double currentMapY = mapLabel.getY();
		double currentMapW = mapLabel.getWidth();
		double currentMapH = mapLabel.getHeight();

		double relX = (mouseX - currentMapX) / currentMapW;
		double relY = (mouseY - currentMapY) / currentMapH;

		int panelWidth = getWidth();
		int panelHeight = getHeight();

		if (originalImage == null) {
			return;
		}

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

		if (originalImage == null) {
			return;
		}

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

		if (originalImage == null) {
			return;
		}

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
		checkVisibilityMode();
	}
}