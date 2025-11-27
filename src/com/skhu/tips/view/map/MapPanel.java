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
import java.util.Collections;
import java.util.Comparator;
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
 * @brief 지도 표시 + 줌/이동 + 아이콘 그리기 + 모든 시설 간략 정보 팝업 표시 + 이용 가이드
 */
public class MapPanel extends JPanel {

	// =======================================================================
	// --- 1. Fields & Constants ---
	// =======================================================================

	private static final String MAP_IMAGE_PATH = "src/resources/images/map/campus_map.jpg";

	// 시설 아이콘 표시 기준 (1.05배 이상 확대 시)
	private static final double FACILITY_VISIBLE_THRESHOLD = 1.05;

	// [추가됨] 화면에 동시에 표시할 최대 팝업(간략 정보) 개수
	private static int maxVisiblePopus = 8;

	private JLabel mapLabel;
	private Image originalImage;
	private MapController mapController;

	private List<Building> buildingList = new ArrayList<>();
	private List<Facility> facilityList = new ArrayList<>();

	private List<Rectangle> drawnPopupBounds = new ArrayList<>(); // 김준 팝업들이 그려지면 여기에 추가.

	// [1. 추가] 팝업의 클릭 영역(Bounds)과 시설 정보를 묶어 저장할 클래스와 리스트
	private class PopupHitbox {
        Rectangle bounds;
        Facility facility;
        public PopupHitbox(Rectangle bounds, Facility facility) {
            this.bounds = bounds;
            this.facility = facility;
        }
    }
	private List<PopupHitbox> currentPopupHitboxes = new ArrayList<>();

	private double zoomLevel = 1.0;
	private boolean showFacilities = false;

	private int mapX = 0;
	private int mapY = 0;
	private int lastMouseX = 0;
	private int lastMouseY = 0;

	private final int scrollSensitivity = 1; // 마우스 휠 스크롤 감도 조절
	private int flagScrol = 0;

	private static final int FACILITY_ICON_SIZE = 14;
	private static final int DEFAULT_BUILDING_ICON_SIZE = 35;
	private static int buildingIconSize = DEFAULT_BUILDING_ICON_SIZE;

	private static final int INFO_ICON_SIZE = 30; // INFO 아이콘의 지름
	private static final int INFO_ICON_PADDING = 10; // INFO 좌측 및 상단 여백

	private static final Color COLOR_BUILDING = new Color(65, 105, 225);
	private static final Color COLOR_FACILITY = new Color(255, 105, 180);

	private static final int BLANK_SPACE = 2; // 팝업간의 간격, 10*2 = 20칸

	// --- 간략 정보(말풍선) 관련 필드 ---
	private Facility selectedFacility = null; // 현재 선택되어 간략 정보를 보여줄 시설 (클릭 이동용)

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

		// [2. 추가] 매번 그리기 전에 기존 Hitbox 리스트 초기화
        currentPopupHitboxes.clear();
        drawnPopupBounds.clear(); // (기존에 있던 clear는 여기서 같이 처리)

		Graphics2D g2 = (Graphics2D) g;
		Rectangle centerBounds = getCenterOneThirdBounds(); // 중앙 1/3 영역 계산


	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	    drawIcons(g2);

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		drawIcons(g2);

		drawnPopupBounds.clear(); // 이전에 넣어둔 팝업 리스트 초기화

