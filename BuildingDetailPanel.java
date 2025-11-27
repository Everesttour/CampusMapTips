// 조민성
package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.skhu.tips.model.entity.Building;

/**
 * 건물 상세 정보를 표시하는 패널
 * 왼쪽: 건물 사진, 중앙: 주요 시설 정보(rooms)
 */
public class BuildingDetailPanel extends JPanel {
	
	private JPanel roomsPanel;
	
	// 정보 표시용 컴포넌트
	private JLabel idLabel;
	private JLabel nameLabel;
	
	// 주요 시설 표시용 컴포넌트
	private JTextArea roomsArea;
	
	public BuildingDetailPanel() {
		super(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		initializeComponents();
		setupLayout();
	}
	
	/**
	 * 컴포넌트 초기화
	 */
	private void initializeComponents() {
		// 중앙: 주요 시설 패널
		roomsPanel = createRoomsPanel();
	}
	
	/**
	 * 주요 시설 패널 생성
	 */
	private JPanel createRoomsPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// 상단: 건물번호와 건물명
		JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
		
		// 건물번호
		idLabel = new JLabel();
		idLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		idLabel.setForeground(new Color(100, 100, 100));
		headerPanel.add(idLabel, BorderLayout.NORTH);
		
		// 건물명
		nameLabel = new JLabel();
		nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
		headerPanel.add(nameLabel, BorderLayout.CENTER);
		
		panel.add(headerPanel, BorderLayout.NORTH);
		
		// 중앙: 주요 시설 정보
		JPanel roomsContainer = new JPanel(new BorderLayout());
		JLabel roomsTitle = new JLabel("주요 시설");
		roomsTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		roomsTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
		roomsContainer.add(roomsTitle, BorderLayout.NORTH);
		
		roomsArea = new JTextArea();
		roomsArea.setEditable(false);
		roomsArea.setLineWrap(true);
		roomsArea.setWrapStyleWord(true);
		roomsArea.setOpaque(false); // 배경 투명하게 (패널 배경과 동일)
		roomsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16)); // 글자 크기 증가
		roomsArea.setForeground(Color.BLACK);
		
		JScrollPane scrollPane = new JScrollPane(roomsArea);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		roomsContainer.add(scrollPane, BorderLayout.CENTER);
		
		panel.add(roomsContainer, BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * 레이아웃 설정
	 */
	private void setupLayout() {
		add(roomsPanel, BorderLayout.CENTER);
	}
	
	/**
	 * 건물 정보를 표시합니다.
	 */
	public void displayBuilding(Building building) {
		if (building == null) {
			clearDisplay();
			return;
		}
		
		// 건물번호
		idLabel.setText("건물번호: " + building.getId());
		
		// 건물명
		nameLabel.setText(building.getName() != null ? building.getName() : "");
		
		// 주요 시설 목록
		if (building.getRooms() != null && !building.getRooms().isEmpty()) {
			roomsArea.setText(building.getRooms());
		} else {
			roomsArea.setText("시설 정보가 없습니다.");
		}
		roomsArea.setCaretPosition(0);
	}
	
	/**
	 * 표시 내용을 초기화합니다.
	 */
	private void clearDisplay() {
		idLabel.setText("");
		nameLabel.setText("");
		roomsArea.setText("");
	}
}
