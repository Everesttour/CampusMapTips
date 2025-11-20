// 김준
package com.skhu.tips.view.map;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class MapPanel extends JPanel {

	// 지도 이미지 경로
	private static final String MAP_IMAGE_PATH = "src/resources/images/map/campus_map.jpg";

	// 지도를 담을 라벨 (이미지 컨테이너 역할)
	private JLabel mapLabel;

	// 원본 이미지 객체 (축소 시 사용)
	private Image originalImage;

	public MapPanel() {
		// 기본 레이아웃 사용
		super(null);

		// 맵 이미지 로드 (원본 이미지를 메모리에 로드)
		loadOriginalMapImage();

		// 맵 라벨 초기화
		mapLabel = new JLabel();

		// 라벨을 패널에 추가
		this.add(mapLabel);

		// 패널 크기가 변경될 때마다 이미지 크기를 조정하도록 리스너 추가
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				resizeAndRepaintMap();
			}
		});

		// 초기 크기 조정
		resizeAndRepaintMap();
	}

	/**
	 * @brief 원본 지도 이미지 파일을 메모리에 로드합니다.
	 */
	private void loadOriginalMapImage() {
		try {
			File file = new File(MAP_IMAGE_PATH);
			if (file.exists()) {
				originalImage = ImageIO.read(file);
				System.out.println("[MapPanel] 원본 지도 이미지 로드 성공.");
			} else {
				System.err.println("[MapPanel] 지도 이미지 파일을 찾을 수 없습니다: " + MAP_IMAGE_PATH);
				originalImage = null;
			}
		} catch (IOException e) {
			System.err.println("[MapPanel] 지도 이미지 로드 중 오류 발생: " + e.getMessage());
			originalImage = null;
		}
	}

	/**
	 * @brief 현재 패널 크기에 맞춰 이미지를 축소하고 다시 그립니다.
	 */
	private void resizeAndRepaintMap() {
		if (originalImage == null) {
			mapLabel.setText("지도 이미지 로드 실패");
			mapLabel.setBounds(0, 0, getWidth(), getHeight());
			return;
		}

		int panelWidth = getWidth();
		int panelHeight = getHeight();

		if (panelWidth <= 0 || panelHeight <= 0) {
			return; // 크기가 0이면 처리하지 않음
		}

		int originalWidth = originalImage.getWidth(null);
		int originalHeight = originalImage.getHeight(null);

		// 1. 비율 계산
		double widthRatio = (double) panelWidth / originalWidth;
		double heightRatio = (double) panelHeight / originalHeight;

		// 2. 패널에 꽉 차도록 하되, 비율은 유지하는 축소 비율 선택 (더 작은 비율 선택)
		double scaleRatio = Math.min(widthRatio, heightRatio);

		// 3. 새로운 크기 계산
		int newWidth = (int) (originalWidth * scaleRatio);
		int newHeight = (int) (originalHeight * scaleRatio);

		// 4. 리사이즈된 이미지 생성
		Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

		// 5. JLabel에 새로운 이미지 설정
		mapLabel.setIcon(new ImageIcon(resizedImage));
		mapLabel.setText(null);

		// 6. JLabel 위치 조정 (패널 중앙에 배치)
		int x = (panelWidth - newWidth) / 2;
		int y = (panelHeight - newHeight) / 2;
		mapLabel.setBounds(x, y, newWidth, newHeight);

		// 화면 갱신
		repaint();
	}
}