		// [수정됨] 모든 시설의 간략 정보 팝업 그리기 (시설 표시 모드일 때)
		// 화면에 보이는 아이콘 중 ID 순서대로 최대 8개까지만 표시
		if (showFacilities) {
			int popupCount = 0;
			int panelWidth = getWidth();
			int panelHeight = getHeight();

			// 좌표 계산을 위한 비율 (drawIcons와 동일 로직)
			int currentMapWidth = mapLabel.getWidth();
			int originalMapWidth = originalImage != null ? originalImage.getWidth(null) : 1;
			double scaleRatio = (double) currentMapWidth / originalMapWidth;


			//맵 중앙에 있는 아이콘들 우선순위 최고등급으로
			for (Facility f : facilityList) {
	            // 1. 현재 화면상 아이콘 중심 좌표 계산
	            int iconX = mapX + (int) (f.getxLocation() * scaleRatio);
	            int iconY = mapY + (int) (f.getyLocation() * scaleRatio);

	            // 2. 아이콘 중심 좌표가 중앙 영역 내에 있는지 확인
	            if (centerBounds.contains(iconX, iconY)) {
	                // 영역 내에 있으면 priority를 최우선(0)으로 설정
	                f.setPriority(0);
	            } else {
	            	f.setPriority(f.getId());
	            }
			}

			// 김준 수정사항, 우선순위에 맞춰 리스트 재정렬. facilities.json에서 값 수정
			Collections.sort(facilityList, new Comparator<Facility>() {
				@Override
				public int compare(Facility f1, Facility f2) {
					// priority 값이 낮은 시설이 앞으로 오도록 (오름차순)
					return Integer.compare(f1.getPriority(), f2.getPriority());
				}
			});

			for (Facility f : facilityList) {
				// 1. 현재 화면상 좌표 계산
				int iconX = mapX + (int) (f.getxLocation() * scaleRatio);
				int iconY = mapY + (int) (f.getyLocation() * scaleRatio);

				// 2. 현재 아이콘이 화면 영역(패널) 안에 있는지 확인
				// (완전히 밖으로 나간 것은 그리지 않음으로써 8개 카운트에서 제외)
				if (iconX >= 0 && iconX <= panelWidth && iconY >= 0 && iconY <= panelHeight) {

					// 3. 팝업 그리기
					if (drawBriefPopup(g2, f)) {
						popupCount++;
					}

					// 4. 최대 개수 도달 시 중단
					if (popupCount >= maxVisiblePopus) {
						break;
					}
				}
			}
		}

		// 우측 상단 이용 가이드 표시 (항상 맨 위에 표시)
		drawUsageGuide(g2);

