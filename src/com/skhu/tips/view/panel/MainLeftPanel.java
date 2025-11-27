package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;

/**
 * @class MainLeftPanel
 * @brief "멍청한 뷰" - 오직 컴포넌트를 소유하고 Getter만 제공합니다.
 */
public class MainLeftPanel extends JPanel {

    // 1. 뷰가 내부적으로 소유한 컴포넌트
    private JList<Building> buildingList;
    private JList<Facility> facilityList;
    private JButton buildingButton;
    private JButton facilityButton;
    private JPanel buttonPanel;
    private JPanel contentPanel;
    private JScrollPane currentScrollPane;

    // (JList에 데이터를 채우기 위한 모델)
    private DefaultListModel<Building> buildingModel = new DefaultListModel<>();
    private DefaultListModel<Facility> facilityModel = new DefaultListModel<>();

    // 이미지 아이콘
    private ImageIcon buildingIcon;
    private ImageIcon facilityIcon;

    // 현재 선택된 뷰 타입 (true: 건물, false: 시설)
    private boolean isBuildingView = true;

    public MainLeftPanel() {
        super(new BorderLayout());

        // 1. 리소스 로드
        loadIcons();

        // 2. 컴포넌트 초기화
        initializeComponents();

        // 3. 레이아웃 설정
        setupLayout();

        // 4. 초기 상태 설정
        updateButtonStates(true);

        // 뷰는 리스너를 등록하지 않습니다!
    }

    /**
     * 모든 컴포넌트를 생성하고 기본 설정을 적용합니다.
     */
    private void initializeComponents() {
        // 리스트 컴포넌트 생성
        buildingList = new JList<>(buildingModel);
        facilityList = new JList<>(facilityModel);

        // 리스트 선택 모드 설정 (단일 선택)
        buildingList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        facilityList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // 커스텀 렌더러 설정
        buildingList.setCellRenderer(new ButtonListCellRenderer<>(true));
        facilityList.setCellRenderer(new ButtonListCellRenderer<>(false));

        // 카테고리 버튼 생성
        buildingButton = createCategoryButton(true);
        facilityButton = createCategoryButton(false);

        // 버튼 패널 생성 및 버튼 추가
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(buildingButton);
        buttonPanel.add(facilityButton);

        // 콘텐츠 패널 생성
        contentPanel = new JPanel(new BorderLayout());
        currentScrollPane = new JScrollPane(buildingList);
        contentPanel.add(currentScrollPane, BorderLayout.CENTER);
    }

