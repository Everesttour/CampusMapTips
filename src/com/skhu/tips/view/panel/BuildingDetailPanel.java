package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import com.skhu.tips.model.entity.Building;

/**
 * 건물 상세 정보를 표시하는 패널 (모서리 둥글게 처리)
 * 우측 상단 닫기(X) 버튼 포함
 */
public class BuildingDetailPanel extends JPanel {

    private int cornerRadius = 25; // 모서리 둥글기 정도 (픽셀)

    private JPanel roomsPanel;
    private JPanel floorBlocksContainer;

    // [추가됨] 닫기 버튼 및 리스너
    private JButton closeButton;
    private Runnable onCloseListener;

    // 정보 표시용 컴포넌트
    private JLabel idLabel;
    private JLabel nameLabel;

    // --- 색상 정의 ---
    private static final Color BACKGROUND_COLOR = new Color(230, 245, 255); // Pale Sky Blue
    private static final Color BLOCK_BACKGROUND_COLOR = Color.WHITE;

    private static final Color HEADER_COLOR = new Color(50, 50, 50);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);
    private static final Color EMPHASIS_COLOR = new Color(0, 102, 102);
    private static final Color FLOOR_HEADER_COLOR = new Color(30, 60, 90);
    private static final Color TEXT_COLOR = new Color(70, 70, 70);

    public BuildingDetailPanel() {
        // 상단 버튼 공간 확보를 위해 수직 간격(vgap)을 5로 줄임
        super(new BorderLayout(15, 5));
        // 상단 패딩을 15 -> 10으로 약간 줄임
        setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        setBackground(BACKGROUND_COLOR);
        setOpaque(false);
        initializeComponents();
        setupLayout();
    }

    /**
     * [중요] 외부에서 닫기 동작을 설정하기 위한 메서드
     * 이 메서드가 없으면 에러가 발생합니다.
     */
    public void setOnCloseListener(Runnable listener) {
        this.onCloseListener = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = cornerRadius;

        RoundRectangle2D roundRect =
            new RoundRectangle2D.Float(0, 0, width, height, arc, arc);

        g2.clip(roundRect);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width, height, arc, arc);

        g2.dispose();
    }

    private void initializeComponents() {
        // 닫기 버튼 생성
        createCloseButton();

        roomsPanel = createRoomsPanel();
        roomsPanel.setOpaque(false);
    }

    /**
     * 닫기 버튼(X) 생성 및 스타일링
     */
    private void createCloseButton() {
        closeButton = new JButton("✖"); // 유니코드 X 문자
        closeButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        closeButton.setForeground(HEADER_COLOR);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 마우스 호버 효과 (빨간색)
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(Color.RED);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(HEADER_COLOR);
            }
        });

        // 클릭 시 동작
        closeButton.addActionListener(e -> {
            if (onCloseListener != null) {
                onCloseListener.run();
            }
        });
    }

    private JPanel createRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setOpaque(false);

        // 상단: 건물번호와 건물명
        JPanel infoHeaderPanel = createInfoHeaderPanel();
        panel.add(infoHeaderPanel, BorderLayout.NORTH);

        // 중앙: 층별 블록 컨테이너
        floorBlocksContainer = new JPanel();
        floorBlocksContainer.setLayout(new BoxLayout(floorBlocksContainer, BoxLayout.Y_AXIS));
        floorBlocksContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(floorBlocksContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportView(floorBlocksContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 상단 제목
        JLabel roomsTitle = new JLabel("📌 주요 시설");
        roomsTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        roomsTitle.setForeground(EMPHASIS_COLOR);
        roomsTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(roomsTitle, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInfoHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 8));
        headerPanel.setOpaque(false);

        idLabel = new JLabel();
        idLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        idLabel.setForeground(ACCENT_COLOR);
        headerPanel.add(idLabel, BorderLayout.NORTH);

        nameLabel = new JLabel();
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        nameLabel.setForeground(HEADER_COLOR);
        headerPanel.add(nameLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createFloorBlock(String floorLabel, String roomInfo) {
        JPanel block = new JPanel(new BorderLayout(10, 5));
        block.setBackground(BLOCK_BACKGROUND_COLOR);

        Border lineBorder = BorderFactory.createLineBorder(new Color(220, 220, 220));
        Border padding = BorderFactory.createEmptyBorder(10, 15, 10, 15);
        block.setBorder(BorderFactory.createCompoundBorder(lineBorder, padding));

        JLabel floorLabelComp = new JLabel(floorLabel);
        floorLabelComp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        floorLabelComp.setForeground(FLOOR_HEADER_COLOR);
        block.add(floorLabelComp, BorderLayout.NORTH);

        JTextArea roomArea = new JTextArea(roomInfo);
        roomArea.setEditable(false);
        roomArea.setLineWrap(true);
        roomArea.setWrapStyleWord(true);
        roomArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        roomArea.setForeground(TEXT_COLOR);
        roomArea.setOpaque(false);

        roomArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        block.add(roomArea, BorderLayout.CENTER);

        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, block.getPreferredSize().height));

        return block;
    }

    /**
     * 레이아웃 설정 (닫기 버튼 추가됨)
     */
    private void setupLayout() {
        // 상단 바 (우측 정렬된 닫기 버튼 포함)
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(closeButton);

        add(topBar, BorderLayout.NORTH);
        add(roomsPanel, BorderLayout.CENTER);
    }

    public void displayBuilding(Building building) {

        floorBlocksContainer.removeAll();

        if (building == null) {
            clearDisplay();
            floorBlocksContainer.revalidate();
            floorBlocksContainer.repaint();
            return;
        }

        idLabel.setText("건물번호: " + building.getId());

        nameLabel.setText(building.getName() != null ? building.getName() : "건물명을 불러올 수 없습니다.");

        if (building.getRooms() != null && !building.getRooms().isEmpty()) {
            String roomsText = building.getRooms();

            List<String> floorInfos = Arrays.asList(roomsText.split(" / "));

            for (String floorInfo : floorInfos) {
                floorInfo = floorInfo.trim();
                if (floorInfo.isEmpty()) {
					continue;
				}

                String floorLabel;
                String roomContent;

                int colonIndex = floorInfo.indexOf(':');

                boolean isFloorSpecific = false;
                if (colonIndex > 0) {
                    String prefix = floorInfo.substring(0, colonIndex);
                    if (prefix.matches(".*[0-9]+[층|F|f|F층]")) {
                        isFloorSpecific = true;
                    }
                }

                if (isFloorSpecific) {
                    floorLabel = floorInfo.substring(0, colonIndex + 1);
                    roomContent = floorInfo.substring(colonIndex + 1).trim();
                } else {
                    floorLabel = "전체 시설";
                    roomContent = floorInfo;
                }

                JPanel floorBlock = createFloorBlock(floorLabel, roomContent);
                floorBlocksContainer.add(floorBlock);

                floorBlocksContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } else {
            JPanel emptyBlock = createFloorBlock("정보 없음", "이 건물에 대한 시설 정보가 등록되어 있지 않습니다.");
            floorBlocksContainer.add(emptyBlock);
        }

        floorBlocksContainer.revalidate();
        floorBlocksContainer.repaint();
    }

    private void clearDisplay() {
        idLabel.setText("");
        nameLabel.setText("건물 정보를 선택해주세요.");
        floorBlocksContainer.removeAll();
    }
}