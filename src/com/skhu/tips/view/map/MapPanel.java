// 김준
package com.skhu.tips.view.map;

import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @class MapPanel
 * @brief 지도 이미지 표시 + 마우스 기준 휠 줌 + 드래그 이동 기능이 포함된 패널
 * * 이 클래스는 캠퍼스 지도를 보여주는 핵심 UI입니다.
 * 사용자는 마우스 휠로 지도를 확대/축소할 수 있고, 드래그하여 이동할 수 있습니다.
 * 확대 레벨에 따라 '시설 아이콘' 표시 모드가 자동으로 변경됩니다.
 */
public class MapPanel extends JPanel {

    // =======================================================================
    // --- 1. Fields & Constants (상수 및 변수) ---
    // =======================================================================

    // 파일 경로 및 설정 상수
    private static final String MAP_IMAGE_PATH = "src/resources/images/map/campus_map.jpg";
    private static final double FACILITY_VISIBLE_THRESHOLD = 1.3; // 시설 아이콘 표시 기준 배율

    // UI 컴포넌트 및 리소스
    private JLabel mapLabel;
    private Image originalImage;

    // 상태 변수
    private double zoomLevel = 1.0;
    private boolean showFacilities = false;

    // 좌표 변수 (드래그 및 계산용)
    private int mapX = 0;
    private int mapY = 0;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    // =======================================================================
    // --- 2. Constructor & Public API (생성자 및 공개 메소드) ---
    // =======================================================================

    /**
     * @brief MapPanel 생성자
     * * 패널을 초기화하고 이미지를 로드하며, 각종 마우스/창 크기 이벤트 리스너를 등록합니다.
     * 드래그 이동을 자유롭게 하기 위해 Layout Manager를 null로 설정합니다.
     */
    public MapPanel() {
        super(null); // 드래그 이동을 위해 레이아웃 매니저 제거 (Absolute Positioning)

        // 1. 데이터 로드 및 초기화
        loadOriginalMapImage();
        mapLabel = new JLabel();
        mapLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(mapLabel);

        // 2. 리스너 등록 (복잡한 코드는 private 메소드로 분리하여 가독성 향상)
        setupMouseWheelListener();
        setupMouseDragListener();
        setupResizeListener();

        // 3. 초기 화면 그리기
        resizeAndRepaintMap();
    }

    /**
     * @brief 현재 시설 아이콘 표시 모드가 켜져 있는지 확인합니다.
     * * 외부(MapController 등)에서 현재 지도가 확대되어 시설 아이콘을
     * 보여줘야 하는 상태인지 확인할 때 사용합니다.
     * * @return true면 시설 아이콘 표시, false면 숨김
     */
    public boolean isShowFacilities() {
        return showFacilities;
    }

    // =======================================================================
    // --- 3. Setup Methods (리스너 설정 - 생성자에서 호출) ---
    // =======================================================================

    /**
     * @brief 마우스 휠 리스너를 설정합니다. (줌 기능)
     * * 마우스 포인터 위치를 기준으로 확대/축소하는 고급 줌 로직이 구현되어 있습니다.
     * 휠을 굴릴 때 배율(zoomLevel)을 변경하고, 마우스 위치가 유지되도록 좌표를 보정합니다.
     */
    private void setupMouseWheelListener() {
        this.addMouseWheelListener(e -> {
            if (originalImage == null) {
				return;
			}

            // [줌 로직 1] 현재 마우스 위치와 지도의 상대적 위치 비율 계산
            int mouseX = e.getX();
            int mouseY = e.getY();
            int currentMapX = mapLabel.getX();
            int currentMapY = mapLabel.getY();
            int currentMapW = mapLabel.getWidth();
            int currentMapH = mapLabel.getHeight();

            // 마우스가 지도 내에서 가로/세로 몇 % 지점에 있는지 (0.0 ~ 1.0)
            double relX = (double) (mouseX - currentMapX) / currentMapW;
            double relY = (double) (mouseY - currentMapY) / currentMapH;

            // [줌 로직 2] 줌 레벨 변경
            double prevZoom = zoomLevel;
            if (e.getWheelRotation() < 0) {
                zoomLevel *= 1.1; // 확대 (10% 증가)
            } else {
                zoomLevel /= 1.1; // 축소 (10% 감소)
            }

            // 최소 배율 제한 (1.0 미만으로 축소되지 않도록 함)
            if (zoomLevel < 1.0) {
				zoomLevel = 1.0;
			}

            // 배율 변화가 없으면 리턴 (불필요한 연산 방지)
            if (zoomLevel == prevZoom) {
				return;
			}

            // [줌 로직 3] 새 크기 계산 및 위치 보정
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int origW = originalImage.getWidth(null);
            int origH = originalImage.getHeight(null);

            // 기본 화면 비율 계산
            double baseRatio = Math.min((double) panelWidth / origW, (double) panelHeight / origH);
            double newScale = baseRatio * zoomLevel;

            int newMapW = (int) (origW * newScale);
            int newMapH = (int) (origH * newScale);

            // 마우스 포인터 위치 유지 (핵심: 상대 비율을 이용해 새 좌표 역산)
            mapX = (int) (mouseX - (relX * newMapW));
            mapY = (int) (mouseY - (relY * newMapH));

            // [줌 로직 4] 적용
            checkVisibilityMode(); // 줌 레벨에 따른 아이콘 표시 모드 체크
            resizeAndRepaintMap(); // 화면 갱신
        });
    }

    /**
     * @brief 마우스 드래그 리스너를 설정합니다. (이동 기능)
     * * 마우스를 누른 좌표를 기억하고, 드래그한 만큼 지도를 이동(Panning)시킵니다.
     */
    private void setupMouseDragListener() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 클릭 시점의 좌표 기억
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // 이동 거리 계산 (현재 위치 - 이전 위치)
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;

