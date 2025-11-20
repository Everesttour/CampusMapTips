// 김상윤
package com.skhu.tips;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import com.skhu.tips.controller.MapController;
import com.skhu.tips.controller.MapControllerImpl;
import com.skhu.tips.controller.PanelController;
import com.skhu.tips.controller.PanelControllerImpl;
import com.skhu.tips.model.service.DataService;
import com.skhu.tips.model.service.DataServiceImpl;
import com.skhu.tips.view.map.MapPanel;
import com.skhu.tips.view.panel.MainLeftPanel;

public class Main {

	public static void main(String[] args) {
		// Swing UI는 항상 Event Dispatch Thread (EDT)에서 실행
		SwingUtilities.invokeLater(() -> {

			// 1. DataService 생성
			DataService dataService = new DataServiceImpl();


			// 2. 뷰 생성
			MainLeftPanel mainLeftPanel = new MainLeftPanel();
			// 3. 컨트롤러 생성
			PanelController panelController = new PanelControllerImpl();

			// 2. 뷰 생성
			MapPanel mapPanel = new MapPanel();
			// 3. 컨트롤러 생성
			MapController mapController = new MapControllerImpl();

            // 4. 뷰와 학교데이터와 생성된 컨트롤러 구현체끼리 서로의 인터페이스를 주입합니다.
            panelController.initialize(mainLeftPanel, dataService, mapController);
            mapController.initialize(mapPanel, dataService, panelController);

			// 5. UI 조립 및 화면 출력
			assembleAndLaunchUI(mainLeftPanel, mapPanel);
		});
	}

	// ---------------------------------------------------------
	// UI 설정 및 실행 헬퍼 함수
	// ---------------------------------------------------------

	/**
	 * @brief 프레임에 주요 뷰들을 배치하고 화면에 표시하는 최종 함수입니다.
	 */
	private static void assembleAndLaunchUI(JPanel leftView, JPanel rightView) {
		// 메인 프레임
		JFrame frame = new JFrame("대학 꿀팁 지도");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1200, 800);
		frame.setLayout(new BorderLayout());

		// 두 개의 뷰를 가로로 분할하는 JSplitPane 생성
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftView, rightView);

		// 분할선 초기 위치 설정 (LeftView의 너비)
		splitPane.setDividerLocation(350);

		// 분할된 뷰를 프레임의 중앙에 추가
		frame.add(splitPane, BorderLayout.CENTER);

		// 프레임 위치 설정 및 표시
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}