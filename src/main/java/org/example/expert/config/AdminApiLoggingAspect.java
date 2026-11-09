
package org.example.expert.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminApiLoggingAspect {

	private final ObjectMapper objectMapper;

	@Around(
		"execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))"
			+ " || execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))"
	)
	public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {

		ServletRequestAttributes attributes =
			(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		HttpServletRequest request = attributes.getRequest();

		Object userId = request.getAttribute("userId");

		LocalDateTime requestTime = LocalDateTime.now();
		String requestUrl = request.getRequestURL().toString();

		// Controller 메서드에 전달되는 인자
		Object[] requestBody = joinPoint.getArgs();

		Map<String, Object> logData = new LinkedHashMap<>();

		logData.put("userId", userId);
		logData.put("requestTime", requestTime);
		logData.put("requestUrl", requestUrl);
		logData.put("requestBody", requestBody);

		try {
			// 실제 Controller 메서드 실행
			Object responseBody = joinPoint.proceed();

			logData.put("responseBody", responseBody);

			log.info("Admin API Log: {}", toJson(logData));

			return responseBody;

		} catch (Throwable e) {

			logData.put("responseBody", e.getMessage());

			log.error("Admin API Error Log: {}", toJson(logData), e);

			throw e;
		}
	}

	private String toJson(Map<String, Object> data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			log.error("API 로그 JSON 변환 실패", e);
			return data.toString();
		}
	}
}

