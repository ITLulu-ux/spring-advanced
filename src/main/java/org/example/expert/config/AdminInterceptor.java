
package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

	private final ObjectMapper objectMapper;

	@Override
	public boolean preHandle(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler
	) throws IOException {

		Object userId = request.getAttribute("userId");
		Object userRole = request.getAttribute("userRole");

		// 인증 정보가 없는 경우
		if (userId == null || userRole == null) {
			sendErrorResponse(
				response,
				HttpStatus.UNAUTHORIZED,
				"인증이 필요합니다."
			);
			return false;
		}

		// ADMIN 권한이 없는 경우
		if (!UserRole.ADMIN.name().equals(userRole.toString())) {
			log.warn("어드민 권한 부족: userId={}, URI={}",
				userId, request.getRequestURI());

			sendErrorResponse(
				response,
				HttpStatus.FORBIDDEN,
				"접근 권한이 없습니다."
			);
			return false;
		}

		// 인증 성공
		log.info(
			"어드민 API 접근: userId={}, time={}, URL={}",
			userId,
			LocalDateTime.now(),
			request.getRequestURI()
		);

		return true;
	}

	private void sendErrorResponse(
		HttpServletResponse response,
		HttpStatus status,
		String message
	) throws IOException {

		response.setStatus(status.value());
		response.setContentType("application/json;charset=UTF-8");

		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", status.name());
		errorResponse.put("code", status.value());
		errorResponse.put("message", message);

		response.getWriter().write(
			objectMapper.writeValueAsString(errorResponse)
		);
	}
}

