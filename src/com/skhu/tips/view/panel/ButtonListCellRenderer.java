package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;

/**
 * JList의 각 셀을 커스텀 레이아웃으로 렌더링하는 커스텀 렌더러
 * 좌측: 동그란 원에 순번, 우측: 명칭과 설명 텍스트
 * @param <T> Building 또는 Facility 타입
 */
public class ButtonListCellRenderer<T> implements ListCellRenderer<Object> {
    private final boolean isBuilding; // 건물인지 시설인지 구분
    private final Color lightBlue = new Color(173, 216, 230); // 연한 파랑
    private final Color lightRed = new Color(255, 182, 193); // 연한 빨강
    
    // 패널 컴포넌트들
    private final JPanel panel;
    private final JLabel numberLabel;
    private final JLabel nameLabel;
    private final JLabel descriptionLabel;
    
    public ButtonListCellRenderer(boolean isBuilding) {
        this.isBuilding = isBuilding;
        
        // 패널 생성
        panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(true);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        // 순번 원형 라벨
        numberLabel = new JLabel();
        numberLabel.setPreferredSize(new Dimension(40, 40));
        numberLabel.setHorizontalAlignment(JLabel.CENTER);
        numberLabel.setVerticalAlignment(JLabel.CENTER);
        
        // 텍스트 패널 (명칭 + 설명)
        JPanel textPanel = new JPanel(new BorderLayout(0, 4));
        textPanel.setOpaque(false);
        
        // 명칭 라벨 (크고 bold)
        nameLabel = new JLabel();
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        nameLabel.setOpaque(false);
        
        // 설명 라벨 (작은 텍스트)
        descriptionLabel = new JLabel();
        descriptionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        descriptionLabel.setForeground(Color.GRAY);
        descriptionLabel.setOpaque(false);
        
        textPanel.add(nameLabel, BorderLayout.NORTH);
        textPanel.add(descriptionLabel, BorderLayout.CENTER);
        
        // 레이아웃 설정
        panel.add(numberLabel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        
        // 데이터 추출
        String name = "";
        String description = "";
        int id = 0;
        
        if (value instanceof Building) {
            Building building = (Building) value;
            name = building.getName();
            description = building.getDescription() != null ? building.getDescription() : "";
            id = building.getId();
        } else if (value instanceof Facility) {
            Facility facility = (Facility) value;
            name = facility.getName();
            description = facility.getDescription() != null ? facility.getDescription() : "";
            id = facility.getId();
        }
        
        // 텍스트 설정
        nameLabel.setText(name);
        descriptionLabel.setText(description);
        
        // 설명이 너무 길면 자르기
        if (description.length() > 30) {
            descriptionLabel.setText(description.substring(0, 27) + "...");
        }
        
        // 원형 아이콘 색상 설정 (카테고리 색상 사용)
        Color bgColor = isBuilding ? lightBlue : lightRed;
        Color circleColor = isSelected ? bgColor.darker() : bgColor;
        
        // 패널 배경색은 흰색으로 설정
        panel.setBackground(Color.WHITE);
        
        // 원형 아이콘 생성 및 설정 (id 사용)
        String numberText = String.valueOf(id);
        ImageIcon circleIcon = createCircleIcon(circleColor, numberText, 40);
        numberLabel.setIcon(circleIcon);
        
        // 선택 시 테두리 추가
        if (isSelected) {
            panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(bgColor.darker().darker(), 2),
                javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
        } else {
            panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
        }
        
        // 크기 설정
        panel.setPreferredSize(new Dimension(list.getWidth() - 20, 70));
        
        return panel;
    }
    
    /**
     * 원형 배경에 숫자가 들어간 아이콘을 생성합니다.
     */
    private ImageIcon createCircleIcon(Color color, String text, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 원 그리기
        g2.setColor(color);
        g2.fillOval(2, 2, size - 4, size - 4);
        
        // 텍스트 그리기
        if (text != null && !text.isEmpty()) {
            g2.setColor(Color.WHITE);
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, 14);
            g2.setFont(font);
            
            // 텍스트 중앙 정렬
            int textX = (size - g2.getFontMetrics().stringWidth(text)) / 2;
            int textY = (size + g2.getFontMetrics().getAscent()) / 2 - 2;
            g2.drawString(text, textX, textY);
        }
        
        g2.dispose();
        return new ImageIcon(image);
    }
}

