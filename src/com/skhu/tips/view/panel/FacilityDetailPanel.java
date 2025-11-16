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

import com.skhu.tips.model.entity.Facility;

/**
 * 시설 상세 정보를 표시하는 패널
 * 왼쪽: 시설 사진, 중앙: 시설 정보, 우측: 꿀팁 내용
 */
public class FacilityDetailPanel extends JPanel {
	
	private JLabel imageLabel;
	private JPanel infoPanel;
	private JPanel tipsPanel;
	
	// 정보 표시용 컴포넌트
	private JLabel nameLabel;
	private JLabel buildingNameLabel;
	private JLabel floorLabel;
	private JTextArea descriptionArea;
	private JTextArea overviewArea;
	private JLabel operatingHoursLabel;
	private JTextArea noticeArea;
	
	// 꿀팁 표시용 컴포넌트
	private JTextArea tipsArea;
	
	public FacilityDetailPanel() {
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
		
		// 우측: 꿀팁 패널
		tipsPanel = createTipsPanel();
	}
	
	/**
	 * 정보 패널 생성
	 */
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createTitledBorder("시설 정보"));
		
		// 상단: 이름, 건물명, 층수
		JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
		
		nameLabel = new JLabel();
		nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		headerPanel.add(nameLabel, BorderLayout.NORTH);
		
		JPanel subHeaderPanel = new JPanel(new BorderLayout(5, 5));
		buildingNameLabel = new JLabel();
		buildingNameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		floorLabel = new JLabel();
		floorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		subHeaderPanel.add(buildingNameLabel, BorderLayout.WEST);
		subHeaderPanel.add(floorLabel, BorderLayout.EAST);
		headerPanel.add(subHeaderPanel, BorderLayout.CENTER);
		
		panel.add(headerPanel, BorderLayout.NORTH);
		
		// 중앙: 개요 및 설명
		JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
		
		JLabel overviewTitle = new JLabel("개요");
		overviewTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		overviewArea = new JTextArea();
		overviewArea.setEditable(false);
		overviewArea.setLineWrap(true);
		overviewArea.setWrapStyleWord(true);
		overviewArea.setBackground(Color.WHITE);
		overviewArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		JScrollPane overviewScroll = new JScrollPane(overviewArea);
		overviewScroll.setBorder(BorderFactory.createTitledBorder("개요"));
		overviewScroll.setPreferredSize(new Dimension(0, 100));
		
		JLabel descriptionTitle = new JLabel("설명");
		descriptionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		descriptionArea = new JTextArea();
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setBackground(Color.WHITE);
		descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
		descriptionScroll.setBorder(BorderFactory.createTitledBorder("설명"));
		
		contentPanel.add(overviewScroll, BorderLayout.NORTH);
		contentPanel.add(descriptionScroll, BorderLayout.CENTER);
		
		panel.add(contentPanel, BorderLayout.CENTER);
		
		// 하단: 운영시간 및 공지사항
		JPanel footerPanel = new JPanel(new BorderLayout(5, 5));
		
		operatingHoursLabel = new JLabel();
		operatingHoursLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		operatingHoursLabel.setBorder(BorderFactory.createTitledBorder("운영 시간"));
		footerPanel.add(operatingHoursLabel, BorderLayout.NORTH);
		
		noticeArea = new JTextArea();
		noticeArea.setEditable(false);
		noticeArea.setLineWrap(true);
		noticeArea.setWrapStyleWord(true);
		noticeArea.setBackground(Color.WHITE);
		noticeArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		JScrollPane noticeScroll = new JScrollPane(noticeArea);
		noticeScroll.setBorder(BorderFactory.createTitledBorder("공지사항"));
		noticeScroll.setPreferredSize(new Dimension(0, 80));
		footerPanel.add(noticeScroll, BorderLayout.CENTER);
		
		panel.add(footerPanel, BorderLayout.SOUTH);
		
		return panel;
	}
	
	/**
	 * 꿀팁 패널 생성
	 */
	private JPanel createTipsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("꿀팁"));
		panel.setPreferredSize(new Dimension(250, 0));
		
		tipsArea = new JTextArea();
		tipsArea.setEditable(false);
		tipsArea.setLineWrap(true);
		tipsArea.setWrapStyleWord(true);
		tipsArea.setBackground(Color.WHITE);
		tipsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		
		JScrollPane scrollPane = new JScrollPane(tipsArea);
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
		add(tipsPanel, BorderLayout.EAST);
	}
	
	/**
	 * 시설 정보를 표시합니다.
	 */
	public void displayFacility(Facility facility) {
		if (facility == null) {
			clearDisplay();
			return;
		}
		
		// 이름
		nameLabel.setText(facility.getName() != null ? facility.getName() : "");
		
		// 건물명
		buildingNameLabel.setText("건물: " + (facility.getBuildingName() != null ? facility.getBuildingName() : ""));
		
		// 층수
		floorLabel.setText("층수: " + facility.getFloor() + "층");
		
		// 개요
		overviewArea.setText(facility.getOverview() != null ? facility.getOverview() : "");
		overviewArea.setCaretPosition(0);
		
		// 설명
		descriptionArea.setText(facility.getDescription() != null ? facility.getDescription() : "");
		descriptionArea.setCaretPosition(0);
		
		// 운영시간
		operatingHoursLabel.setText(facility.getOperatingHours() != null ? facility.getOperatingHours() : "정보 없음");
		
		// 공지사항
		noticeArea.setText(facility.getNotice() != null ? facility.getNotice() : "");
		noticeArea.setCaretPosition(0);
		
		// 꿀팁
		if (facility.getTips() != null && facility.getTips().length > 0) {
			StringBuilder tipsText = new StringBuilder();
			for (int i = 0; i < facility.getTips().length; i++) {
				tipsText.append("• ").append(facility.getTips()[i]);
				if (i < facility.getTips().length - 1) {
					tipsText.append("\n\n");
				}
			}
			tipsArea.setText(tipsText.toString());
		} else {
			tipsArea.setText("꿀팁 정보가 없습니다.");
		}
		tipsArea.setCaretPosition(0);
		
		// 이미지 로드
		loadFacilityImage(facility);
	}
	
	/**
	 * 시설 이미지를 로드합니다.
	 */
	private void loadFacilityImage(Facility facility) {
		if (facility == null || facility.getName() == null) {
			imageLabel.setIcon(null);
			imageLabel.setText("이미지 없음");
			return;
		}
		
		// 이미지 경로 시도 (시설명 기반)
		String[] imagePaths = {
			"images/facilities/" + facility.getName() + ".jpg",
			"images/facilities/" + facility.getName() + ".png",
			"images/facilities/" + facility.getId() + ".jpg",
			"images/facilities/" + facility.getId() + ".png"
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
		buildingNameLabel.setText("");
		floorLabel.setText("");
		overviewArea.setText("");
		descriptionArea.setText("");
		operatingHoursLabel.setText("");
		noticeArea.setText("");
		tipsArea.setText("");
		imageLabel.setIcon(null);
		imageLabel.setText("이미지 없음");
	}
}
