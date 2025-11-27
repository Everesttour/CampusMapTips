package com.skhu.tips.controller;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;
import com.skhu.tips.model.service.DataService;
import com.skhu.tips.view.panel.AppInfoPanel;
import com.skhu.tips.view.panel.BuildingDetailPanel;
import com.skhu.tips.view.panel.FacilityDetailPanel;
import com.skhu.tips.view.panel.MainLeftPanel;

/**
 * @class PanelControllerImpl
 * @brief 뷰의 컴포넌트에 직접 리스너를 등록합니다.
 */
public class PanelControllerImpl implements PanelController {

    private MainLeftPanel mainLeftPanel;
    private DataService dataService;
    private MapController mapController;

    // 디테일 패널들
    private BuildingDetailPanel buildingDetailPanel;
    private FacilityDetailPanel facilityDetailPanel;
    private AppInfoPanel appInfoPanel;

    // 디테일 패널 컨테이너 (JLayeredPane에 추가될 패널)
    private JPanel buildingDetailContainer;
    private JPanel facilityDetailContainer;
    private JPanel appInfoContainer;

    // 이미지 라벨 (패널과 분리)
    private JLabel buildingImageLabel;
    private JLabel facilityImageLabel;

    // 이미지 원본 비율 저장 (너비 계산용)
    private double buildingImageAspectRatio = 1.0;
    private double facilityImageAspectRatio = 1.0;

    // 부모 프레임 참조
    private JFrame parentFrame;

    /**
     * @brief 기본 생성자: configure()에서 의존성 주입
     */
    public PanelControllerImpl() {
        // configure()에서 초기화됨
    }
    // =======================================================================
    // --- PanelController Interface Implementation ---
    // =======================================================================

    /**
     * @brief 1단계: 의존성 주입 (Wiring)
     */
    @Override
    public void configure(MainLeftPanel mainLeftPanel, DataService dataService, MapController mapController) {
        this.mainLeftPanel = mainLeftPanel;
        this.dataService = dataService;
        this.mapController = mapController;

        // 디테일 패널 초기화
        buildingDetailPanel = new BuildingDetailPanel();
        facilityDetailPanel = new FacilityDetailPanel();
        appInfoPanel = new AppInfoPanel();
    }

    /**
     * @brief 2단계: 초기 데이터 로딩 및 리스너 등록
     */
    @Override
    public void loadInitialData() {
        // 뷰에 데이터 채우기
        mainLeftPanel.setBuildingListData(dataService.getBuildings());
        mainLeftPanel.setFacilityListData(dataService.getFacilities());

        // 뷰의 컴포넌트에 직접 리스너를 등록
        attachViewListeners();

        // 초기 뷰 상태 설정
        switchToBuildingView();
    }

