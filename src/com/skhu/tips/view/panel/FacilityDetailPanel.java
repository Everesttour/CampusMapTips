// 조민성
package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
// 둥근 모서리 구현을 위해 추가된 Graphics 관련 Import
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
// ----------------------------------------------------

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.skhu.tips.model.entity.Facility;

/**
 * 시설 상세 정보를 표시하는 패널 (핑크 배경, 흰색 정보 블록, 둥근 모서리 적용)
 * 중앙 정보 패널 내부의 모든 정보 블록이 수직 스택으로 배치됨.
 */
public class FacilityDetailPanel extends JPanel {
	
	private int cornerRadius = 25; // 모서리 둥글기 정도 (픽셀)
	
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
	
	// --- 색상 정의 (핑크 & 화이트 테마) ---
	private static final Color BACKGROUND_COLOR = new Color(255, 230, 240); // Pale Pink (#FFE6F0) - 메인 패널 배경
    private static final Color BLOCK_BACKGROUND_COLOR = Color.WHITE; // 정보/꿀팁 블록 배경 (요청에 따라 흰색)
	private static final Color ACCENT_COLOR = new Color(200, 90, 120); // Rose Pink (#C85A78) - 시설명 등 강조색
	private static final Color HEADER_COLOR = new Color(80, 30, 50); // Dark Plum (#501E32) - 일반 텍스트 및 제목
	private static final Color TEXT_COLOR = new Color(100, 50, 70); // 일반 텍스트 색상
	private static final Color BORDER_COLOR = new Color(220, 200, 210); // Light Grayish Pink (#DCC8D2) - 테두리 및 TitledBorder
	private static final Color TIPS_EMPHASIS_COLOR = new Color(180, 50, 80); // Deep Raspberry (#B43250) - 꿀팁 제목 강조색

