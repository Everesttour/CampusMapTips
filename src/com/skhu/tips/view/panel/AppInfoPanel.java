// 조민성
package com.skhu.tips.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @class AppInfoPanel
 * @brief 앱 정보(소개, 사용법, 범례 등)를 표시하는 패널입니다.
 * 상세 정보 패널과 동일한 핑크/화이트 테마 및 둥근 모서리를 적용합니다.
 */
public class AppInfoPanel extends JPanel {
   
   private int cornerRadius = 25; // 모서리 둥글기 정도 (픽셀)
   
   // --- 색상 정의 (핑크 & 화이트 테마) ---
   private static final Color BACKGROUND_COLOR = new Color(155, 230, 240); //- 메인 패널 배경
    private static final Color BLOCK_BACKGROUND_COLOR = Color.WHITE; // 정보/꿀팁 블록 배경 (흰색)
   private static final Color HEADER_COLOR = new Color(80, 30, 50); // Dark Plum (#501E32) - 제목
   private static final Color TEXT_COLOR = new Color(100, 50, 70); // 일반 텍스트 색상
   
   private JTextArea contentArea;

   public AppInfoPanel() {
      super(new BorderLayout(15, 15)); 
      setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
      setBackground(BACKGROUND_COLOR); 
      setOpaque(false); // 둥근 모서리 구현을 위해 불투명도 false로 설정
      initializeContent();
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
    * 정보 내용을 초기화하고 레이아웃에 추가합니다.
    */
   private void initializeContent() {
      // 제목 패널
      JLabel titleLabel = new JLabel("🗺️ 성공회대학교 시설 가이드 앱 정보");
      titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
      titleLabel.setForeground(HEADER_COLOR);
      titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 5));
      add(titleLabel, BorderLayout.NORTH);
      
      // 내용 영역 (JTextArea)
      contentArea = new JTextArea();
      contentArea.setEditable(false);
      contentArea.setLineWrap(true);
      contentArea.setWrapStyleWord(true);
      contentArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
      contentArea.setForeground(TEXT_COLOR);
      contentArea.setBackground(BLOCK_BACKGROUND_COLOR); // 흰색 배경
      contentArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // 내부 패딩
      
      // 마크다운 형식의 내용을 JTextArea에 삽입
      String infoContent = 
         "## 1. 앱 소개 (Welcome)\n\n" +
         "환영합니다! 이 앱은 성공회대학교 캠퍼스 내 주요 시설의 위치, 상세 정보, 그리고 유용한 '꿀팁'을 빠르고 쉽게 찾아볼 수 있도록 제작되었습니다. 학교 생활을 편리하게 만드는 데 도움이 되기를 바랍니다.\n\n" +
         "## 2. 주요 기능 및 사용 방법\n\n" +
         "이 가이드를 통해 필요한 시설 정보를 쉽게 찾아보세요.\n\n" +
         "1. 시설 선택: 지도 상의 **번호 아이콘**을 클릭하거나, 좌측의 **시설 목록**에서 원하는 시설을 선택합니다.\n\n" +
         "2. 상세 정보 확인: 선택된 시설의 **개요, 공지사항, 층수, 운영 시간, 설명** 등의 정보가 오른쪽 상세 패널에 표시됩니다.\n\n" +
         "3. 꿀팁 활용: 상세 정보 패널 우측의 **💡 꿀팁** 섹션을 확인하여, 시설 이용 시 알아두면 좋은 학생들의 팁을 얻으세요.\n\n" +
         "4. 지도 조작: 지도를 드래그하여 이동하고, 스크롤 또는 핀치 동작으로 확대/축소가 가능합니다.\n\n" +
         "## 3. 아이콘 범례 (Legend)\n\n" +
         "지도에 표시된 각 번호 아이콘은 건물의 번호를 나타냅니다.\n\n" +
         "| 번호 | 아이콘 의미 | 예시 시설 |\n" +
         "| :---: | :---: | :---: |\n" +
         "| 01~13 | 주요 건물 / 승연관, 일만관, 월당관, 나눔관, 이천환기념관, 새천년관, 중앙도서관, 성미가엘성당,피츠버그홀, 구두인관, 미가엘관, 성베드로학교, 행복기숙사 |\n" +
         "| 14 | 외부 시설 |  |\n\n" +
         "## 4. 앱 개발 정보\n\n" +
         "* **버전:** 1.0.0 (2025년 11월)\n\n" +
         "* **제작:** 김주환 김상윤 김준 조민성\n\n" +
         "* **문의:** 앱 사용 중 오류나 건의 사항이 있으시면 [kimjuhwan6315@naver.com]로 연락 주시면 빠르게 개선하겠습니다.";
      
      contentArea.setText(infoContent);
      contentArea.setCaretPosition(0); // 스크롤을 맨 위로 설정
      
      JScrollPane scrollPane = new JScrollPane(contentArea);
      scrollPane.setBorder(BorderFactory.createLineBorder(BLOCK_BACKGROUND_COLOR, 10)); // 주변에 약간의 여백 제공
      scrollPane.getViewport().setBackground(BLOCK_BACKGROUND_COLOR); // 뷰포트도 흰색 배경
      
      add(scrollPane, BorderLayout.CENTER);
   }
}
