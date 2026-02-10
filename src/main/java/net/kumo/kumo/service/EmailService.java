package net.kumo.kumo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {
	private final JavaMailSender javaMailSender;
	
	public void sendEmail(String toEmail, String title, String text){
		SimpleMailMessage emailForm = createEmailForm(toEmail, title, text);
			
			try {
				javaMailSender.send(emailForm);
			} catch (RuntimeException e){
				e.printStackTrace();
				throw new RuntimeException("이메일 발송에 실패했습니다" + e.getMessage());
			}
		
		}
		
	
	
	private SimpleMailMessage createEmailForm(String toEmail, String title,String text){
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject(title);
		message.setText(text);
		
		return message;
		
	}
	
	public String sendCertigicationMail(String email){
		String certificationNumber = createKey();
		String title = "[KUMO] 認証番号のお知らせ / 인증번호 안내";
		StringBuilder content = new StringBuilder();
		
		// 1. 일본어 (Main)
		content.append("KUMOをご利用いただきありがとうございます。\n");
		content.append("下記の認証番号を入力してください。\n\n");
		content.append("認証番号: ").append(certificationNumber).append("\n\n");
		content.append("※ 他人に知られないようご注意ください。\n");
		
		content.append("\n--------------------------------------------------\n\n");
		
		// 2. 한국어 (Sub)
		content.append("KUMO를 이용해 주셔서 감사합니다.\n");
		content.append("아래 인증번호를 입력해 주세요.\n\n");
		content.append("인증번호: ").append(certificationNumber).append("\n\n");
		content.append("※ 타인에게 노출되지 않도록 주의해 주세요.");
		
		sendEmail(email, title, content.toString());
		
		return certificationNumber;
		
	}
	
	public String createKey(){
		Random random = new Random();
		return "ㅋㅋ";
	}
	
	
}
