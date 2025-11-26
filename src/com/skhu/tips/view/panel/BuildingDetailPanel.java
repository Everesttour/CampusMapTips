package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout; 
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import com.skhu.tips.model.entity.Building;

/**
 * 건물 상세 정보를 표시하는 패널 (모서리 둥글게 처리)
 */
public class BuildingDetailPanel extends JPanel {
	
	private int cornerRadius = 25; // 모서리 둥글기 정도 (픽셀)
	
	private JPanel roomsPanel;
	private JPanel floorBlocksContainer; // 층별 블록을 담을 새로운 컨테이너
	
	// 정보 표시용 컴포넌트
	private JLabel idLabel;
	private JLabel nameLabel;
	
	// --- 색상 정의 ---
	private static final Color BACKGROUND_COLOR = new Color(230, 245, 255); // Pale Sky Blue (#E6F5FF)
	private static final Color BLOCK_BACKGROUND_COLOR = Color.WHITE; 
	
	private static final Color HEADER_COLOR = new Color(50, 50, 50); 
	private static final Color ACCENT_COLOR = new Color(70, 130, 180); 
    private static final Color EMPHASIS_COLOR = new Color(0, 102, 102); 
	private static final Color FLOOR_HEADER_COLOR = new Color(30, 60, 90); 
	private static final Color TEXT_COLOR = new Color(70, 70, 70); 
	
	public BuildingDetailPanel() {
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
    protected void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int arc = cornerRadius;
        
        RoundRectangle2D roundRect = 
            new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
        
        g2.clip(roundRect);

        // 배경을 채우기 전에 Graphics2D의 색상을 BACKGROUND_COLOR로 설정
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width, height, arc, arc);
        
        // super.paintComponent(g2); // JPanel의 paintComponent를 호출할 필요 없이 직접 배경을 그림
        
        g2.dispose();
    }
    
	/**
	 * 컴포넌트 초기화
	 */
	private void initializeComponents() {
		roomsPanel = createRoomsPanel();
		// roomsPanel은 하위 컴포넌트이므로 배경색을 지정하지 않고 투명하게 유지
		roomsPanel.setOpaque(false); 
        // 이전 코드에서 roomsPanel.setBackground(BACKGROUND_COLOR); 는 제거됨
	}
	
	/**
	 * 주요 시설 패널 생성
	 */
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
        // JScrollPane 배경을 투명하게 설정하여 메인 패널의 둥근 모서리가 보이도록 함
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
	
	/**
	 * 건물 번호/명 정보를 담는 헤더 패널 생성
	 */
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
	
	/**
	 * 단일 층 정보를 담는 블록 패널을 생성합니다.
	 */
	private JPanel createFloorBlock(String floorLabel, String roomInfo) {
	    JPanel block = new JPanel(new BorderLayout(10, 5));
	    block.setBackground(BLOCK_BACKGROUND_COLOR); // 흰색 블록 배경
	    
	    // 블록의 경계선과 패딩
	    Border lineBorder = BorderFactory.createLineBorder(new Color(220, 220, 220));
	    Border padding = BorderFactory.createEmptyBorder(10, 15, 10, 15);
	    // 얇은 경계선 + 내부 패딩
	    block.setBorder(BorderFactory.createCompoundBorder(lineBorder, padding));
	    
	    // 층 번호 (Header)
	    JLabel floorLabelComp = new JLabel(floorLabel);
	    floorLabelComp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
	    floorLabelComp.setForeground(FLOOR_HEADER_COLOR); 
	    block.add(floorLabelComp, BorderLayout.NORTH);
	    
	    // 시설 정보 (Content)
	    JTextArea roomArea = new JTextArea(roomInfo);
	    roomArea.setEditable(false);
	    roomArea.setLineWrap(true);
	    roomArea.setWrapStyleWord(true);
	    roomArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
	    roomArea.setForeground(TEXT_COLOR);
	    roomArea.setOpaque(false); 
	    
	    roomArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); 
	    
	    block.add(roomArea, BorderLayout.CENTER);
	    
	    // 블록의 크기가 내용에 따라 유동적으로 조정되도록 설정
	    block.setMaximumSize(new Dimension(Integer.MAX_VALUE, block.getPreferredSize().height));
	    
	    return block;
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
		
		floorBlocksContainer.removeAll();
		
		if (building == null) {
			clearDisplay();
			floorBlocksContainer.revalidate();
			floorBlocksContainer.repaint();
			return;
		}
		
		// 건물번호
		idLabel.setText("건물번호: " + building.getId());
		
		// 건물명
		nameLabel.setText(building.getName() != null ? building.getName() : "건물명을 불러올 수 없습니다.");
		
		// 주요 시설 목록
		if (building.getRooms() != null && !building.getRooms().isEmpty()) {
			String roomsText = building.getRooms();
			
			// 1. '/' 문자를 기준으로 층별 정보를 분리합니다.
			List<String> floorInfos = Arrays.asList(roomsText.split(" / "));
			
			for (String floorInfo : floorInfos) {
				floorInfo = floorInfo.trim();
				if (floorInfo.isEmpty()) continue;

				String floorLabel;
				String roomContent;
				
				// 2. 층:시설목록 형태로 분리합니다.
				int colonIndex = floorInfo.indexOf(':');
				
				// 층 정보 정규식 확인 (예: "1층", "2층", "5층")
				boolean isFloorSpecific = false;
				if (colonIndex > 0) {
					String prefix = floorInfo.substring(0, colonIndex);
					// 앞에 있는 문자열이 숫자 + '층' 또는 숫자 + '~층' 등의 층 정보 패턴인지 확인
					if (prefix.matches(".*[0-9]+[층|F|f|F층]")) { 
						isFloorSpecific = true;
					}
				}
				
				if (isFloorSpecific) {
					// 층 번호가 명확히 존재하는 경우 (예: "3층: 성공회역사자료관")
					floorLabel = floorInfo.substring(0, colonIndex + 1); // 예: "3층:"
					roomContent = floorInfo.substring(colonIndex + 1).trim(); 
				} else {
					// 층 번호가 없거나, 건물 전체를 나타내는 첫 번째 정보인 경우 (예: "중앙도서관", "학생회실, 동아리실...")
					floorLabel = "전체 시설"; 
					roomContent = floorInfo;
				}
				
				// 3. 층별 블록을 생성하고 컨테이너에 추가합니다.
				JPanel floorBlock = createFloorBlock(floorLabel, roomContent);
				floorBlocksContainer.add(floorBlock);
				
				// 블록 사이에 간격 추가 (10픽셀)
				floorBlocksContainer.add(Box.createRigidArea(new Dimension(0, 10)));
			}
		} else {
			// 시설 정보가 없는 경우
			JPanel emptyBlock = createFloorBlock("정보 없음", "이 건물에 대한 시설 정보가 등록되어 있지 않습니다.");
			floorBlocksContainer.add(emptyBlock);
		}
		
		// 변경된 내용을 즉시 반영
		floorBlocksContainer.revalidate();
		floorBlocksContainer.repaint();
	}
	
	/**
	 * 표시 내용을 초기화합니다.
	 */
	private void clearDisplay() {
		idLabel.setText("");
		nameLabel.setText("건물 정보를 선택해주세요.");
		floorBlocksContainer.removeAll();
	}
}
