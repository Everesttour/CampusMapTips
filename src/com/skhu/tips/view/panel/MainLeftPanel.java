package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import java.awt.Graphics;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;

/**
 * @class MainLeftPanel
 * @brief 왼쪽 메인 패널 뷰. 컴포넌트를 소유하며, 컨트롤러가 사용할 Getter와 단순 동작 메소드를 제공합니다.
 */
public class MainLeftPanel extends JPanel {

    // --- 1. Fields (컴포넌트 및 데이터 모델) ---
    private JList<Building> buildingList;
    private JList<Facility> facilityList;
    private JButton buildingButton;
    private JButton facilityButton;
    private JPanel buttonPanel;
    private JPanel contentPanel;
    private DefaultListModel<Building> buildingModel = new DefaultListModel<>();
    private DefaultListModel<Facility> facilityModel = new DefaultListModel<>();
    private JScrollPane buildingScrollPane;
    private JScrollPane facilityScrollPane;
    private ImageIcon buildingIcon;
    private ImageIcon facilityIcon;
    private final Color buildingColor = new Color(173, 216, 230);
    private final Color facilityColor = new Color(255, 182, 193);

    /**
     * @brief 생성자: 뷰 초기화 및 레이아웃 설정
     */
    public MainLeftPanel() {
        super(new BorderLayout());
        loadIcons();
        initializeComponents();
        setupLayout();
        
        // 초기 상태 설정: 건물 리스트를 기본으로 표시
        updateButtonColors(true);
    }

    // =======================================================================
    // --- 2. Public API & Getters (컨트롤러가 사용하는 메소드) ---
    // =======================================================================

    /**
     * @brief [Controller API] 건물 리스트 JList 객체를 반환합니다.
     */
    public JList<Building> getBuildingList() { return buildingList; }

    /**
     * @brief [Controller API] 시설 리스트 JList 객체를 반환합니다.
     */
    public JList<Facility> getFacilityList() { return facilityList; }

    /**
     * @brief [Controller API] 건물 버튼 객체를 반환합니다.
     */
    public JButton getBuildingButton() { return buildingButton; }

    /**
     * @brief [Controller API] 시설 버튼 객체를 반환합니다.
     */
    public JButton getFacilityButton() { return facilityButton; }

    /**
     * @brief [Controller API] 건물 데이터를 리스트에 설정합니다.
     */
    public void setBuildingListData(List<Building> data) {
        buildingModel.clear();
        data.forEach(buildingModel::addElement);
    }

    /**
     * @brief [Controller API] 시설 데이터를 리스트에 설정합니다.
     */
    public void setFacilityListData(List<Facility> data) {
        facilityModel.clear();
        data.forEach(facilityModel::addElement);
    }

