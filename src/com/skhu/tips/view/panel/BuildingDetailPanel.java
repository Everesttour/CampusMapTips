// 조민성
package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.skhu.tips.model.entity.Building;

/**
 * 건물 상세 정보를 표시하는 패널
 * 왼쪽: 건물 사진, 중앙: 건물 정보, 우측: 주요 시설 목록
 */
public class BuildingDetailPanel extends JPanel {
	
	private JLabel imageLabel;
	private JPanel infoPanel;
	private JPanel roomsPanel;
	
	// 정보 표시용 컴포넌트
	private JLabel nameLabel;
	private JTextArea descriptionArea;
	
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
		// 왼쪽: 이미지 패널
		imageLabel = new JLabel();
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		imageLabel.setVerticalAlignment(SwingConstants.CENTER);
		imageLabel.setPreferredSize(new Dimension(200, 300));
		imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		imageLabel.setOpaque(true);
		imageLabel.setBackground(Color.WHITE);
		
		// 중앙: 정보 패널
		infoPanel = createInfoPanel();
		
		// 우측: 주요 시설 패널
		roomsPanel = createRoomsPanel();
	}
	
	/**
	 * 정보 패널 생성
	 */
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createTitledBorder("건물 정보"));
		
		// 상단: 이름
		nameLabel = new JLabel();
		nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		panel.add(nameLabel, BorderLayout.NORTH);
		
		// 중앙: 설명
		descriptionArea = new JTextArea();
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setBackground(Color.WHITE);
		descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
		descriptionScroll.setBorder(BorderFactory.createTitledBorder("설명"));
		panel.add(descriptionScroll, BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * 주요 시설 패널 생성
	 */
	private JPanel createRoomsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("주요 시설"));
		panel.setPreferredSize(new Dimension(250, 0));
		
		roomsArea = new JTextArea();
		roomsArea.setEditable(false);
		roomsArea.setLineWrap(true);
		roomsArea.setWrapStyleWord(true);
		roomsArea.setBackground(Color.WHITE);
		roomsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		
		JScrollPane scrollPane = new JScrollPane(roomsArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(scrollPane, BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * 레이아웃 설정
	 */
	private void setupLayout() {
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(imageLabel, BorderLayout.CENTER);
		leftPanel.setPreferredSize(new Dimension(200, 0));
		
		add(leftPanel, BorderLayout.WEST);
		add(infoPanel, BorderLayout.CENTER);
		add(roomsPanel, BorderLayout.EAST);
	}
	
	/**
	 * 건물 정보를 표시합니다.
	 */
	public void displayBuilding(Building building) {
		if (building == null) {
			clearDisplay();
			return;
		}
		
		// 이름
		nameLabel.setText(building.getName() != null ? building.getName() : "");
		
		// 설명
		descriptionArea.setText(building.getDescription() != null ? building.getDescription() : "");
		descriptionArea.setCaretPosition(0);
		
		// 주요 시설 목록
		if (building.getRooms() != null && building.getRooms().length > 0) {
			StringBuilder roomsText = new StringBuilder();
			for (int i = 0; i < building.getRooms().length; i++) {
				roomsText.append("• ").append(building.getRooms()[i]);
				if (i < building.getRooms().length - 1) {
					roomsText.append("\n\n");
				}
			}
			roomsArea.setText(roomsText.toString());
		} else {
			roomsArea.setText("시설 정보가 없습니다.");
		}
		roomsArea.setCaretPosition(0);
		
		// 이미지 로드
		loadBuildingImage(building);
	}
	
	/**
	 * 건물 이미지를 로드합니다.
	 */
	private void loadBuildingImage(Building building) {
		if (building == null || building.getName() == null) {
			imageLabel.setIcon(null);
			imageLabel.setText("이미지 없음");
			return;
		}
		
		// 이미지 경로 시도 (건물명 기반)
		String[] imagePaths = {
			"images/buildings/" + building.getName() + ".jpg",
			"images/buildings/" + building.getName() + ".png",
			"images/buildings/" + building.getId() + ".jpg",
			"images/buildings/" + building.getId() + ".png"
		};
		
		ImageIcon icon = null;
		for (String path : imagePaths) {
			icon = loadImageIcon(path, 300);
			if (icon != null && icon.getIconWidth() > 0) {
				break;
			}
		}
		
		if (icon != null && icon.getIconWidth() > 0) {
			imageLabel.setIcon(icon);
			imageLabel.setText("");
		} else {
			imageLabel.setIcon(null);
			imageLabel.setText("이미지 없음");
		}
	}
	
	/**
	 * 이미지 파일을 로드하여 ImageIcon으로 변환합니다.
	 */
	private ImageIcon loadImageIcon(String resourcePath, int maxHeight) {
		try {
			Image image = loadImage(resourcePath);
			
			if (image != null) {
				// 비율 유지하며 높이에 맞춰 리사이즈
				int width = image.getWidth(null);
				int height = image.getHeight(null);
				if (height > maxHeight) {
					width = (width * maxHeight) / height;
					height = maxHeight;
				}
				image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
				return new ImageIcon(image);
			}
		} catch (Exception e) {
			System.err.println("이미지 로드 실패 [" + resourcePath + "]: " + e.getMessage());
		}
		
		return null;
	}
	
	/**
	 * 이미지 파일을 로드합니다.
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
			"resources/" + resourcePath,
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
	
	/**
	 * 표시 내용을 초기화합니다.
	 */
	private void clearDisplay() {
		nameLabel.setText("");
		descriptionArea.setText("");
		roomsArea.setText("");
		imageLabel.setIcon(null);
		imageLabel.setText("이미지 없음");
	}
}
