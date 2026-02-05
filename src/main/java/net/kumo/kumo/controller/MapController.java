package net.kumo.kumo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("map")
@RequiredArgsConstructor
public class MapController {
	
	// Key
	@Value("${GOOGLE_MAPS_KEY}")
	private String googleMapKey;
	
	
	/**
	 * 지도 메인페이지 연결
	 * @return 메인 페이지
	 */
	@GetMapping("main")
	public String mainMap(Model model) {
		log.debug("메인화면 연결");
		
		model.addAttribute("googleMapsKey", googleMapKey);
		return "mainView/main";
	}
}