		drawInfoIcon(g2);
	}

	private void drawIcons(Graphics2D g2) {
		int currentMapWidth = mapLabel.getWidth();
		int originalMapWidth = originalImage != null ? originalImage.getWidth(null) : 1;
		double scaleRatio = (double) currentMapWidth / originalMapWidth;

		// 1. 건물 아이콘
		for (Building b : buildingList) {
			g2.setColor(COLOR_BUILDING);
			int x = mapX + (int) (b.getxLocation() * scaleRatio);
			int y = mapY + (int) (b.getyLocation() * scaleRatio);
			int offset = buildingIconSize / 2;

			g2.fillRoundRect(x - offset, y - offset, buildingIconSize, buildingIconSize, 12, 12);
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(2));
			g2.drawRoundRect(x - offset, y - offset, buildingIconSize, buildingIconSize, 12, 12);

			String idText = String.format("%02d", b.getId() % 100);
			g2.setFont(new Font("SansSerif", Font.BOLD, offset));
			FontMetrics fm = g2.getFontMetrics();
			int textX = x - (fm.stringWidth(idText) / 2);
			int textY = y + (fm.getAscent() - fm.getDescent() + fm.getLeading()) / 2;
			g2.drawString(idText, textX, textY);

			// 지도에 그려진 좌표 Building 필드에 저장
			int currentX = x - offset;
			int currentY = y - offset;
			b.setCurrentX(currentX);
			b.setCurrentY(currentY);
		}

		// 2. 시설 아이콘
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

				// 지도에 그려진 좌표 Facility 필드에 저장
				int currentX = x - offset;
				int currentY = y - offset;
				f.setCurrentX(currentX);
				f.setCurrentY(currentY);
			}
		}

	}

	/**
	 * @brief 시설 아이콘에 연결된 간략 정보 팝업을 그립니다. 팝업의 위치에 따라 연결 선을 자동으로 조정합니다.
	 */
	private boolean drawBriefPopup(Graphics2D g2, Facility f) {
		int currentMapWidth = mapLabel.getWidth();
		int originalMapWidth = originalImage != null ? originalImage.getWidth(null) : 1;
		double scaleRatio = (double) currentMapWidth / originalMapWidth;

		// 아이콘 중심 좌표
		int iconX = mapX + (int) (f.getxLocation() * scaleRatio);
		int iconY = mapY + (int) (f.getyLocation() * scaleRatio);

		// 팝업 영역 계산 (패널 크기 전달하여 알고리즘 적용 가능하게 함)
		Rectangle bounds = calculatePopupBounds(iconX, iconY, getWidth(), getHeight());

		if (bounds == null) { // calculaterPopupBounds에서 제대로 리턴되지 않는다면, 해당 팝업 그리기 포기.
			return false;
		}

		drawnPopupBounds.add(bounds); // 김준, 그려지는 팝업 리스트화. 이후에 충돌 방지를 위하여

		// [3. 추가] 화면에 그려지는 팝업의 위치와 시설 정보를 저장
        currentPopupHitboxes.add(new PopupHitbox(bounds, f));

		int popupX = bounds.x;
		int popupY = bounds.y;
		int popupWidth = bounds.width;
		int popupHeight = bounds.height;
		int arc = 15;

		// --- 1. 연결 선 그리기 (자동 방향 감지) ---
		// 분리된 메서드 호출
		drawConnectionLine(g2, iconX, iconY, bounds);

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
		g2.drawString(idStr, popupX + padding + 10 - (fmId.stringWidth(idStr) / 2), currentY + 14);

		g2.setColor(Color.BLACK);
		g2.setFont(new Font("SansSerif", Font.BOLD, 14));
		g2.drawString(f.getName(), popupX + padding + 28, currentY + 15);

		currentY += 32;

		// 3-2. [New] Location & Floor (건물명 + 층수)
		g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
		g2.setColor(new Color(80, 80, 80)); // 짙은 회색

		String buildingName = f.getBuildingName() != null ? f.getBuildingName() : "";
		int floor = f.getFloor();
		String floorStr = "";
		if (floor > 0) {
			floorStr = floor + "층";
		} else if (floor < 0) {
			floorStr = "B" + Math.abs(floor);
		}

		String locationText = buildingName + " " + floorStr;
		if (locationText.length() > 22) {
			locationText = locationText.substring(0, 22) + "...";
		}
		g2.drawString("📍 " + locationText.trim(), popupX + padding - 4, currentY);

		currentY += 20;

		// 3-3. Overview
		g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
		g2.setColor(Color.DARK_GRAY);
		String overview = f.getOverview();
		if (overview != null && overview.length() > 15) {
			overview = overview.substring(0, 15) + "...";
		}
		g2.drawString(overview != null ? overview : "-", popupX + padding, currentY);

		currentY += 10;

		// 3-4. Notice
		g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
		g2.setColor(new Color(220, 20, 60));
		String notice = f.getNotice();
		if (notice != null && notice.length() > 18) {
			notice = notice.substring(0, 18) + "...";
		}
		g2.drawString("📢 " + (notice != null ? notice : "-"), popupX + padding - 4, currentY + 5);

		// 3-5. Detail Hint
		g2.setFont(new Font("SansSerif", Font.ITALIC, 9));
		g2.setColor(Color.LIGHT_GRAY);
		g2.drawString("Detail >", popupX + popupWidth - 45, popupY + popupHeight - 8);
		return true; // 김준
	}

	/**
	 * @brief 시설 아이콘과 팝업 간의 연결 선을 그립니다.
	 * @param g2          Graphics2D 객체
	 * @param iconX       아이콘의 중심 X 좌표 (화면 좌표)
	 * @param iconY       아이콘의 중심 Y 좌표 (화면 좌표)
	 * @param bounds      팝업이 그려질 영역 (Rectangle)
	 * @param popupWidth  팝업의 너비
	 * @param popupHeight 팝업의 높이
	 */
	private void drawConnectionLine(Graphics2D g2, int iconX, int iconY, Rectangle bounds) {

		// --- 1. 스타일 설정 ---
		g2.setColor(COLOR_FACILITY);
		g2.setStroke(new BasicStroke(2));

		// 팝업의 위치 정보 추출
		int popupX = bounds.x;
		int popupY = bounds.y;
		double popupCenterX = bounds.getCenterX();
		double popupCenterY = bounds.getCenterY();

		int startX = iconX;
		int startY = iconY;
		int endX = iconX;
		int endY = iconY;

		// --- 2. 선 좌표 계산 (자동 방향 감지) ---
		// 가로 거리가 세로 거리보다 멀면 좌/우 배치로 간주
		if (Math.abs(popupCenterX - iconX) > Math.abs(popupCenterY - iconY)) {
			// 수평 연결
			startY = iconY;
			// 선 끝점이 팝업 박스 높이 범위를 벗어나지 않도록 클램핑
			endY = Math.max(popupY, Math.min(popupY + bounds.height, iconY));

			if (popupCenterX > iconX) { // 팝업이 오른쪽
				startX = iconX + (FACILITY_ICON_SIZE / 2);
				endX = popupX;
			} else { // 팝업이 왼쪽
				startX = iconX - (FACILITY_ICON_SIZE / 2);
				endX = popupX + bounds.width;
			}
		} else {
			// 수직 연결
			startX = iconX;
			endX = Math.max(popupX, Math.min(popupX + bounds.width, iconX));

			if (popupCenterY > iconY) { // 팝업이 아래
				startY = iconY + (FACILITY_ICON_SIZE / 2);
				endY = popupY;
			} else { // 팝업이 위
				startY = iconY - (FACILITY_ICON_SIZE / 2);
				endY = popupY + bounds.height;
			}
		}

		// --- 3. 실제 선 그리기 ---
		g2.drawLine(startX, startY, endX, endY);
	}

	/**
	 * @brief 아이콘 좌표를 기준으로 팝업의 영역을 계산합니다. [수정됨] 패널 크기 정보(panelWidth, panelHeight)를
	 *        인자로 추가하여 화면 경계를 벗어나는지 확인하고 위치를 조정하는 알고리즘을 작성할 수 있도록 변경했습니다.
	 */
	private Rectangle calculatePopupBounds(int iconX, int iconY, int panelWidth, int panelHeight) {
		// [수정됨] 높이 증가 (95 -> 115) : 건물 위치 정보 라인 추가로 인한 확장
		int popupWidth = 190;
		int popupHeight = 100;
		int margin = 30; // 아이콘 중심에서 팝업까지의 거리 (연결 선 길이)

		// -----------------------------------------------------------------------
		// TODO: [알고리즘 구현부] 여기서 위치를 결정하세요.
		// iconX, iconY: 아이콘의 현재 화면상 중심 좌표
		// panelWidth, panelHeight: 전체 패널(화면) 크기
		// -----------------------------------------------------------------------

		// 예시: 기본적으로 오른쪽에 배치
		int popupX = iconX + margin;
		int popupY = iconY - (popupHeight / 2);

		// 이미 그려졌다면? 피해야 함. 피하는 로직 추가
		if (checkUnUsableSpace(popupX, popupY, popupWidth, popupHeight, panelWidth, panelHeight)) {
			// true로 빠져나오면 충돌이 없다는 뜻.
			return new Rectangle(popupX, popupY, popupWidth, popupHeight);

		}

		// false로 빠져나오면 충돌이 생긴다는 뜻
		Rectangle bounds = null;
		int currentMargin = margin;

		// margin을 늘려가며, 8방향중 놓을 만한 곳이 있나 탐색
		while (currentMargin <= 300) { // margin 한계 (300)까지 탐색
			// findEmptySpace2를 호출하여 해당 마진에서 사각형 충돌이 없는 16방향 중 첫 번째를 찾음
			bounds = findEmptySpace2(iconX, iconY, popupWidth, popupHeight, currentMargin, panelWidth, panelHeight);
			if (bounds != null) {
				return bounds; // 빈 공간을 찾으면 즉시 반환
			}
			currentMargin += 5; // 다음 마진 증가
		}

		return bounds;
	}

	/**
	 * @brief 지도 우측 상단에 이용 가이드 문구를 표시합니다.
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

	/**
	 * @brief 좌측 최상단에 고정된 정보 아이콘 (동그라미 내부에 'i')을 그립니다.
	 * @param g2 렌더링할 Graphics2D 객체
	 */
	private void drawInfoIcon(Graphics2D g2) {
	    // 1. 아이콘 크기 및 위치 고정 (맵 크기 변화와 무관)
	    final int ICON_SIZE = 30; // 아이콘의 지름 (px)
	    final int PADDING = 10;   // 좌측 및 상단 여백 (px)

	    int x = PADDING;
	    int y = PADDING;

	    // 폰트 설정
	    Font originalFont = g2.getFont(); // 원래 폰트 저장
	    Font iconFont = new Font("Arial", Font.BOLD, ICON_SIZE - 10); // 'i' 글자 크기 조정

	    // 렌더링 힌트 (텍스트 부드럽게)
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	    // 2. 동그라미 그리기
	    g2.setColor(new Color(60, 140, 220)); // 파란색 계열
	    g2.fillOval(x, y, ICON_SIZE, ICON_SIZE); // 채워진 동그라미

	    // 3. 'i' 문자 그리기
	    g2.setColor(Color.WHITE); // 흰색 글자
	    g2.setFont(iconFont);

	    // 텍스트 중앙 정렬을 위한 메트릭 계산
	    FontMetrics fm = g2.getFontMetrics(iconFont);
	    String infoText = "i";
	    int textWidth = fm.stringWidth(infoText);
	    int textHeight = fm.getHeight();

	    // 텍스트 위치 계산 (동그라미 중앙)
	    int textX = x + (ICON_SIZE - textWidth) / 2;
	    int textY = y + (ICON_SIZE - textHeight) / 2 + fm.getAscent(); // baseline 조정

	    g2.drawString(infoText, textX, textY);

	    // 4. 원래 폰트 및 렌더링 힌트 복원
	    g2.setFont(originalFont);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	// =======================================================================
	// --- 4. Setup Methods ---
	// =======================================================================

	private void setupMouseClickListener() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleIconClick(e.getX(), e.getY());
				System.out.println(e.getX() + ", " + e.getY());
			}
		});
	}

	private void handleIconClick(int mouseX, int mouseY) {
		int currentMapWidth = mapLabel.getWidth();
		int originalMapWidth = originalImage != null ? originalImage.getWidth(null) : 1;
		double scaleRatio = (double) currentMapWidth / originalMapWidth;

		final int x = INFO_ICON_PADDING;
	    final int y = INFO_ICON_PADDING;
	    final int size = INFO_ICON_SIZE;
	    Rectangle infoIconBounds = new Rectangle(x, y, size, size);

	 // [4. 수정] 저장된 팝업 Hitbox 리스트를 확인하여 클릭 처리
        if (showFacilities) {
            // 거꾸로 탐색 (화면 맨 위에 그려진 팝업부터 확인하기 위함)
            for (int i = currentPopupHitboxes.size() - 1; i >= 0; i--) {
                PopupHitbox hitbox = currentPopupHitboxes.get(i);

                // 마우스 클릭 위치가 저장된 팝업 영역 안에 있는지 확인
                if (hitbox.bounds.contains(mouseX, mouseY)) {
                    System.out.println("[MapPanel] 시설 팝업 클릭됨: " + hitbox.facility.getName());

                    // ★ 상세 페이지 이동 메서드 호출 ★
                    mapController.onFacilityClicked(hitbox.facility);
                    return; // 찾았으면 메서드 종료
                }
            }
        }

		// 1. 모든 시설 팝업 클릭 확인
		if (showFacilities) {
			for (int i = facilityList.size() - 1; i >= 0; i--) {
				Facility f = facilityList.get(i);
				int iconX = mapX + (int) (f.getxLocation() * scaleRatio);
				int iconY = mapY + (int) (f.getyLocation() * scaleRatio);

				// [수정됨] 패널 크기 전달
				Rectangle bounds = calculatePopupBounds(iconX, iconY, getWidth(), getHeight());

				//calculatePopupBounds에서 bounds가 null이 될 수도 있음. null 체크
				if (bounds != null && bounds.contains(mouseX, mouseY)) {
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

				if (mouseX >= cx - radius && mouseX <= cx + radius && mouseY >= cy - radius && mouseY <= cy + radius) {

					System.out.println("[MapPanel] 시설 아이콘 클릭됨: " + f.getName());
					mapController.onFacilityClicked(f);
					return;
				}
			}
		}

		// 3. 건물 아이콘 클릭 확인
		int radius = buildingIconSize / 2;
		for (Building b : buildingList) {
			int cx = mapX + (int) (b.getxLocation() * scaleRatio);
			int cy = mapY + (int) (b.getyLocation() * scaleRatio);

			if (mouseX >= cx - radius && mouseX <= cx + radius && mouseY >= cy - radius && mouseY <= cy + radius) {

				mapController.onBuildingClicked(b);
				return;
			}
		}


		// 4. AppInfo 아이콘 클릭 확인
		if (infoIconBounds.contains(mouseX, mouseY)) {
	        System.out.println("[MapPanel] 앱 정보 아이콘 클릭됨.");
	        mapController.onAppInfoClicked();
	        return; // 클릭 이벤트 처리 완료 후 종료
	    }
	}

	private void setupMouseWheelListener() {
		this.addMouseWheelListener(e -> {
			if (flagScrol++ >= scrollSensitivity) {
				flagScrol = 0;
				int mouseX = e.getX();
				int mouseY = e.getY();

				double prevZoom = zoomLevel;
				if (e.getWheelRotation() < 0) {
					zoomLevel *= 1.4;
				} else {
					zoomLevel /= 1.4;
				}

				if (zoomLevel < 1.0) {
					zoomLevel = 1.0;
				} else if (zoomLevel > 2.5) {
					zoomLevel = 2.5;
				}
				if (zoomLevel == prevZoom) {
					return;
				}

				setSizeMap(mouseX, mouseY);
				setMaxVisiblePopus();
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

		buildingIconSize = (int)(DEFAULT_BUILDING_ICON_SIZE * zoomLevel);

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

		this.selectedFacility = facility;
	}

	public void setZoomLevel(double level) {
		this.zoomLevel = level;
		checkVisibilityMode();
	}

	public void setMaxVisiblePopus() {
		maxVisiblePopus = (int) (zoomLevel * 1.5) + 4;
		buildingIconSize = (int) (DEFAULT_BUILDING_ICON_SIZE * zoomLevel);
	}

	private boolean checkUnUsableSpace(int x, int y, int popupWidth, int popupHeight, int panelWidth, int panelHeight) {
		// 1. 화면 경계 체크 (가장 먼저 실행)
		if (x < 0 || y < 0 || x + popupWidth > panelWidth || y + popupHeight > panelHeight) {
			return false; // 화면 경계를 벗어남
		}

		for (Facility f : facilityList) {
			if (f.getCurrentX() <= x + popupWidth && x <= f.getCurrentX() + FACILITY_ICON_SIZE) {
				if (f.getCurrentY() <= y + popupHeight && y <= f.getCurrentY() + FACILITY_ICON_SIZE) {
					return false;
				}
			}
		}

		for (Building b : buildingList) {
			if (b.getCurrentX() <= x + popupWidth && x <= b.getCurrentX() + buildingIconSize) {
				if (b.getCurrentY() <= y + popupHeight && y <= b.getCurrentY() + buildingIconSize) {
					return false;
				}
			}
		}
		for (Rectangle d : drawnPopupBounds) {
			if (d.x - BLANK_SPACE <= x + popupWidth + BLANK_SPACE && x - BLANK_SPACE <= d.x + d.width + BLANK_SPACE) {
				if (d.y - BLANK_SPACE <= y + popupHeight + BLANK_SPACE && y - BLANK_SPACE <= d.y + d.height + BLANK_SPACE) {
					return false;
				}
			}
		}
		return true;
	}

	private Rectangle findEmptySpace2(int iconX, int iconY, int popupWidth, int popupHeight, int margin, int panelWidth,
			int panelHeight) {
		Rectangle r = new Rectangle(0, 0, popupWidth, popupHeight);

		// 기존 45도 대각선 상수
		final double DIAGONAL_FACTOR = 1.0 / Math.sqrt(2.0);
		int diagOffset = (int) Math.round(margin * DIAGONAL_FACTOR);

		// 22.5도, 67.5도 방향에 사용되는 새로운 상수
		final double FACTOR_A = 0.9239; // cos(22.5) ≈ 0.9239
		final double FACTOR_B = 0.3827; // sin(22.5) ≈ 0.3827

		// A, B 오프셋 계산 (int로 변환)
		int offsetA = (int) Math.round(margin * FACTOR_A);
		int offsetB = (int) Math.round(margin * FACTOR_B);

		// -----------------------------------------------------------------------
		// 1~8. 기존 8방향 탐색 (수평/수직/45도 대각선) - 코드 동일
		// -----------------------------------------------------------------------
		// 1. 우 (East)
		r.x = iconX + margin;
		r.y = iconY - (r.height / 2);
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 2. 좌 (West)
		r.x = iconX - margin - r.width;
		r.y = iconY - (r.height / 2);
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 3. 하 (South)
		r.x = iconX - (r.width / 2);
		r.y = iconY + margin;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 4. 상 (North)
		r.x = iconX - (r.width / 2);
		r.y = iconY - margin - r.height;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 5. 우하 (Southeast)
		r.x = iconX + diagOffset;
		r.y = iconY + diagOffset;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 6. 좌하 (Southwest)
		r.x = iconX - diagOffset - r.width;
		r.y = iconY + diagOffset;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 7. 우상 (Northeast)
		r.x = iconX + diagOffset;
		r.y = iconY - diagOffset - r.height;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 8. 좌상 (Northwest)
		r.x = iconX - diagOffset - r.width;
		r.y = iconY - diagOffset - r.height;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// -----------------------------------------------------------------------
		// 9~16. 추가된 8개 방향 탐색 (22.5도, 67.5도 계열)
		// -----------------------------------------------------------------------

		// 9. 동남동 (ESE: 22.5도)
		r.x = iconX + offsetA;
		r.y = iconY + offsetB;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 10. 북동동 (ENE: 337.5도)
		r.x = iconX + offsetA;
		r.y = iconY - offsetB - r.height;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 11. 남남동 (SSE: 67.5도)
		r.x = iconX + offsetB;
		r.y = iconY + offsetA;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 12. 북북동 (NNE: 292.5도)
		r.x = iconX + offsetB;
		r.y = iconY - offsetA - r.height;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 13. 서남서 (WSW: 202.5도)
		r.x = iconX - offsetA - r.width;
		r.y = iconY + offsetB;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 14. 북서서 (WNW: 247.5도)
		r.x = iconX - offsetA - r.width;
		r.y = iconY - offsetB - r.height;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 15. 남남서 (SSW: 157.5도)
		r.x = iconX - offsetB - r.width;
		r.y = iconY + offsetA;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// 16. 북북서 (NNW: 112.5도)
		r.x = iconX - offsetB - r.width;
		r.y = iconY - offsetA - r.height;
		if (checkUnUsableSpace(r.x, r.y, r.width, r.height, panelWidth, panelHeight)) {
			return r;
		}

		// -----------------------------------------------------------------------
		// 16개 방향 모두 충돌 발생 시
		return null;
	}

	//맵의 중앙 범위를 리턴함
	public Rectangle getCenterOneThirdBounds() {
		final double ratio = 2.0; // 이 변수만 변경하여 비율을 조절합니다.

	    int panelWidth = getWidth();
	    int panelHeight = getHeight();

	    // 중앙 영역의 가로/세로 길이
	    // 예: PanelWidth / 3.0
	    int centerWidth = (int) (panelWidth / ratio);
	    int centerHeight = (int) (panelHeight / ratio);

	    // 중앙 영역의 시작점 좌표
	    // (PanelWidth - CenterWidth) / 2
	    // = (PanelWidth - (PanelWidth / ratio)) / 2
	    // = (PanelWidth * (1 - 1/ratio)) / 2
	    int xStart = (panelWidth - centerWidth) / 2;
	    int yStart = (panelHeight - centerHeight) / 2;

	    // 3. Rectangle 객체 생성 및 반환
	    return new Rectangle(xStart, yStart, centerWidth, centerHeight);
	}

}