    /**
     * 레이아웃을 설정하고 컴포넌트를 배치합니다.
     */
    private void setupLayout() {
        add(buttonPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * 카테고리 버튼을 생성합니다.
     */
    private JButton createCategoryButton(boolean isBuilding) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int arc = 15; // 둥근 모서리 크기

                // 둥근 사각형 배경 그리기
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, width, height, arc, arc);

                // 검은색 테두리 그리기
                g2.setColor(Color.BLACK);
                g2.setStroke(new java.awt.BasicStroke(2)); // 테두리 두께
                g2.drawRoundRect(1, 1, width - 2, height - 2, arc, arc);

                // 아이콘과 텍스트 그리기
                super.paintComponent(g2);
                g2.dispose();
            }
        };

        // 아이콘 크기 조정 (버튼용)
        ImageIcon buttonIcon = resizeIconForButton(isBuilding ? buildingIcon : facilityIcon, 32);
        button.setIcon(buttonIcon);

     // 텍스트 설정 (건물/시설 이름 추가)
        if (isBuilding) {
            button.setText("건물");
        } else {
            button.setText("시설");
        }

     // 폰트 및 간격 설정
        button.setFont(new java.awt.Font("맑은 고딕", java.awt.Font.BOLD, 14));
        button.setIconTextGap(10); // 아이콘과 글자 사이의 간격 (10픽셀)
        button.setForeground(Color.BLACK); // 글자색 검정

        // 색상 설정
        if (isBuilding) {
            button.setBackground(new Color(173, 216, 230)); // 연한 파랑
        } else {
            button.setBackground(new Color(255, 182, 193)); // 연한 빨강
        }

        button.setOpaque(false); // paintComponent에서 직접 그리므로 false
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(150, 50));

        return button;
    }

    /**
     * 아이콘을 버튼용 크기로 리사이즈합니다.
     */
    private ImageIcon resizeIconForButton(ImageIcon originalIcon, int size) {
        if (originalIcon == null || originalIcon.getIconWidth() == 0) {
            return new ImageIcon();
        }
        Image img = originalIcon.getImage();
        Image resizedImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    /**
     * 버튼 상태를 업데이트하고 뷰를 전환합니다.
     */
    private void updateButtonStates(boolean showBuilding) {
        isBuildingView = showBuilding;

        if (showBuilding) {
            // 건물 버튼 활성화, 시설 버튼 비활성화
            buildingButton.setBackground(new Color(173, 216, 230).darker());
            facilityButton.setBackground(new Color(255, 182, 193));

            // 건물 리스트 표시
            contentPanel.remove(currentScrollPane);
            currentScrollPane = new JScrollPane(buildingList);
            contentPanel.add(currentScrollPane, BorderLayout.CENTER);
        } else {
            // 시설 버튼 활성화, 건물 버튼 비활성화
            facilityButton.setBackground(new Color(255, 182, 193).darker());
            buildingButton.setBackground(new Color(173, 216, 230));

            // 시설 리스트 표시
            contentPanel.remove(currentScrollPane);
            currentScrollPane = new JScrollPane(facilityList);
            contentPanel.add(currentScrollPane, BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    /**
     * 이미지 아이콘을 로드합니다.
     */
    private void loadIcons() {
        buildingIcon = loadImageIcon("images/LeftPanel/Building_Icon.png", 40);
        facilityIcon = loadImageIcon("images/LeftPanel/Facility_Icon.png", 40);
    }

    /**
     * 이미지 파일을 로드하여 ImageIcon으로 변환합니다.
     * 클래스패스와 파일 시스템 경로를 모두 시도합니다.
     *
     * @param resourcePath 클래스패스 기준 리소스 경로 (예: "images/LeftPanel/icon.png")
     * @param size 리사이즈할 크기 (정사각형)
     * @return 로드된 ImageIcon, 실패 시 빈 ImageIcon
     */
    private ImageIcon loadImageIcon(String resourcePath, int size) {
        try {
            Image image = loadImage(resourcePath);

            if (image != null) {
                image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(image);
            }
        } catch (Exception e) {
            System.err.println("이미지 로드 실패 [" + resourcePath + "]: " + e.getMessage());
        }

        return new ImageIcon();
    }

    /**
     * 이미지 파일을 로드합니다. 클래스패스를 먼저 시도하고, 실패하면 파일 시스템 경로를 시도합니다.
     *
     * @param resourcePath 클래스패스 기준 리소스 경로
     * @return 로드된 Image, 실패 시 null
     */
    private Image loadImage(String resourcePath) throws IOException {
        // 1. 클래스패스에서 리소스 로드 시도
        java.net.URL url = getClass().getClassLoader().getResource(resourcePath);
        if (url != null) {
            return ImageIO.read(url);
        }

        // 2. 파일 시스템에서 로드 시도
        String[] paths = {
            "src/resources/" + resourcePath,
            "CampusMapTips/src/resources/" + resourcePath,
            "../src/resources/" + resourcePath
        };

        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        }

        return null;
    }

    // --- 4. 컨트롤러가 접근할 수 있도록 Getter를 제공 ---

    public JList<Building> getBuildingList() {
        return buildingList;
    }

    public JList<Facility> getFacilityList() {
        return facilityList;
    }

    public JButton getBuildingButton() {
        return buildingButton;
    }

    public JButton getFacilityButton() {
        return facilityButton;
    }

    /**
     * 뷰를 건물 또는 시설로 전환합니다.
     */
    public void switchToBuildingView() {
        updateButtonStates(true);
    }

    /**
     * 뷰를 시설로 전환합니다.
     */
    public void switchToFacilityView() {
        updateButtonStates(false);
    }

    // --- 5. 뷰가 데이터를 표시하기 위한 메소드 ---
    public void setBuildingListData(List<Building> data) {
        buildingModel.clear();
        data.forEach(buildingModel::addElement);
    }

    public void setFacilityListData(List<Facility> data) {
        facilityModel.clear();
        data.forEach(facilityModel::addElement);
    }
}