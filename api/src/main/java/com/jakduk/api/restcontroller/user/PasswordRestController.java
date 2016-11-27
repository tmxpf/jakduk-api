package com.jakduk.api.restcontroller.user;

import com.jakduk.core.common.CommonConst;
import com.jakduk.core.common.util.CoreUtils;
import com.jakduk.core.exception.ServiceError;
import com.jakduk.core.exception.ServiceException;
import com.jakduk.core.model.db.Token;
import com.jakduk.core.model.simple.UserProfile;
import com.jakduk.core.notification.EmailService;
import com.jakduk.core.repository.TokenRepository;
import com.jakduk.core.service.CommonService;
import com.jakduk.core.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.*;

@Slf4j
@Api(tags = "User", description = "회원 API")
@RestController
@RequestMapping("/api/password")
public class PasswordRestController {

	@Resource
	LocaleResolver localeResolver;

	@Autowired
	private TokenRepository tokenRepository;

	@Autowired
	private CommonService commonService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	// jakduk 비밀번호 찾기 처리.
	@RequestMapping(value = "/find", method = RequestMethod.POST)
	public Map<String, Object> findPassword(@RequestParam String email,
	                                        @RequestParam String callbackUrl,
											Locale locale) {
		String message = "";

		UserProfile userProfile = userService.findOneByEmail(email);

		if (Objects.isNull(userProfile)) {
			message = CoreUtils.getResourceBundleMessage( "messages.user", "user.msg.you.are.not.registered");
		} else {
			switch (userProfile.getProviderId()) {
				case JAKDUK:
					message = CoreUtils.getResourceBundleMessage("messages.user", "user.msg.reset.password.sendok");
					try {

						emailService.sendResetPassword(locale, callbackUrl, email);
					} catch (MessagingException e) {
						throw new ServiceException(ServiceError.SEND_EMAIL_FAILED);
					}
					break;
				case DAUM:
					message = CoreUtils.getResourceBundleMessage("messages.user", "user.msg.you.connect.with.sns", CommonConst.ACCOUNT_TYPE.DAUM);
					break;
				case FACEBOOK:
					message = CoreUtils.getResourceBundleMessage("messages.user", "user.msg.you.connect.with.sns", CommonConst.ACCOUNT_TYPE.FACEBOOK);
					break;
			}
		}

		Map<String, Object> response = new HashMap<>();

		response.put("subject", email);
		response.put("message", message);

		return response;
	}

	// jakduk 비밀번호 재설정.
	@RequestMapping(value = "/reset/{code}", method = RequestMethod.GET)
	public Map<String, Object> resetPassword(@PathVariable String code) {

		if (Objects.isNull(code))
			throw new IllegalArgumentException(CoreUtils.getExceptionMessage("exception.invalid.parameter"));

		Map<String, Object> response = new HashMap<>();
		Optional<Token> oToken = commonService.getTokenByCode(code);

		if (! oToken.isPresent() || ! StringUtils.equals(oToken.get().getCode(), code))
			throw new IllegalArgumentException(CoreUtils.getResourceBundleMessage("messages.user", "user.msg.reset.password.invalid"));

		Token token = oToken.get();
		response.put("subject", token.getEmail());
		response.put("code", token.getCode());

		return response;
	}

	// jakduk 비밀번호 재설정 처리.
	@RequestMapping(value = "/reset", method = RequestMethod.POST)
	public Map<String, Object> updatePassword(
			@RequestParam(value = "password") String password,
			@RequestParam(value = "code") String code) {

		Optional<Token> oToken = commonService.getTokenByCode(code);

		Map<String, Object> response = new HashMap<>();
		String message;

		if (oToken.isPresent()) {
			Token token = oToken.get();
			userService.userPasswordUpdateByEmail(token.getEmail(), passwordEncoder.encode(password.trim()));
			message = CoreUtils.getResourceBundleMessage("messages.user", "user.msg.success.change.password");
			response.put("subject", token.getEmail());

			tokenRepository.delete(token);
		} else {
			message = CoreUtils.getResourceBundleMessage("messages.user", "user.msg.reset.password.invalid");
		}

		response.put("message", message);

		return response;
	}
}
