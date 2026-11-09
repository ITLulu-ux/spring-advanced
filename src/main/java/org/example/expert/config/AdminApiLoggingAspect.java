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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
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

		RequestAttributes requestAttributes =
			RequestContextHolder.getRequestAttributes();

		if (!(requestAttributes instanceof ServletRequestAttributes attributes)) {
			log.warn("HTTP 요청 정보를 가져올 수 없습니다.");
			return joinPoint.proceed();
		}

		HttpServletRequest request = attributes.getRequest();

		Map<String, Object> logData = new LinkedHashMap<>();

		logData.put("userId", request.getAttribute("userId"));
		logData.put("requestTime", LocalDateTime.now());
		logData.put("requestUrl", request.getRequestURL().toString());

		// Controller 메서드에 전달된 전체 인자
		logData.put("requestArguments", joinPoint.getArgs());

		try {
			Object responseBody = joinPoint.proceed();

			logData.put("responseBody", responseBody);

			log.info("Admin API Log: {}", toJson(logData));

			return responseBody;

		} catch (Throwable e) {

			logData.put("errorMessage", e.getMessage());

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

