package com.skhu.tips.controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;
import com.skhu.tips.model.service.DataService; // (DataServiceImpl이 아닌 인터페이스)
import com.skhu.tips.view.panel.BuildingDetailPanel;
import com.skhu.tips.view.panel.FacilityDetailPanel;
import com.skhu.tips.view.panel.MainLeftPanel;

/**
 * @class PanelControllerImpl
 * @brief 뷰의 컴포넌트에 직접 리스너를 등록합니다.
 */
public class PanelControllerImpl implements PanelController {

    private final MainLeftPanel mainLeftPanel;
    private final DataService dataService;
    private MapController mapController;
    
    // 디테일 패널들
    private BuildingDetailPanel buildingDetailPanel;
    private FacilityDetailPanel facilityDetailPanel;
    
    // 디테일 다이얼로그들
    private JDialog buildingDialog;
    private JDialog facilityDialog;

    public PanelControllerImpl(MainLeftPanel mainLeftPanel, DataService dataService) {
        this.mainLeftPanel = mainLeftPanel;
        this.dataService = dataService;

        // 1. 디테일 패널 초기화
        buildingDetailPanel = new BuildingDetailPanel();
        facilityDetailPanel = new FacilityDetailPanel();

        // 2. 뷰에 데이터 채우기 (컨트롤러의 역할)
        this.mainLeftPanel.setBuildingListData(dataService.getBuildings());
        this.mainLeftPanel.setFacilityListData(dataService.getFacilities());

        // 3. 뷰의 컴포넌트에 직접 리스너를 등록
        attachViewListeners();
    }

    /**
     * @brief 뷰(MainLeftPanel)의 스윙 컴포넌트에 이벤트 리스너를 등록합니다.
     */
    private void attachViewListeners() {

        // 1. 건물 리스트(JList)에 리스너 등록
        mainLeftPanel.getBuildingList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Building selected = mainLeftPanel.getBuildingList().getSelectedValue();

                if (selected != null) {
                    System.out.println("건물 선택됨: " + selected.getName()); // 디버깅용
                    // 2. 컨트롤러의 핵심 로직: MapController에게 명령 (null 체크)
                    if (mapController != null) {
                        mapController.focusOn(selected);
                    }
                    // mapController가 null이어도 detail panel은 열 수 있음
                    openBuildingDetail(selected);
                }
            }
        });

        // 2. 시설 리스트(JList)에도 리스너 등록
        mainLeftPanel.getFacilityList().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Facility selected = mainLeftPanel.getFacilityList().getSelectedValue();
                if (selected != null) {
                    System.out.println("시설 선택됨: " + selected.getName()); // 디버깅용
                    // mapController가 null이어도 detail panel은 열 수 있음
                    if (mapController != null) {
                        mapController.focusOn(selected);
                    }
                    openFacilityDetail(selected);
                }
            }
        });

        // 3. 카테고리 버튼 클릭 리스너 등록
        mainLeftPanel.getBuildingButton().addActionListener(e -> switchToBuildingView());
        mainLeftPanel.getFacilityButton().addActionListener(e -> switchToFacilityView());
    }

    // --- PanelController 인터페이스 구현 ---

    @Override
    public void switchToBuildingView() {
        mainLeftPanel.switchToBuildingView();
        if (mapController != null) {
            mapController.switchToBuildingView();
        }
    }

    @Override
    public void switchToFacilityView() {
        mainLeftPanel.switchToFacilityView();
        if (mapController != null) {
            mapController.switchToFacilityView();
        }
    }

    @Override
    public void openBuildingDetail(Building building) {
        if (building == null) {
            System.out.println("openBuildingDetail: building이 null입니다."); // 디버깅용
            return;
        }
        
        System.out.println("openBuildingDetail 호출됨: " + building.getName()); // 디버깅용
        
        // 패널에 건물 정보 표시
        buildingDetailPanel.displayBuilding(building);
        
        // 다이얼로그가 없거나 닫혀있으면 새로 생성
        if (buildingDialog == null || !buildingDialog.isVisible()) {
            // 부모 프레임 찾기
            JFrame parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(mainLeftPanel);
            
            buildingDialog = new JDialog(parentFrame, "건물 상세 정보", false);
            buildingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            buildingDialog.add(buildingDetailPanel);
            buildingDialog.setSize(900, 600);
            buildingDialog.setLocationRelativeTo(parentFrame);
            
            // 다이얼로그가 닫힐 때 참조 제거
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
            System.out.println("openFacilityDetail: facility가 null입니다."); // 디버깅용
            return;
        }
        
        System.out.println("openFacilityDetail 호출됨: " + facility.getName()); // 디버깅용
        
        // 패널에 시설 정보 표시
        facilityDetailPanel.displayFacility(facility);
        
        // 다이얼로그가 없거나 닫혀있으면 새로 생성
        if (facilityDialog == null || !facilityDialog.isVisible()) {
            // 부모 프레임 찾기
            JFrame parentFrame = (JFrame) javax.swing.SwingUtilities.getWindowAncestor(mainLeftPanel);
            
            facilityDialog = new JDialog(parentFrame, "시설 상세 정보", false);
            facilityDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            facilityDialog.add(facilityDetailPanel);
            facilityDialog.setSize(900, 600);
            facilityDialog.setLocationRelativeTo(parentFrame);
            
            // 다이얼로그가 닫힐 때 참조 제거
            facilityDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    facilityDialog = null;
                }
            });
        }
        
        facilityDialog.setVisible(true);
    }

    @Override
    public void setMapController(MapController mapController) {
        this.mapController = mapController;
    }
}