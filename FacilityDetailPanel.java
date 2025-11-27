// 조민성
package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.skhu.tips.model.entity.Facility;

/**
 * 시설 상세 정보를 표시하는 패널
 * 왼쪽: 시설 사진, 중앙: 시설 정보, 우측: 꿀팁 내용
 */
public class FacilityDetailPanel extends JPanel {
	
	private JPanel infoPanel;
	private JPanel tipsPanel;
	
	// 정보 표시용 컴포넌트
	private JLabel nameLabel;
	private JLabel buildingNameLabel;
	private JLabel floorLabel;
	private JTextArea descriptionArea;
	private JTextArea overviewArea;
	private JLabel operatingHoursLabel;
	private JLabel noticeLabel;
	
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
		
		// 상단: 이름, 건물명
		JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
		
		nameLabel = new JLabel();
		nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		headerPanel.add(nameLabel, BorderLayout.NORTH);
		
		buildingNameLabel = new JLabel();
		buildingNameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		headerPanel.add(buildingNameLabel, BorderLayout.CENTER);
		
		panel.add(headerPanel, BorderLayout.NORTH);
		
		// 중앙: 개요 및 설명
		JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
		
		JLabel overviewTitle = new JLabel("개요");
		overviewTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		overviewArea = new JTextArea();
		overviewArea.setEditable(false);
		overviewArea.setLineWrap(true);
		overviewArea.setWrapStyleWord(true);
		overviewArea.setOpaque(false); // 배경 투명하게 (패널 배경과 동일)
		overviewArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		JScrollPane overviewScroll = new JScrollPane(overviewArea);
		overviewScroll.setOpaque(false);
		overviewScroll.getViewport().setOpaque(false);
		overviewScroll.setBorder(BorderFactory.createTitledBorder("개요"));
		overviewScroll.setPreferredSize(new Dimension(0, 100));
		
		JLabel descriptionTitle = new JLabel("설명");
		descriptionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		descriptionArea = new JTextArea();
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setOpaque(false); // 배경 투명하게 (패널 배경과 동일)
		descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
		descriptionScroll.setOpaque(false);
		descriptionScroll.getViewport().setOpaque(false);
		descriptionScroll.setBorder(BorderFactory.createTitledBorder("설명"));
		
		contentPanel.add(overviewScroll, BorderLayout.NORTH);
		contentPanel.add(descriptionScroll, BorderLayout.CENTER);
		
		panel.add(contentPanel, BorderLayout.CENTER);
		
		// 하단: 층수, 운영시간, 공지사항 (가로 정렬)
		JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		
		// 층수
		floorLabel = new JLabel("-");
		floorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		floorLabel.setBorder(BorderFactory.createTitledBorder("층수"));
		footerPanel.add(floorLabel);
		
		// 운영시간
		operatingHoursLabel = new JLabel("-");
		operatingHoursLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		operatingHoursLabel.setBorder(BorderFactory.createTitledBorder("운영 시간"));
		footerPanel.add(operatingHoursLabel);
		
		// 공지사항
		noticeLabel = new JLabel("-");
		noticeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		noticeLabel.setBorder(BorderFactory.createTitledBorder("공지사항"));
		footerPanel.add(noticeLabel);
		
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
		tipsArea.setOpaque(false); // 배경 투명하게 (패널 배경과 동일)
		tipsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		
		JScrollPane scrollPane = new JScrollPane(tipsArea);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(scrollPane, BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * 레이아웃 설정
	 */
	private void setupLayout() {
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
		String name = facility.getName();
		nameLabel.setText((name != null && !name.trim().isEmpty()) ? name : "-");
		
		// 건물명
		String buildingName = facility.getBuildingName();
		buildingNameLabel.setText("건물: " + ((buildingName != null && !buildingName.trim().isEmpty()) ? buildingName : "-"));
		
		// 층수
		int floor = facility.getFloor();
		String floorText;
		if (floor == 0) {
			floorText = "     -     ";
		} else if (floor < 0) {
			floorText = "지하 " + Math.abs(floor) + "층";
		} else {
			floorText = "  " + floor + "층  ";
		}
		floorLabel.setText(floorText);
		
		// 개요
		String overview = facility.getOverview();
		overviewArea.setText((overview != null && !overview.trim().isEmpty()) ? overview : "-");
		overviewArea.setCaretPosition(0);
		
		// 설명
		String description = facility.getDescription();
		descriptionArea.setText((description != null && !description.trim().isEmpty()) ? description : "-");
		descriptionArea.setCaretPosition(0);
		
		// 운영시간
		String operatingHours = facility.getOperatingHours();
		operatingHoursLabel.setText((operatingHours != null && !operatingHours.trim().isEmpty()) ? operatingHours : "-");
		
		// 공지사항
		String notice = facility.getNotice();
		noticeLabel.setText((notice != null && !notice.trim().isEmpty()) ? notice : "-");
		
		// 꿀팁
		if (facility.getTips() != null && facility.getTips().length > 0) {
			StringBuilder tipsText = new StringBuilder();
			boolean hasValidTip = false;
			for (int i = 0; i < facility.getTips().length; i++) {
				String tip = facility.getTips()[i];
				if (tip != null && !tip.trim().isEmpty()) {
					if (hasValidTip) {
						tipsText.append("\n\n");
					}
					tipsText.append("• ").append(tip);
					hasValidTip = true;
				}
			}
			tipsArea.setText(hasValidTip ? tipsText.toString() : "-");
		} else {
			tipsArea.setText("-");
		}
		tipsArea.setCaretPosition(0);
	}
	
	/**
	 * 표시 내용을 초기화합니다.
	 */
	private void clearDisplay() {
		nameLabel.setText("-");
		buildingNameLabel.setText("건물: -");
		floorLabel.setText("-");
		overviewArea.setText("-");
		descriptionArea.setText("-");
		operatingHoursLabel.setText("-");
		noticeLabel.setText("-");
		tipsArea.setText("-");
	}
}
