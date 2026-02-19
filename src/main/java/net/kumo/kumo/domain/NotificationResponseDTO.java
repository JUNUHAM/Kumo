package net.kumo.kumo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
	// JS에서 data.id 혹은 data.notificationId로 받을 수 있게 명시
	@JsonProperty("notificationId")
	private Long notificationId;
	
	// ★ 제일 많이 틀리는 부분!
	// Lombok은 getIsRead()를 만들지만 JSON은 'read'나 'isRead'로 헷갈릴 수 있음.
	// 명확하게 'read'로 보내라고 지정.
	@JsonProperty("read")
	private boolean isRead;
	
	private String title;
	private String content;
	private String type;
	private String targetUrl;
	
	@JsonProperty("createdAt")
	private String createdAt; // 혹은 LocalDateTime
}
