package net.kumo.kumo.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kumo.kumo.domain.dto.SeekerMyPageDTO;
import net.kumo.kumo.domain.entity.ProfileImageEntity;
import net.kumo.kumo.domain.entity.UserEntity;
import net.kumo.kumo.repository.ProfileImageRepository;
import net.kumo.kumo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SeekerService {
		private final UserRepository userRepository;
		private final ProfileImageRepository profileImageRepository;
	
	@Value("${file.upload.dir}")
	private String uploadDir; // application.properties에서 가져옴 (C:/KumoUpload/)
		
	
	public SeekerMyPageDTO getDTO(String username) {
		UserEntity userEntity = userRepository.findByEmail(username)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. email=" + username));
		return SeekerMyPageDTO.EntityToDto(userEntity);
		
	}
	
	public String updateProfileImage(String username, MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드할 파일이 없습니다.");
		}
		//DB집어넣기
		UserEntity userentity = userRepository.findByEmail(username).orElseThrow(()-> new IllegalArgumentException("해당유저없음"));
		
		
		// 파일 저장 경로 설정
		String profileFolder = "profileImage/";
		String absolutePath = uploadDir + profileFolder;
		
		File folder = new File(absolutePath);
		if(!folder.exists()){folder.mkdirs();}
		
		// 파일명 생성
		String originalFileName = file.getOriginalFilename();
		String uuid = UUID.randomUUID().toString(); //브라우저 캐시 방지위해 파일이름 앞에 뭐 붙임
		String saveFileName = uuid + "_" + originalFileName;
		
		log.info("파일이름 : {}",saveFileName);
		
		//물리적 파일 저장
		File saveFile = new File(absolutePath,saveFileName);
		file.transferTo(saveFile);
		
		// 브라우저 접근용 URL 설정
		String fileUrl = "/uploads/" + profileFolder + saveFileName;
		
		// 기존 프로필 존재 여부
		ProfileImageEntity existingImage = userentity.getProfileImage();
		if(existingImage !=null){
			File oldFile = new File(absolutePath, existingImage.getStoredFileName());
			if(oldFile.exists()){oldFile.delete();}
			existingImage.setOriginalFileName(file.getOriginalFilename());
			existingImage.setStoredFileName(saveFileName);
			existingImage.setFileUrl(fileUrl);
			existingImage.setFileSize(file.getSize());
		}else {
			ProfileImageEntity newImage = ProfileImageEntity.builder()
					.originalFileName(file.getOriginalFilename())
					.storedFileName(saveFileName)
					.fileUrl(fileUrl)
					.fileSize(file.getSize())
					.user(userentity)
					.build();
			userentity.setProfileImage(newImage);
			profileImageRepository.save(newImage);
					
		}
		
		return fileUrl;
		
	}
	
	private  void deleteOldProfileImage(String projectPath){
		File directory = new File(projectPath);
		
		// 경로가있는지 확인
		if(directory.exists() && directory.isDirectory()){
			File[] files= directory.listFiles();
			
			if(files != null && files.length>0){
			
			}
			
			
		}
		
	}
	
}
