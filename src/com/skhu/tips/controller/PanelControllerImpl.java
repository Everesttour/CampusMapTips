package com.skhu.tips.controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;
import com.skhu.tips.model.service.DataService;
import com.skhu.tips.view.panel.BuildingDetailPanel;
import com.skhu.tips.view.panel.FacilityDetailPanel;
import com.skhu.tips.view.panel.MainLeftPanel;

/**
 * @class PanelControllerImpl
 * @brief PanelController 인터페이스의 구현체. 2단계 초기화 패턴 적용.
 */
public class PanelControllerImpl implements PanelController {

    // --- 1. Fields (DI 및 상태) ---
    private MainLeftPanel mainLeftPanel;
    private DataService dataService;
    private MapController mapController;

    // 디테일 패널들
    private BuildingDetailPanel buildingDetailPanel;
    private FacilityDetailPanel facilityDetailPanel;

    // 디테일 다이얼로그들
    private JDialog buildingDialog;
    private JDialog facilityDialog;

    // =======================================================================
    // --- 2. Configuration & Initialization (초기화 로직 분리) ---
    // =======================================================================

    /**
     * @brief [Step 1] 의존성 주입 (Wiring)
     * 단순히 객체 주소를 변수에 저장하기만 합니다. 로직 충돌 방지.
     */
    @Override
    public void configure(MainLeftPanel mainLeftPanel, DataService dataService, MapController mapController) {
        this.mainLeftPanel = mainLeftPanel;
        this.dataService = dataService;
        this.mapController = mapController;
    }

    /**
     * @brief [Step 2] 초기 로직 실행 (Logic)
     * 모든 설정이 끝난 뒤 호출되어 뷰를 초기화하고 리스너를 등록합니다.
     */
    @Override
    public void loadInitialData() {
        // 1. 디테일 패널 초기화
        buildingDetailPanel = new BuildingDetailPanel();
        facilityDetailPanel = new FacilityDetailPanel();

        // 2. 뷰에 데이터 채우기
        if (this.mainLeftPanel != null && this.dataService != null) {
            this.mainLeftPanel.setBuildingListData(dataService.getBuildings());
            this.mainLeftPanel.setFacilityListData(dataService.getFacilities());
        }

        // 3. 뷰의 컴포넌트에 리스너 등록
        attachViewListeners();

        // 4. 초기 뷰 상태 설정
        showBuildingView();
    }

    // =======================================================================
    // --- 3. PanelController Interface Implementation (외부 노출 API) ---
    // =======================================================================

    /**
     * @brief [MapController 호출] 뷰를 건물 리스트 뷰로 전환합니다.
     */
    @Override
    public void switchToBuildingView() {
        showBuildingView();
    }

    /**
     * @brief [MapController 호출] 뷰를 시설 리스트 뷰로 전환합니다.
     */
    @Override
    public void switchToFacilityView() {
        showFacilityView();
    }

    // =======================================================================
    // --- 4. Private Logic Helpers (실제 로직 구현) ---
    // =======================================================================

    private void showBuildingView() {
        if (mainLeftPanel != null) {
            mainLeftPanel.showBuildingList();
            mainLeftPanel.updateButtonColors(true);
        }
        if (mapController != null) {
            mapController.switchToBuildingView();
        }
    }

    private void showFacilityView() {
        if (mainLeftPanel != null) {
            mainLeftPanel.showFacilityList();
            mainLeftPanel.updateButtonColors(false);
        }
        if (mapController != null) {
            mapController.switchToFacilityView();
        }
    }

    // =======================================================================
    // --- 5. Event Listener Registration (리스너 등록) ---
    // =======================================================================

    private void attachViewListeners() {
        if (mainLeftPanel == null) {
			return;
		}

        // 1. 건물 리스트 클릭 시
        mainLeftPanel.getBuildingList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Building selected = mainLeftPanel.getBuildingList().getSelectedValue();
                if (selected != null) {
                    System.out.println("건물 선택됨: " + selected.getName());
                    if (mapController != null) {
						mapController.focusOn(selected);
					}
                    openBuildingDetail(selected);
                }
            }
        });

        // 2. 시설 리스트 클릭 시
        mainLeftPanel.getFacilityList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Facility selected = mainLeftPanel.getFacilityList().getSelectedValue();
                if (selected != null) {
                    System.out.println("시설 선택됨: " + selected.getName());
                    if (mapController != null) {
						mapController.focusOn(selected);
					}
                    openFacilityDetail(selected);
                }
            }
        });

        // 3. 카테고리 버튼 클릭
        mainLeftPanel.getBuildingButton().addActionListener(e -> switchToBuildingView());
        mainLeftPanel.getFacilityButton().addActionListener(e -> switchToFacilityView());
    }

    @Override
    public void openBuildingDetail(Building building) {
        if (building == null) {
			return;
		}

        buildingDetailPanel.displayBuilding(building);

        if (buildingDialog == null || !buildingDialog.isVisible()) {
            JFrame parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(mainLeftPanel);
            buildingDialog = new JDialog(parentFrame, "건물 상세 정보", false);
            buildingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            buildingDialog.add(buildingDetailPanel);
            buildingDialog.setSize(900, 600);
            buildingDialog.setLocationRelativeTo(parentFrame);

            buildingDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    buildingDialog = null;
                }
            });
        }
        buildingDialog.setVisible(true);
    }

    @Override
    public void openFacilityDetail(Facility facility) {
        if (facility == null) {
			return;
		}

        facilityDetailPanel.displayFacility(facility);

        if (facilityDialog == null || !facilityDialog.isVisible()) {
            JFrame parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(mainLeftPanel);
            facilityDialog = new JDialog(parentFrame, "시설 상세 정보", false);
            facilityDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            facilityDialog.add(facilityDetailPanel);
            facilityDialog.setSize(900, 600);
            facilityDialog.setLocationRelativeTo(parentFrame);

            facilityDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    facilityDialog = null;
                }
            });
        }
        facilityDialog.setVisible(true);
    }
}