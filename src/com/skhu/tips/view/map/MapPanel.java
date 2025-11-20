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

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;

/**
 * @class MapPanel
 * @brief 지도 표시 + 줌/이동 + 건물/시설 아이콘 그리기 기능 포함
 */
public class MapPanel extends JPanel {

    // =======================================================================
    // --- 1. Fields & Constants ---
    // =======================================================================

    private static final String MAP_IMAGE_PATH = "src/resources/images/map/campus_map.jpg";
    private static final double FACILITY_VISIBLE_THRESHOLD = 1.3;

    // UI 컴포넌트
    private JLabel mapLabel;
    private Image originalImage;

    // 데이터 (건물 및 시설 리스트)
    private List<Building> buildingList = new ArrayList<>();
    private List<Facility> facilityList = new ArrayList<>();

    // 상태 변수
    private double zoomLevel = 1.0;
    private boolean showFacilities = false;

    // 좌표 변수
    private int mapX = 0;
    private int mapY = 0;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    // 아이콘 스타일 상수
    private static final int BUILDING_ICON_SIZE = 30; // 건물 아이콘 크기
    private static final int FACILITY_ICON_SIZE = 14; // 시설 아이콘 크기
    private static final Color COLOR_BUILDING = new Color(65, 105, 225); // 로얄 블루
    private static final Color COLOR_FACILITY = new Color(255, 105, 180); // 핫 핑크

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
        setupResizeListener();

        resizeAndRepaintMap();
    }

    /**
     * @brief 컨트롤러로부터 건물 및 시설 데이터를 전달받습니다.
     */
    public void setMapData(List<Building> buildings, List<Facility> facilities) {
        this.buildingList = buildings;
        this.facilityList = facilities;
        repaint(); // 데이터가 들어오면 화면을 다시 그립니다.
    }

    public boolean isShowFacilities() {
        return showFacilities;
    }

    // =======================================================================
    // --- 3. Painting Logic (아이콘 그리기 핵심) ---
    // =======================================================================

    /**
     * @brief 자식 컴포넌트(지도 이미지)를 그린 후, 그 위에 아이콘을 덧그립니다.
     */
    @Override
    protected void paintChildren(Graphics g) {
        // 1. 먼저 지도 이미지(JLabel)를 그립니다.
        super.paintChildren(g);

        // 2. 데이터나 이미지가 없으면 그리지 않습니다.
        if (originalImage == null || buildingList == null) {
			return;
		}

        // 3. 고품질 그래픽 설정 (계단현상 제거)
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 4. 아이콘 그리기
        drawIcons(g2);
    }

    /**
     * @brief 현재 지도 위치와 배율에 맞춰 아이콘 좌표를 계산하고 그립니다.
     */
    private void drawIcons(Graphics2D g2) {
        int currentMapWidth = mapLabel.getWidth();
        int originalMapWidth = originalImage.getWidth(null);

        // 현재 확대 비율 계산 (현재 지도 너비 / 원본 지도 너비)
        // 아이콘 위치 = 지도 시작점(mapX, mapY) + (데이터 좌표 * 비율)
        double scaleRatio = (double) currentMapWidth / originalMapWidth;

        // --- [A] 시설 아이콘 그리기 (조건: 확대되었을 때만) ---
        if (showFacilities && facilityList != null) {
            g2.setColor(COLOR_FACILITY);
            for (Facility f : facilityList) {
                // 좌표 계산 (데이터 좌표를 픽셀로 가정)
                int x = mapX + (int)(f.getxLocation() * scaleRatio);
                int y = mapY + (int)(f.getyLocation() * scaleRatio);

                // 작은 핑크색 원 그리기 (중심 기준)
                int offset = FACILITY_ICON_SIZE / 2;
                g2.fillOval(x - offset, y - offset, FACILITY_ICON_SIZE, FACILITY_ICON_SIZE);

                // 테두리 (선택 사항)
                g2.setColor(Color.WHITE);
                g2.drawOval(x - offset, y - offset, FACILITY_ICON_SIZE, FACILITY_ICON_SIZE);
                g2.setColor(COLOR_FACILITY); // 색상 복구
            }
        }

        // --- [B] 건물 아이콘 그리기 (항상 표시) ---
        for (Building b : buildingList) {
            g2.setColor(COLOR_BUILDING);

            // 좌표 계산
            int x = mapX + (int)(b.getxLocation() * scaleRatio);
            int y = mapY + (int)(b.getyLocation() * scaleRatio);

            // 1. 네모난 아이콘 그리기 (중심 기준)
            int offset = BUILDING_ICON_SIZE / 2;
            g2.fillRoundRect(x - offset, y - offset, BUILDING_ICON_SIZE, BUILDING_ICON_SIZE, 10, 10);

            // 2. 테두리 그리기
            g2.setColor(Color.WHITE);
            g2.setStroke(new java.awt.BasicStroke(2));
            g2.drawRoundRect(x - offset, y - offset, BUILDING_ICON_SIZE, BUILDING_ICON_SIZE, 10, 10);

            // 3. 숫자(ID 뒷자리) 그리기
            String idText = getFormattedId(b.getId());
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));

            // 글자 중앙 정렬 계산
            FontMetrics fm = g2.getFontMetrics();
            int textX = x - (fm.stringWidth(idText) / 2);
            int textY = y + (fm.getAscent() - fm.getDescent() + fm.getLeading()) / 2;

            g2.drawString(idText, textX, textY);
        }
    }

    /**
     * @brief ID를 사람이 보기 편한 두 자리 숫자 문자열로 변환합니다.
     * 예: 1001 -> "01", 5 -> "05"
     */
    private String getFormattedId(int id) {
        int shortId = id % 100; // 뒤 2자리만 추출
        return String.format("%02d", shortId); // 항상 2자리 문자열로 포맷팅 (01, 05, 12 등)
    }

    // =======================================================================
    // --- 4. Setup Methods (리스너) ---
    // =======================================================================

    private void setupMouseWheelListener() {
        this.addMouseWheelListener(e -> {
            if (originalImage == null) {
				return;
			}

            int mouseX = e.getX();
            int mouseY = e.getY();
            int currentMapX = mapLabel.getX();
            int currentMapY = mapLabel.getY();
            int currentMapW = mapLabel.getWidth();
            int currentMapH = mapLabel.getHeight();

            double relX = (double) (mouseX - currentMapX) / currentMapW;
            double relY = (double) (mouseY - currentMapY) / currentMapH;

            double prevZoom = zoomLevel;
            if (e.getWheelRotation() < 0) {
				zoomLevel *= 1.1;
			} else {
				zoomLevel /= 1.1;
			}

            if (zoomLevel < 1.0) {
				zoomLevel = 1.0;
			}
            if (zoomLevel == prevZoom) {
				return;
			}

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
                repaint(); // 아이콘 위치도 갱신되어야 하므로 repaint 필요
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
    // --- 5. Core Logic (리사이즈, 위치제한, 모드체크) ---
    // =======================================================================

    private void resizeAndRepaintMap() {
        if (originalImage == null || getWidth() <= 0 || getHeight() <= 0) {
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
            repaint(); // 상태가 바뀌면 즉시 다시 그려야 함
        }
    }

    private void loadOriginalMapImage() {
        try {
            File file = new File(MAP_IMAGE_PATH);
            if (file.exists()) {
				originalImage = ImageIO.read(file);
			}
        } catch (IOException e) {
            originalImage = null;
        }
    }
}