    /**
     * @brief [Controller API] 뷰를 건물 리스트로 전환합니다.
     */
    public void showBuildingList() {
        contentPanel.remove(facilityScrollPane);
        contentPanel.add(buildingScrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * @brief [Controller API] 뷰를 시설 리스트로 전환합니다.
     */
    public void showFacilityList() {
        contentPanel.remove(buildingScrollPane);
        contentPanel.add(facilityScrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * @brief [Controller API] 버튼 색상을 활성화/비활성화 상태로 변경합니다.
     */
    public void updateButtonColors(boolean isBuildingActive) {
        if (isBuildingActive) {
            buildingButton.setBackground(buildingColor.darker());
            facilityButton.setBackground(facilityColor);
        } else {
            buildingButton.setBackground(buildingColor);
            facilityButton.setBackground(facilityColor.darker());
        }
    }


    // =======================================================================
    // --- 3. Private Implementation Helpers (내부 구현 로직) ---
    // =======================================================================

    /**
     * @brief 내부 컴포넌트를 초기화하고 커스텀 렌더러를 설정합니다.
     */
    private void initializeComponents() {
        buildingList = new JList<>(buildingModel);
        facilityList = new JList<>(facilityModel);

        // 리스트 선택 모드 설정 (단일 선택)
        buildingList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        facilityList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        buildingList.setCellRenderer(new ButtonListCellRenderer<>(true));
        facilityList.setCellRenderer(new ButtonListCellRenderer<>(false));

        buildingScrollPane = new JScrollPane(buildingList);
        facilityScrollPane = new JScrollPane(facilityList);

        buildingButton = createCategoryButton(true);
        facilityButton = createCategoryButton(false);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(buildingButton);
        buttonPanel.add(facilityButton);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(buildingScrollPane, BorderLayout.CENTER); // 기본 설정
    }

    /**
     * @brief 메인 레이아웃을 설정하고 컴포넌트를 배치합니다.
     */
    private void setupLayout() {
        add(buttonPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * @brief 카테고리 버튼을 생성하고 스타일을 적용합니다.
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

        ImageIcon buttonIcon = resizeIconForButton(isBuilding ? buildingIcon : facilityIcon, 32);
        button.setIcon(buttonIcon);

        button.setBackground(isBuilding ? buildingColor : facilityColor);
        button.setOpaque(false); // paintComponent에서 직접 그리므로 false
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(150, 60));

        return button;
    }

    /**
     * @brief 원본 아이콘을 지정된 크기로 안전하게 리사이즈합니다.
     */
    private ImageIcon resizeIconForButton(ImageIcon originalIcon, int size) {
        if (originalIcon == null || originalIcon.getIconWidth() == 0) {
            return new ImageIcon();
        }
        Image img = originalIcon.getImage();
        if (img == null) {
            return new ImageIcon();
        }
        Image resizedImg = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    /**
     * @brief 이미지 리소스를 클래스패스에서 안전하게 불러옵니다.
     */
    private void loadIcons() {
        buildingIcon = loadImageIcon("images/LeftPanel/Building_Icon.png", "resources/images/LeftPanel/Building_Icon.png", 40);
        facilityIcon = loadImageIcon("images/LeftPanel/Facility_Icon.png", "resources/images/LeftPanel/Facility_Icon.png", 40);
    }

    /**
     * @brief 이미지 파일을 읽어와 ImageIcon으로 변환하는 헬퍼 메소드 (Null-Safe)
     */
    private ImageIcon loadImageIcon(String primaryPath, String fallbackPath, int size) {
        Image image = null;
        try {
            java.net.URL url = getClass().getClassLoader().getResource(primaryPath);
            if (url == null) {
                url = getClass().getClassLoader().getResource(fallbackPath);
            }

            if (url != null) {
                image = ImageIO.read(url);
                image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(image);
            } else {
                System.err.println("이미지 리소스 찾기 실패: " + primaryPath + " 및 " + fallbackPath);
            }
        } catch (Exception e) {
            System.err.println("이미지 로드 실패 [" + primaryPath + "]: " + e.getMessage());
        }
        return new ImageIcon();
    }


    // =======================================================================
    // --- 4. Custom ListCellRenderer (JList 커스텀 그리기) ---
    // =======================================================================

    /**
     * @class ButtonListCellRenderer
     * @brief JList의 각 항목을 커스텀 디자인(원형 순번, 명칭, 설명)으로 그리는 렌더러.
     */
    private static class ButtonListCellRenderer<T> implements ListCellRenderer<Object> {

        private final boolean isBuilding;
        private final Color lightBlue = new Color(173, 216, 230);
        private final Color lightRed = new Color(255, 182, 193);
        private final JPanel panel;
        private final JLabel numberLabel;
        private final JLabel nameLabel;
        private final JLabel descriptionLabel;

        public ButtonListCellRenderer(boolean isBuilding) {
            this.isBuilding = isBuilding;

            panel = new JPanel(new BorderLayout(10, 0));
            panel.setOpaque(true);
            panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));

            numberLabel = new JLabel();
            numberLabel.setPreferredSize(new Dimension(40, 40));
            numberLabel.setHorizontalAlignment(JLabel.CENTER);
            numberLabel.setVerticalAlignment(JLabel.CENTER);

            JPanel textPanel = new JPanel(new BorderLayout(0, 4));
            textPanel.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            nameLabel.setOpaque(false);

            descriptionLabel = new JLabel();
            descriptionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            descriptionLabel.setForeground(Color.GRAY);
            descriptionLabel.setOpaque(false);

            textPanel.add(nameLabel, BorderLayout.NORTH);
            textPanel.add(descriptionLabel, BorderLayout.CENTER);

            panel.add(numberLabel, BorderLayout.WEST);
            panel.add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            String name = "";
            String description = "";

            if (value instanceof Building) {
                Building building = (Building) value;
                name = building.getName();
                description = building.getDescription() != null ? building.getDescription() : "";
            } else if (value instanceof Facility) {
                Facility facility = (Facility) value;
                name = facility.getName();
                description = facility.getDescription() != null ? facility.getDescription() : "";
            }

            nameLabel.setText(name);
            descriptionLabel.setText(description);

            if (description.length() > 30) {
                descriptionLabel.setText(description.substring(0, 27) + "...");
            }

            Color bgColor = isBuilding ? lightBlue : lightRed;
            Color circleColor = isSelected ? bgColor.darker() : bgColor;
            panel.setBackground(Color.WHITE);

            String numberText = String.valueOf(index + 1);
            ImageIcon circleIcon = createCircleIcon(circleColor, numberText, 40);
            numberLabel.setIcon(circleIcon);

            if (isSelected) {
                panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(bgColor.darker().darker(), 2),
                        javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)
                ));
            } else {
                panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
            }

            panel.setPreferredSize(new Dimension(list.getWidth() - 20, 70));

            return panel;
        }

        /**
         * @brief 원형 배경에 숫자가 들어간 아이콘을 그려주는 헬퍼
         */
        private ImageIcon createCircleIcon(Color color, String text, int size) {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.fillOval(2, 2, size - 4, size - 4);

            if (text != null && !text.isEmpty()) {
                g2.setColor(Color.WHITE);
                Font font = new Font(Font.SANS_SERIF, Font.BOLD, 14);
                g2.setFont(font);

                int textX = (size - g2.getFontMetrics().stringWidth(text)) / 2;
                int textY = (size + g2.getFontMetrics().getAscent()) / 2 - 2;
                g2.drawString(text, textX, textY);
            }

            g2.dispose();
            return new ImageIcon(image);
        }
    }
}