    /**
     * @brief 뷰(MainLeftPanel)의 스윙 컴포넌트에 이벤트 리스너를 등록합니다.
     */
    private void attachViewListeners() {

        // 1. 건물 리스트(JList)에 리스너 등록
        mainLeftPanel.getBuildingList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Building selected = mainLeftPanel.getBuildingList().getSelectedValue();
                if (selected != null) {
                    mapController.focusOn(selected);
                    openBuildingDetail(selected);
                }
            }
        });

        mainLeftPanel.getBuildingList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // 단일 클릭
                    int index = mainLeftPanel.getBuildingList().locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Building selected = mainLeftPanel.getBuildingList().getModel().getElementAt(index);
                        if (selected != null) {
                            mainLeftPanel.getBuildingList().setSelectedIndex(index);
                            mapController.focusOn(selected);
                            openBuildingDetail(selected);
                        }
                    }
                }
            }
        });

        // 2. 시설 리스트(JList)에도 리스너 등록
        mainLeftPanel.getFacilityList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Facility selected = mainLeftPanel.getFacilityList().getSelectedValue();
                if (selected != null) {
                    mapController.focusOn(selected);
                    openFacilityDetail(selected);
                }
            }
        });

        mainLeftPanel.getFacilityList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // 단일 클릭
                    int index = mainLeftPanel.getFacilityList().locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Facility selected = mainLeftPanel.getFacilityList().getModel().getElementAt(index);
                        if (selected != null) {
                            mainLeftPanel.getFacilityList().setSelectedIndex(index);
                            mapController.focusOn(selected);
                            openFacilityDetail(selected);
                        }
                    }
                }
            }
        });

        // 3. 카테고리 버튼 클릭 리스너 등록
        mainLeftPanel.getBuildingButton().addActionListener(e -> switchToBuildingView());
        mainLeftPanel.getFacilityButton().addActionListener(e -> switchToFacilityView());
    }


    // --- PanelController 인터페이스 구현 ---

    @Override
    public void switchToBuildingView() {
        mainLeftPanel.switchToBuildingView();
        if (mapController != null) {
            mapController.switchToBuildingView();
        }
    }

    @Override
    public void switchToFacilityView() {
        mainLeftPanel.switchToFacilityView();
        if (mapController != null) {
            mapController.switchToFacilityView();
        }
    }

    @Override
    public void openBuildingDetail(Building building) {
        if (building == null) {
            System.out.println("openBuildingDetail: building이 null입니다.");
            return;
        }

        System.out.println("openBuildingDetail 호출됨: " + building.getName());

        // 패널에 건물 정보 표시
        buildingDetailPanel.displayBuilding(building);

        // [수정됨] 패널의 X버튼을 누르면 창이 닫히도록 리스너 연결
        buildingDetailPanel.setOnCloseListener(this::closeBuildingDetail);

        // 부모 프레임 찾기
        if (parentFrame == null) {
            parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(mainLeftPanel);
            // 창 크기 변경 리스너 등록 (한 번만)
            setupResizeListener();
        }

        // 컨테이너가 없거나 보이지 않으면 새로 생성
        if (buildingDetailContainer == null || !buildingDetailContainer.isVisible()) {
            createBuildingDetailContainer();
        }

        // 이미지 로드
        loadBuildingImage(building);

        // 배경 어둡게 만들기
        setBackgroundDim(parentFrame, true, buildingDetailContainer);

        // 컨테이너 표시 및 레이어 순서 조정
        buildingDetailContainer.setVisible(true);
        JComponent glassPane = (JComponent) parentFrame.getGlassPane();
        glassPane.setComponentZOrder(buildingDetailContainer, 0); // 가장 위로
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    /**
     * 건물 상세 정보 컨테이너 생성
     */
    private void createBuildingDetailContainer() {
        if (parentFrame == null) {
			return;
		}

        buildingDetailContainer = new JPanel(null);
        buildingDetailContainer.setOpaque(false);
        buildingDetailContainer.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

        // 이미지 라벨 생성 및 좌측에 배치
        buildingImageLabel = new JLabel();
        buildingImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        buildingImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        buildingImageLabel.setPreferredSize(new java.awt.Dimension(200, 300));
        buildingImageLabel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.GRAY, 1));
        buildingImageLabel.setOpaque(true);
        buildingImageLabel.setBackground(Color.WHITE);

        // 패널과 이미지 배치 계산
        int panelWidth = 900;
        int panelHeight = 600;
        int imageHeight = 350; // 이미지 높이 (패널 높이와 독립적)
        int imageWidth = (int) (imageHeight * buildingImageAspectRatio); // 원본 비율에 맞게 너비 계산
        int gap = 100; // 이미지와 패널 사이 간격
        int totalWidth = imageWidth + gap + panelWidth;
        int x = (parentFrame.getWidth() - totalWidth) / 2;
        int panelY = (parentFrame.getHeight() - panelHeight) / 2;
        int imageY = (parentFrame.getHeight() - imageHeight) / 2; // 이미지 중앙 정렬

        // 이미지 좌측 배치
        buildingImageLabel.setBounds(x, imageY, imageWidth, imageHeight);
        buildingDetailContainer.add(buildingImageLabel);

        // 패널 우측 배치 (이미지와 100px 간격)
        buildingDetailPanel.setBounds(x + imageWidth + gap, panelY, panelWidth, panelHeight);
        buildingDetailContainer.add(buildingDetailPanel);

        // detailPanel이 마우스 이벤트를 소비하도록 설정 (패널 클릭 시 통과 방지)
        buildingDetailPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume(); // 이벤트 소비
            }
        });

        // 이미지 라벨도 이벤트 소비
        buildingImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume(); // 이벤트 소비
            }
        });

        // [수정됨] 배경 클릭 시 닫기 기능 주석 처리 (제거)
        /*
        buildingDetailContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                java.awt.Point point = e.getPoint();
                // 패널이나 이미지 영역이 아닌 곳을 클릭한 경우
                if (!buildingDetailPanel.getBounds().contains(point) &&
                    !buildingImageLabel.getBounds().contains(point)) {
                    closeBuildingDetail();
                }
            }
        });
        */

        // GlassPane에 추가 (dimLayer 위에 배치)
        JComponent glassPane = (JComponent) parentFrame.getGlassPane();
        glassPane.setLayout(null);
        glassPane.add(buildingDetailContainer);
        // detailContainer를 가장 위 레이어로 이동 (dimLayer 위)
        glassPane.setComponentZOrder(buildingDetailContainer, 0);
    }

    /**
     * 건물 상세 정보 닫기
     */
    private void closeBuildingDetail() {
        if (buildingDetailContainer != null) {
            buildingDetailContainer.setVisible(false);
            if (parentFrame != null && parentFrame.getGlassPane() != null) {
                JComponent glassPane = (JComponent) parentFrame.getGlassPane();
                glassPane.remove(buildingDetailContainer);
            }
            // 이미지 라벨 제거
            buildingImageLabel = null;
            setBackgroundDim(parentFrame, false, null);
        }
    }

    @Override
    public void openFacilityDetail(Facility facility) {
        if (facility == null) {
            System.out.println("openFacilityDetail: facility가 null입니다.");
            return;
        }

        System.out.println("openFacilityDetail 호출됨: " + facility.getName());

        // 패널에 시설 정보 표시
        facilityDetailPanel.displayFacility(facility);

        // [수정됨] 패널의 X버튼을 누르면 창이 닫히도록 리스너 연결
        facilityDetailPanel.setOnCloseListener(this::closeFacilityDetail);

        // 부모 프레임 찾기
        if (parentFrame == null) {
            parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(mainLeftPanel);
            // 창 크기 변경 리스너 등록 (한 번만)
            setupResizeListener();
        }

        // 컨테이너가 없거나 보이지 않으면 새로 생성
        if (facilityDetailContainer == null || !facilityDetailContainer.isVisible()) {
            createFacilityDetailContainer();
        }

        // 이미지 로드
        loadFacilityImage(facility);

        // 배경 어둡게 만들기
        setBackgroundDim(parentFrame, true, facilityDetailContainer);

        // 컨테이너 표시 및 레이어 순서 조정
        facilityDetailContainer.setVisible(true);
        JComponent glassPane = (JComponent) parentFrame.getGlassPane();
        glassPane.setComponentZOrder(facilityDetailContainer, 0); // 가장 위로
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    /**
     * 시설 상세 정보 컨테이너 생성
     */
    private void createFacilityDetailContainer() {
        if (parentFrame == null) {
			return;
		}

        facilityDetailContainer = new JPanel(null);
        facilityDetailContainer.setOpaque(false);
        facilityDetailContainer.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

        // 이미지 라벨 생성 및 좌측에 배치
        facilityImageLabel = new JLabel();
        facilityImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        facilityImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        facilityImageLabel.setPreferredSize(new java.awt.Dimension(200, 300));
        facilityImageLabel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.GRAY, 1));
        facilityImageLabel.setOpaque(true);
        facilityImageLabel.setBackground(Color.WHITE);

        // 패널과 이미지 배치 계산
        int panelWidth = 900;
        int panelHeight = 600;
        int imageHeight = 350; // 이미지 높이 (패널 높이와 독립적)
        int imageWidth = (int) (imageHeight * facilityImageAspectRatio); // 원본 비율에 맞게 너비 계산
        int gap = 100; // 이미지와 패널 사이 간격
        int totalWidth = imageWidth + gap + panelWidth;
        int x = (parentFrame.getWidth() - totalWidth) / 2;
        int panelY = (parentFrame.getHeight() - panelHeight) / 2;
        int imageY = (parentFrame.getHeight() - imageHeight) / 2; // 이미지 중앙 정렬

        // 이미지 좌측 배치
        facilityImageLabel.setBounds(x, imageY, imageWidth, imageHeight);
        facilityDetailContainer.add(facilityImageLabel);

        // 패널 우측 배치 (이미지와 100px 간격)
        facilityDetailPanel.setBounds(x + imageWidth + gap, panelY, panelWidth, panelHeight);
        facilityDetailContainer.add(facilityDetailPanel);

        // detailPanel이 마우스 이벤트를 소비하도록 설정 (패널 클릭 시 통과 방지)
        facilityDetailPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume(); // 이벤트 소비
            }
        });

        // 이미지 라벨도 이벤트 소비
        facilityImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume(); // 이벤트 소비
            }
        });

        // [수정됨] 배경 클릭 시 닫기 기능 주석 처리 (제거)
        /*
        facilityDetailContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                java.awt.Point point = e.getPoint();
                // 패널이나 이미지 영역이 아닌 곳을 클릭한 경우
                if (!facilityDetailPanel.getBounds().contains(point) &&
                    !facilityImageLabel.getBounds().contains(point)) {
                    closeFacilityDetail();
                }
            }
        });
        */

        // GlassPane에 추가 (dimLayer 위에 배치)
        JComponent glassPane = (JComponent) parentFrame.getGlassPane();
        glassPane.setLayout(null);
        glassPane.add(facilityDetailContainer);
        // detailContainer를 가장 위 레이어로 이동 (dimLayer 위)
        glassPane.setComponentZOrder(facilityDetailContainer, 0);
    }

    /**
     * 시설 상세 정보 닫기
     */
    private void closeFacilityDetail() {
        if (facilityDetailContainer != null) {
            facilityDetailContainer.setVisible(false);
            if (parentFrame != null && parentFrame.getGlassPane() != null) {
                JComponent glassPane = (JComponent) parentFrame.getGlassPane();
                glassPane.remove(facilityDetailContainer);
            }
            // 이미지 라벨 제거
            facilityImageLabel = null;
            setBackgroundDim(parentFrame, false, null);
        }
    }






    @Override
    public void openAppInfoDetail() {
        // 부모 프레임 찾기 (buildingDetailPanel과 동일한 로직)
        if (parentFrame == null) {
            parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(mainLeftPanel);
            setupResizeListener(); // 프레임 크기 변경 리스너 등록 (필요 시)
        }

        // 컨테이너가 없으면 새로 생성
        if (appInfoContainer == null || !appInfoContainer.isVisible()) {
            createAppInfoContainer();
        }

        // *필요에 따라* 다른 상세 패널이 열려있다면 닫는 로직 추가 가능
        // 예: closeBuildingDetail(); closeFacilityDetail();

        // 배경 어둡게 만들기 (dim Layer 표시)
        setBackgroundDim(parentFrame, true, appInfoContainer); //

        // 컨테이너 표시 및 레이어 순서 조정 (가장 위로)
        appInfoContainer.setVisible(true);
        JComponent glassPane = (JComponent) parentFrame.getGlassPane(); //
        glassPane.setComponentZOrder(appInfoContainer, 0);
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    private void createAppInfoContainer() {
        if (parentFrame == null) {
			return;
		}

        appInfoContainer = new JPanel(null); // null 레이아웃 사용
        appInfoContainer.setOpaque(false); // 투명 배경
        appInfoContainer.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

        // 패널 크기 및 위치 설정 (예시: BuildingDetailPanel의 패널 크기 900x600을 참고)
        int panelWidth = 800;
        int panelHeight = 650;

        // 화면 중앙 정렬 위치 계산
        int x = (parentFrame.getWidth() - panelWidth) / 2;
        int y = (parentFrame.getHeight() - panelHeight) / 2;

        // AppInfoPanel 배치
        appInfoPanel.setBounds(x, y, panelWidth, panelHeight);
        appInfoContainer.add(appInfoPanel);

        // 1. 패널 내부 클릭 시 이벤트 소비 (팝업창 닫힘 방지)
        appInfoPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }
        });

        // 2. 배경 클릭 시 닫기 (BuildingDetailContainer와 동일)
        // [참고] AppInfoPanel에 대해서는 별도 요청이 없어 기존 로직을 유지했습니다.
        // 만약 이것도 닫고 싶지 않으시면 아래 블록도 주석 처리하세요.
        appInfoContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                java.awt.Point point = e.getPoint();
                // AppInfoPanel 영역이 아닌 곳을 클릭한 경우
                if (!appInfoPanel.getBounds().contains(point)) {
                    closeAppInfoDetail();
                }
            }
        });

        // GlassPane에 추가
        JComponent glassPane = (JComponent) parentFrame.getGlassPane();
        glassPane.setLayout(null);
        glassPane.add(appInfoContainer);
    }


    /**
     * 앱 정보 팝업창을 닫습니다.
     */
    private void closeAppInfoDetail() {
        if (appInfoContainer != null && appInfoContainer.isVisible()) {
            appInfoContainer.setVisible(false);
            setBackgroundDim(parentFrame, false, appInfoContainer); // 배경 복원
        }
    }










    /**
     * 창 크기 변경 리스너 설정 (한 번만 등록)
     */
    private void setupResizeListener() {
        if (parentFrame == null) {
			return;
		}

        parentFrame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // dimLayer 크기 업데이트
                JComponent glassPane = (JComponent) parentFrame.getGlassPane();
                for (java.awt.Component comp : glassPane.getComponents()) {
                    if (comp instanceof JComponent && comp.getName() != null && comp.getName().equals("dimLayer")) {
                        comp.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());
                        comp.repaint();
                        break;
                    }
                }

                // detailContainer 크기 업데이트
                if (buildingDetailContainer != null && buildingDetailContainer.isVisible()) {
                    updateContainerSize(buildingDetailContainer, buildingDetailPanel);
                }
                if (facilityDetailContainer != null && facilityDetailContainer.isVisible()) {
                    updateContainerSize(facilityDetailContainer, facilityDetailPanel);
                }
                if (appInfoContainer != null && appInfoContainer.isVisible()) {
                    updateContainerSize(appInfoContainer, appInfoPanel);
                }
            }
        });
    }

    /**
     * 컨테이너와 패널 크기를 업데이트합니다.
     */
    private void updateContainerSize(JPanel container, JPanel detailPanel) {
        if (parentFrame == null || container == null || detailPanel == null) {
			return;
		}

        container.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

        // 패널과 이미지 배치 계산
        int panelWidth = 900;
        int panelHeight = 600;
        int imageHeight = 350; // 이미지 높이 (패널 높이와 독립적)
        int gap = 100; // 이미지와 패널 사이 간격

        // 이미지와 패널 위치 업데이트
        if (container == buildingDetailContainer && buildingImageLabel != null) {
            int imageWidth = (int) (imageHeight * buildingImageAspectRatio); // 원본 비율에 맞게 너비 계산
            int totalWidth = imageWidth + gap + panelWidth;
            int x = (parentFrame.getWidth() - totalWidth) / 2;
            int panelY = (parentFrame.getHeight() - panelHeight) / 2;
            int imageY = (parentFrame.getHeight() - imageHeight) / 2;

            buildingImageLabel.setBounds(x, imageY, imageWidth, imageHeight);
            detailPanel.setBounds(x + imageWidth + gap, panelY, panelWidth, panelHeight);
        } else if (container == facilityDetailContainer && facilityImageLabel != null) {
            int imageWidth = (int) (imageHeight * facilityImageAspectRatio); // 원본 비율에 맞게 너비 계산
            int totalWidth = imageWidth + gap + panelWidth;
            int x = (parentFrame.getWidth() - totalWidth) / 2;
            int panelY = (parentFrame.getHeight() - panelHeight) / 2;
            int imageY = (parentFrame.getHeight() - imageHeight) / 2;

            facilityImageLabel.setBounds(x, imageY, imageWidth, imageHeight);
            detailPanel.setBounds(x + imageWidth + gap, panelY, panelWidth, panelHeight);
        } else {
            // 이미지가 없는 경우 (기존 로직)
            int x = (parentFrame.getWidth() - panelWidth) / 2;
            int panelY = (parentFrame.getHeight() - panelHeight) / 2;
            detailPanel.setBounds(x, panelY, panelWidth, panelHeight);
        }

        container.revalidate();
        container.repaint();
    }

    /**
     * 건물 이미지를 비동기로 로드합니다.
     */
    private void loadBuildingImage(Building building) {
        if (building == null || buildingImageLabel == null) {
            if (buildingImageLabel != null) {
                buildingImageLabel.setIcon(null);
                buildingImageLabel.setText("이미지 없음");
            }
            return;
        }

        // 로딩 중 표시
        buildingImageLabel.setIcon(null);
        buildingImageLabel.setText("로딩 중...");

        // [수정] 리소스 경로를 /resources/... 로 시작하는 절대 경로로 통일
        // 대소문자 주의 (폴더명 정확히)
        String[] imagePaths = {
            "resources/images/Buildings/" + building.getId() + "_Building.jpg",
            "resources/images/buildings/" + building.getId() + "_Building.jpg",
            "resources/images/Buildings/" + building.getId() + "_Building.png",
            "resources/images/buildings/" + building.getId() + "_Building.png"
        };

        // SwingWorker를 사용하여 백그라운드에서 이미지 로드
        SwingWorker<ImageLoadResult, Void> worker = new SwingWorker<ImageLoadResult, Void>() {
            @Override
            protected ImageLoadResult doInBackground() throws Exception {
                ImageIcon icon = null;
                BufferedImage originalImage = null;
                double aspectRatio = 1.0;

                // 원본 이미지 로드하여 비율 계산
                for (String path : imagePaths) {
                    originalImage = loadOriginalImage(path);
                    if (originalImage != null) {
                        aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                        icon = loadImageIcon(path, 350);
                        if (icon != null && icon.getIconWidth() > 0) {
                            break;
                        }
                    }
                }
                return new ImageLoadResult(icon, aspectRatio);
            }

            @Override
            protected void done() {
                try {
                    ImageLoadResult result = get();
                    if (result.icon != null && result.icon.getIconWidth() > 0) {
                        buildingImageAspectRatio = result.aspectRatio;
                        buildingImageLabel.setIcon(result.icon);
                        buildingImageLabel.setText("");
                        updateBuildingImageSize();
                    } else {
                        buildingImageLabel.setIcon(null);
                        buildingImageLabel.setText("이미지 없음");
                        buildingImageAspectRatio = 1.0;
                    }
                } catch (Exception e) {
                    buildingImageLabel.setIcon(null);
                    buildingImageLabel.setText("이미지 로드 실패");
                    buildingImageAspectRatio = 1.0;
                    System.err.println("이미지 로드 오류: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 이미지 로드 결과를 담는 내부 클래스
     */
    private static class ImageLoadResult {
        final ImageIcon icon;
        final double aspectRatio;

        ImageLoadResult(ImageIcon icon, double aspectRatio) {
            this.icon = icon;
            this.aspectRatio = aspectRatio;
        }
    }

    /**
     * 건물 이미지 크기 업데이트 (비율에 맞게)
     */
    private void updateBuildingImageSize() {
        if (buildingImageLabel == null || buildingDetailContainer == null || !buildingDetailContainer.isVisible()) {
            return;
        }

        int imageHeight = 350;
        int imageWidth = (int) (imageHeight * buildingImageAspectRatio);
        int panelWidth = 900;
        int panelHeight = 600;
        int gap = 100;
        int totalWidth = imageWidth + gap + panelWidth;
        int x = (parentFrame.getWidth() - totalWidth) / 2;
        int imageY = (parentFrame.getHeight() - imageHeight) / 2;

        buildingImageLabel.setBounds(x, imageY, imageWidth, imageHeight);

        // 패널 위치도 업데이트
        int panelY = (parentFrame.getHeight() - panelHeight) / 2;
        buildingDetailPanel.setBounds(x + imageWidth + gap, panelY, panelWidth, panelHeight);

        buildingDetailContainer.revalidate();
        buildingDetailContainer.repaint();
    }

    /**
     * 시설 이미지를 비동기로 로드합니다.
     */
    private void loadFacilityImage(Facility facility) {
        if (facility == null || facilityImageLabel == null) {
            if (facilityImageLabel != null) {
                facilityImageLabel.setIcon(null);
                facilityImageLabel.setText("이미지 없음");
            }
            return;
        }

        // 로딩 중 표시
        facilityImageLabel.setIcon(null);
        facilityImageLabel.setText("로딩 중...");

        // [수정] 리소스 경로 통일 (/resources/...)
        String[] imagePaths = {
            "resources/images/Facilities/" + facility.getId() + "_Facility.jpg",
            "resources/images/facilities/" + facility.getId() + "_Facility.jpg",
            "resources/images/Facilities/" + facility.getId() + "_Facility.png",
            "resources/images/facilities/" + facility.getId() + "_Facility.png"
        };

        SwingWorker<ImageLoadResult, Void> worker = new SwingWorker<ImageLoadResult, Void>() {
            @Override
            protected ImageLoadResult doInBackground() throws Exception {
                ImageIcon icon = null;
                BufferedImage originalImage = null;
                double aspectRatio = 1.0;

                for (String path : imagePaths) {
                    originalImage = loadOriginalImage(path);
                    if (originalImage != null) {
                        aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                        icon = loadImageIcon(path, 350);
                        if (icon != null && icon.getIconWidth() > 0) {
                            break;
                        }
                    }
                }
                return new ImageLoadResult(icon, aspectRatio);
            }

            @Override
            protected void done() {
                try {
                    ImageLoadResult result = get();
                    if (result.icon != null && result.icon.getIconWidth() > 0) {
                        facilityImageAspectRatio = result.aspectRatio;
                        facilityImageLabel.setIcon(result.icon);
                        facilityImageLabel.setText("");
                        updateFacilityImageSize();
                    } else {
                        facilityImageLabel.setIcon(null);
                        facilityImageLabel.setText("이미지 없음");
                        facilityImageAspectRatio = 1.0;
                    }
                } catch (Exception e) {
                    facilityImageLabel.setIcon(null);
                    facilityImageLabel.setText("이미지 로드 실패");
                    facilityImageAspectRatio = 1.0;
                    System.err.println("이미지 로드 오류: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 시설 이미지 크기 업데이트 (비율에 맞게)
     */
    private void updateFacilityImageSize() {
        if (facilityImageLabel == null || facilityDetailContainer == null || !facilityDetailContainer.isVisible()) {
            return;
        }

        int imageHeight = 350;
        int imageWidth = (int) (imageHeight * facilityImageAspectRatio);
        int panelWidth = 900;
        int panelHeight = 600;
        int gap = 100;
        int totalWidth = imageWidth + gap + panelWidth;
        int x = (parentFrame.getWidth() - totalWidth) / 2;
        int imageY = (parentFrame.getHeight() - imageHeight) / 2;

        facilityImageLabel.setBounds(x, imageY, imageWidth, imageHeight);

        // 패널 위치도 업데이트
        int panelY = (parentFrame.getHeight() - panelHeight) / 2;
        facilityDetailPanel.setBounds(x + imageWidth + gap, panelY, panelWidth, panelHeight);

        facilityDetailContainer.revalidate();
        facilityDetailContainer.repaint();
    }

    /**
     * 원본 이미지를 로드합니다 (비율 계산용).
     * 수정: File I/O 제거, 클래스패스 리소스 로딩만 사용
     */
    private BufferedImage loadOriginalImage(String resourcePath) {
        try {
            BufferedImage image = null;

            // [수정] 경로 앞에 '/'가 없으면 붙여줌 (절대 경로로 통일)
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }

            // 클래스패스에서 리소스 로드
            java.net.URL url = getClass().getResource(resourcePath);

            if (url != null) {
                image = ImageIO.read(url);
            } else {
                // 디버깅을 위해 에러 로그 남기기
                // System.err.println("이미지 리소스 없음: " + resourcePath);
                return null;
            }

            // EXIF orientation 처리: 이미지가 가로로 긴 경우 90도 회전
            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();

                // 가로가 세로보다 크면 90도 시계방향 회전
                if (width > height) {
                    image = rotateImage(image, 90);
                }
            }

            return image;
        } catch (Exception e) {
            System.err.println("원본 이미지 로드 실패 [" + resourcePath + "]: " + e.getMessage());
            return null;
        }
    }

    /**
     * 이미지 파일을 로드하여 ImageIcon으로 변환합니다. (높이만 지정, 너비는 비율에 맞게)
     */
    private ImageIcon loadImageIcon(String resourcePath, int targetHeight) {
        try {
            BufferedImage image = loadOriginalImage(resourcePath);

            if (image != null) {
                // 원본 비율 계산
                double aspectRatio = (double) image.getWidth() / image.getHeight();

                // 높이에 맞춰 너비 계산
                int width = (int) (targetHeight * aspectRatio);
                int height = targetHeight;

                Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.err.println("이미지 로드 실패 [" + resourcePath + "]: " + e.getMessage());
        }

        return null;
    }

    /**
     * 이미지 파일을 로드합니다.
     * 수정: File I/O 제거, 클래스패스 리소스 로딩만 사용
     */
    private Image loadImage(String resourcePath) throws IOException {
        BufferedImage image = null;

        // [수정] 경로 앞에 '/'가 없으면 붙여줌
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }

        // 클래스패스에서 리소스 로드
        java.net.URL url = getClass().getResource(resourcePath);

        if (url != null) {
            image = ImageIO.read(url);
        } else {
            return null;
        }

        // EXIF orientation 처리
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();

            if (width > height) {
                image = rotateImage(image, 90);
            }
        }

        return image;
    }

    /**
     * 이미지를 회전시킵니다.
     */
    private BufferedImage rotateImage(BufferedImage image, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int newWidth = (int) Math.round(image.getWidth() * cos + image.getHeight() * sin);
        int newHeight = (int) Math.round(image.getWidth() * sin + image.getHeight() * cos);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rotated.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                             java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform transform = new AffineTransform();
        transform.translate(newWidth / 2, newHeight / 2);
        transform.rotate(radians);
        transform.translate(-image.getWidth() / 2, -image.getHeight() / 2);

        g2d.setTransform(transform);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    /**
     * 배경을 어둡게 만들거나 복원합니다.
     */
    private void setBackgroundDim(JFrame parentFrame, boolean dim, JComponent detailContainer) {
        if (parentFrame == null) {
			return;
		}

        JComponent glassPane = (JComponent) parentFrame.getGlassPane();

        if (dim) {
            // 반투명 검은색 레이어 생성 (이미 있으면 재사용)
            JComponent dimLayer = null;
            for (java.awt.Component comp : glassPane.getComponents()) {
                if (comp instanceof JComponent && comp.getName() != null && comp.getName().equals("dimLayer")) {
                    dimLayer = (JComponent) comp;
                    break;
                }
            }

            if (dimLayer == null) {
                dimLayer = new JComponent() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        if (getWidth() > 0 && getHeight() > 0) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                            g2.setColor(Color.BLACK);
                            g2.fillRect(0, 0, getWidth(), getHeight());
                            g2.dispose();
                        }
                    }
                };
                dimLayer.setName("dimLayer");
                dimLayer.setOpaque(false);
                dimLayer.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

                // 마우스 이벤트가 제대로 전달되도록 설정
                dimLayer.setEnabled(true);
                dimLayer.setFocusable(false);

                glassPane.add(dimLayer, 0); // 가장 아래 레이어
            }

            // dimLayer 크기 업데이트
            dimLayer.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

            dimLayer.setVisible(true);
            glassPane.setVisible(true);
            glassPane.revalidate();
            glassPane.repaint();
        } else {
            // 배경 복원
            for (java.awt.Component comp : glassPane.getComponents()) {
                if (comp instanceof JComponent && comp.getName() != null && comp.getName().equals("dimLayer")) {
                    comp.setVisible(false);
                    break;
                }
            }

            // 모든 detail container가 닫혔는지 확인
            boolean hasVisibleContainer = false;
            for (java.awt.Component comp : glassPane.getComponents()) {
                if (comp.isVisible() && comp != glassPane.getComponent(0)) {
                    hasVisibleContainer = true;
                    break;
                }
            }

            if (!hasVisibleContainer) {
                glassPane.setVisible(false);
            }
        }
    }
}