                // 맵 좌표 업데이트
                mapX += dx;
                mapY += dy;

                // 기준 좌표 갱신
                lastMouseX = e.getX();
                lastMouseY = e.getY();

                // 화면 이탈 방지 및 실제 이동 적용
                constrainMapPosition();
                mapLabel.setLocation(mapX, mapY);
            }
        };
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
    }

    /**
     * @brief 창 크기 변경 리스너를 설정합니다.
     * * 창 크기가 바뀔 때마다 지도를 화면 비율에 맞게 다시 계산하여 그립니다.
     */
    private void setupResizeListener() {
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                resizeAndRepaintMap();
            }
        });
    }

    // =======================================================================
    // --- 4. Core Logic (핵심 비즈니스 로직) ---
    // =======================================================================

    /**
     * @brief 현재 상태(패널 크기, 줌 레벨, 좌표)에 맞춰 지도를 화면에 다시 그립니다.
     * * 1. 원본 이미지와 패널 크기를 비교하여 기본 비율(Base Ratio)을 계산합니다.
     * 2. 현재 줌 레벨을 반영하여 최종 이미지 크기를 결정합니다.
     * 3. 이미지를 고품질(Smooth)로 리사이즈하여 라벨에 설정합니다.
     * 4. 위치가 화면을 벗어나지 않도록 보정하고 적용합니다.
     */
    private void resizeAndRepaintMap() {
        if (originalImage == null || getWidth() <= 0 || getHeight() <= 0) {
			return;
		}

        // 1. 크기 계산
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int origW = originalImage.getWidth(null);
        int origH = originalImage.getHeight(null);

        // 화면에 꽉 차게 맞추는 기본 비율 (가로/세로 중 더 작은 쪽 기준)
        double baseRatio = Math.min((double) panelWidth / origW, (double) panelHeight / origH);
        // 줌 레벨을 반영한 최종 비율
        double finalRatio = baseRatio * zoomLevel;

        int newWidth = (int) (origW * finalRatio);
        int newHeight = (int) (origH * finalRatio);

        // 2. 이미지 리사이즈 (SCALE_SMOOTH: 부드러운 품질 유지)
        Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        mapLabel.setIcon(new ImageIcon(resizedImage));
        mapLabel.setSize(newWidth, newHeight);

        // 3. 위치 보정 및 적용
        constrainMapPosition();
        mapLabel.setLocation(mapX, mapY);

        repaint(); // 화면 강제 갱신
    }

    /**
     * @brief 지도가 화면 밖으로 이탈하지 않도록 좌표(mapX, mapY)를 제한합니다.
     * * - 지도가 패널보다 클 때: 지도의 끝부분이 패널 안쪽으로 들어오지 않도록 막습니다 (빈 공간 방지).
     * - 지도가 패널보다 작을 때: 항상 패널의 중앙에 위치하도록 강제합니다.
     */
    private void constrainMapPosition() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imgWidth = mapLabel.getWidth();
        int imgHeight = mapLabel.getHeight();

        // 가로(X) 제한
        if (imgWidth > panelWidth) {
            // 이미지가 화면보다 클 경우: 왼쪽/오른쪽 벽을 넘어가지 않게 제한
            if (mapX > 0) {
				mapX = 0;
			}
            if (mapX < panelWidth - imgWidth) {
				mapX = panelWidth - imgWidth;
			}
        } else {
            // 이미지가 화면보다 작을 경우: 중앙 정렬
            mapX = (panelWidth - imgWidth) / 2;
        }

        // 세로(Y) 제한
        if (imgHeight > panelHeight) {
            // 이미지가 화면보다 클 경우: 위/아래 벽을 넘어가지 않게 제한
            if (mapY > 0) {
				mapY = 0;
			}
            if (mapY < panelHeight - imgHeight) {
				mapY = panelHeight - imgHeight;
			}
        } else {
            // 이미지가 화면보다 작을 경우: 중앙 정렬
            mapY = (panelHeight - imgHeight) / 2;
        }
    }

    /**
     * @brief 줌 레벨에 따라 표시 모드(시설 아이콘 보이기/숨기기)를 전환합니다.
     * * 줌 레벨이 FACILITY_VISIBLE_THRESHOLD(1.3) 이상이면 시설 표시 모드를 켭니다.
     * 상태가 변경될 때 콘솔에 로그를 출력합니다.
     */
    private void checkVisibilityMode() {
        boolean previousState = showFacilities;

        if (zoomLevel >= FACILITY_VISIBLE_THRESHOLD) {
            showFacilities = true;
        } else {
            showFacilities = false;
        }

        // 상태 변경 시 로그 출력
        if (previousState != showFacilities) {
            System.out.println("[View] 시설 표시 모드: " + (showFacilities ? "ON" : "OFF"));
        }
    }

    // =======================================================================
    // --- 5. Helper Methods (단순 보조 기능) ---
    // =======================================================================

    /**
     * @brief 파일 시스템에서 원본 지도 이미지를 로드합니다.
     * * 초기화 시 한 번만 호출됩니다. 로드 실패 시 에러 메시지를 출력합니다.
     */
    private void loadOriginalMapImage() {
        try {
            File file = new File(MAP_IMAGE_PATH);
            if (file.exists()) {
                originalImage = ImageIO.read(file);
            } else {
                System.err.println("[MapPanel] 이미지 파일을 찾을 수 없습니다: " + MAP_IMAGE_PATH);
            }
        } catch (IOException e) {
            originalImage = null;
            System.err.println("[MapPanel] 이미지 로드 실패: " + e.getMessage());
        }
    }
}