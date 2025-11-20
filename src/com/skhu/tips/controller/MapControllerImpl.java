package com.skhu.tips.controller;

import com.skhu.tips.model.entity.Building;
import com.skhu.tips.model.entity.Facility;
import com.skhu.tips.model.service.DataService;
import com.skhu.tips.view.map.MapPanel;

/**
 * @class MapControllerImpl
 * @brief MapController 인터페이스의 구현체 (템플릿)
 */
public class MapControllerImpl implements MapController {

    private MapPanel mapPanel;
    private DataService dataService;
    private PanelController panelController;

    // --- MapController Interface Implementation ---

    // TODO: 김준 (구현 필요)

    /**
     * @brief 시스템 시작 후 처음에 실행되어 외부 클래스들을 주입받아 컨트롤러를 구성
     */
    @Override
    public void initialize(MapPanel mapPanel, DataService dataService, PanelController panelController) {
        this.mapPanel = mapPanel;
        this.dataService = dataService;
        this.panelController = panelController;
    }

    @Override
    public void focusOn(Building building) {

    }

    @Override
    public void focusOn(Facility facility) {

    }

    @Override
    public void switchToBuildingView() {

    }

    @Override
    public void switchToFacilityView() {

    }


    // --- Private Helper Methods ---

    // private void attachViewListeners() {
    //     ...
    // }

    // private void handleMapClick(int x, int y) {
    //     ...
    // }
}