	public FacilityDetailPanel() {
		super(new BorderLayout(15, 15)); 
		setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); 
		setBackground(BACKGROUND_COLOR); 
		setOpaque(false); // 둥근 모서리 구현을 위해 불투명도 false로 설정
		initializeComponents();
		setupLayout();
	}
	
    /**
     * 패널의 배경을 둥근 사각형으로 그립니다.
     */
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
	
	/**
	 * 컴포넌트 초기화
	 */
	private void initializeComponents() {
		infoPanel = createInfoPanel();
		tipsPanel = createTipsPanel();
	}
	
	/**
	 * TitledBorder 스타일 생성 도우미 메소드
	 */
	private TitledBorder createStyledTitledBorder(String title) {
	    TitledBorder border = BorderFactory.createTitledBorder(
	        BorderFactory.createLineBorder(BORDER_COLOR, 1), 
	        title
	    );
	    border.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
	    border.setTitleColor(HEADER_COLOR); 
	    return border;
	}
	
	/**
	 * 수직 스택 정보 블록 컴포넌트 생성 도우미
	 * @param title 블록 제목
	 * @return 내용물을 표시할 JLabel 인스턴스
	 */
	private JLabel createVerticalInfoBlock(String title) {
		// TitledBorder 내부의 내용을 담을 JLabel
	    JLabel contentLabel = new JLabel("-");
	    contentLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
	    contentLabel.setForeground(TEXT_COLOR);
	    contentLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8)); // 내부 패딩
	    contentLabel.setHorizontalAlignment(JLabel.LEFT); 
	    contentLabel.setOpaque(true);
	    contentLabel.setBackground(BLOCK_BACKGROUND_COLOR);
	    
	    // TitledBorder를 가진 외부 패널
	    JPanel blockPanel = new JPanel(new BorderLayout());
	    blockPanel.setOpaque(true);
	    blockPanel.setBackground(BLOCK_BACKGROUND_COLOR);
	    blockPanel.setBorder(createStyledTitledBorder(title));
	    blockPanel.add(contentLabel, BorderLayout.CENTER);
	    
	    // 높이 고정을 위해 최대 크기 설정
	    blockPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, blockPanel.getPreferredSize().height));
	    
	    return contentLabel;
	}

	/**
	 * 정보 패널 생성 (흰색 블록, 수직 스택)
	 */
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10)); // 이 패널은 TitleBorder와 Header를 분리하기 위해 BorderLayout 사용
		panel.setBackground(BLOCK_BACKGROUND_COLOR); 
		panel.setOpaque(true); 
		panel.setBorder(createStyledTitledBorder("시설 정보")); 
		
		// 1. 상단: 이름, 건물명
		JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
		headerPanel.setOpaque(true); 
		headerPanel.setBackground(BLOCK_BACKGROUND_COLOR);
		
		nameLabel = new JLabel();
		nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22)); 
		nameLabel.setForeground(ACCENT_COLOR); 
		headerPanel.add(nameLabel, BorderLayout.NORTH);
		
		buildingNameLabel = new JLabel();
		buildingNameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		buildingNameLabel.setForeground(TEXT_COLOR);
		headerPanel.add(buildingNameLabel, BorderLayout.CENTER);
		
		panel.add(headerPanel, BorderLayout.NORTH);
		
		// 2. 중앙 컨텐츠 영역 (수직 스택)
		JPanel verticalStackPanel = new JPanel();
		verticalStackPanel.setLayout(new BoxLayout(verticalStackPanel, BoxLayout.Y_AXIS));
		verticalStackPanel.setOpaque(true);
		verticalStackPanel.setBackground(BLOCK_BACKGROUND_COLOR);
		
		// 2-1. 개요 (Overview) - 고정 높이 100
		overviewArea = new JTextArea();
		overviewArea.setEditable(false);
		overviewArea.setLineWrap(true);
		overviewArea.setWrapStyleWord(true);
		overviewArea.setOpaque(true); 
		overviewArea.setBackground(BLOCK_BACKGROUND_COLOR);
		overviewArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		overviewArea.setForeground(TEXT_COLOR);
		JScrollPane overviewScroll = new JScrollPane(overviewArea);
		overviewScroll.setOpaque(true); 
		overviewScroll.getViewport().setBackground(BLOCK_BACKGROUND_COLOR); 
		overviewScroll.setBorder(createStyledTitledBorder("개요")); 
		overviewScroll.setPreferredSize(new Dimension(0, 100));
		overviewScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // 높이 고정
		
		verticalStackPanel.add(overviewScroll);
		verticalStackPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
		
		// 2-2. 공지사항 (Notice) - 수직 블록
		noticeLabel = createVerticalInfoBlock("공지사항");
		verticalStackPanel.add(noticeLabel.getParent());
		verticalStackPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// 2-3. 층수 (Floor) - 수직 블록
		floorLabel = createVerticalInfoBlock("층수");
		verticalStackPanel.add(floorLabel.getParent());
		verticalStackPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// 2-4. 운영 시간 (Operating Hours) - 수직 블록
		operatingHoursLabel = createVerticalInfoBlock("운영 시간");
		verticalStackPanel.add(operatingHoursLabel.getParent());
		verticalStackPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		// 2-5. 설명 (Description) - 남은 공간을 모두 차지
		descriptionArea = new JTextArea();
		descriptionArea.setEditable(false);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setOpaque(true); 
		descriptionArea.setBackground(BLOCK_BACKGROUND_COLOR);
		descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		descriptionArea.setForeground(TEXT_COLOR);
		JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
		descriptionScroll.setOpaque(true); 
		descriptionScroll.getViewport().setBackground(BLOCK_BACKGROUND_COLOR);
		descriptionScroll.setBorder(createStyledTitledBorder("설명")); 
		
		verticalStackPanel.add(descriptionScroll);
		verticalStackPanel.add(Box.createVerticalGlue()); // 남은 공간을 채우도록 강제
		
		panel.add(verticalStackPanel, BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * 꿀팁 패널 생성 (흰색 블록 + 전구 아이콘)
	 */
	private JPanel createTipsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BLOCK_BACKGROUND_COLOR); 
		panel.setOpaque(true); 
		
		// 꿀팁 TitledBorder의 제목에 전구 아이콘(💡) 추가
		TitledBorder tipsBorder = BorderFactory.createTitledBorder(
		    BorderFactory.createLineBorder(BORDER_COLOR, 1), 
		    "💡 꿀팁" 
		);
		tipsBorder.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		tipsBorder.setTitleColor(TIPS_EMPHASIS_COLOR); // 꿀팁 제목 강조색 적용
		panel.setBorder(tipsBorder);
		
		panel.setPreferredSize(new Dimension(250, 0));
		
		tipsArea = new JTextArea();
		tipsArea.setEditable(false);
		tipsArea.setLineWrap(true);
		tipsArea.setWrapStyleWord(true);
		tipsArea.setOpaque(true); 
		tipsArea.setBackground(BLOCK_BACKGROUND_COLOR);
		tipsArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		tipsArea.setForeground(TEXT_COLOR);
		
		JScrollPane scrollPane = new JScrollPane(tipsArea);
		scrollPane.setOpaque(true); 
		scrollPane.getViewport().setBackground(BLOCK_BACKGROUND_COLOR); 
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
		nameLabel.setText((name != null && !name.trim().isEmpty()) ? name : "시설명을 불러올 수 없습니다.");
		
		// 건물명
		String buildingName = facility.getBuildingName();
		buildingNameLabel.setText("건물: " + ((buildingName != null && !buildingName.trim().isEmpty()) ? buildingName : "-"));
		
		// 층수
		int floor = facility.getFloor();
		String floorText;
		if (floor == 0) {
			floorText = "정보 없음"; 
		} else if (floor < 0) {
			floorText = "지하 " + Math.abs(floor) + "층";
		} else {
			floorText = floor + "층";
		}
		floorLabel.setText(floorText);
		
		// 개요
		String overview = facility.getOverview();
		overviewArea.setText((overview != null && !overview.trim().isEmpty()) ? overview : "개요 정보가 없습니다.");
		overviewArea.setCaretPosition(0);
		
		// 설명
		String description = facility.getDescription();
		descriptionArea.setText((description != null && !description.trim().isEmpty()) ? description : "설명 정보가 없습니다.");
		descriptionArea.setCaretPosition(0);
		
		// 운영시간
		String operatingHours = facility.getOperatingHours();
		operatingHoursLabel.setText((operatingHours != null && !operatingHours.trim().isEmpty()) ? operatingHours : "정보 없음");
		
		// 공지사항
		String notice = facility.getNotice();
		noticeLabel.setText((notice != null && !notice.trim().isEmpty()) ? notice : "정보 없음");
		
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
			tipsArea.setText(hasValidTip ? tipsText.toString() : "등록된 꿀팁이 없습니다.");
		} else {
			tipsArea.setText("등록된 꿀팁이 없습니다.");
		}
		tipsArea.setCaretPosition(0);
	}
	
	/**
	 * 표시 내용을 초기화합니다.
	 */
	private void clearDisplay() {
		nameLabel.setText("시설명을 선택해주세요.");
		buildingNameLabel.setText("건물: -");
		floorLabel.setText("정보 없음");
		overviewArea.setText("개요 정보가 없습니다.");
		descriptionArea.setText("설명 정보가 없습니다.");
		operatingHoursLabel.setText("정보 없음");
		noticeLabel.setText("정보 없음");
		tipsArea.setText("등록된 꿀팁이 없습니다.");
	}
}
