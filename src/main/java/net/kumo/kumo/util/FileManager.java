package net.kumo.kumo.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component; // Service 대신 Component가 더 적절
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component // 유틸리티 클래스는 보통 Component로 등록합니다.
public class FileManager {
	
	// ★ 파일이 저장될 기본 경로 (프로젝트 내 static/uploads 폴더)
	// Mac/Linux/Windows 모두 호환되도록 설정
	private final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
	
	/**
	 * 파일을 저장하고 "웹에서 접근 가능한 경로(/uploads/파일명)"를 리턴한다.
	 */
	public String saveFile(MultipartFile file) {
		if (file.isEmpty()) {
			return null;
		}
		
		try {
			// 1. 디렉토리 생성 (없으면 만듦)
			File directory = new File(UPLOAD_DIR);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			
			// 2. 파일명 생성 (날짜_UUID.확장자)
			String originalFileName = file.getOriginalFilename();
			String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
			String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			String uuidString = UUID.randomUUID().toString();
			
			// 예: 20260208_a1b2c3...png
			String savedFileName = dateString + "_" + uuidString + extension;
			
			// 3. 실제 저장
			File destFile = new File(UPLOAD_DIR + savedFileName);
			file.transferTo(destFile);
			
			log.info("💾 파일 저장 완료: {}", destFile.getAbsolutePath());
			
			// ★ DB에 저장할 땐 "웹 경로"를 리턴해줘야 함 (/uploads/파일명)
			return "/uploads/" + savedFileName;
			
		} catch (IOException e) {
			log.error("파일 저장 실패", e);
			throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
		}
	}
	
	/**
	 * 파일 삭제 (필요할 때 사용)
	 */
	public boolean deleteFile(String fileName) {
		try {
			// /uploads/파일명 -> 실제 경로로 변환해서 삭제
			String actualFileName = fileName.replace("/uploads/", "");
			Path filePath = Paths.get(UPLOAD_DIR, actualFileName);
			return Files.deleteIfExists(filePath);
		} catch (IOException e) {
			log.error("파일 삭제 실패", e);
			return false;
		}
	}
}