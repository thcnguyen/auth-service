package exchange.velox.authservice.service;

import exchange.velox.authservice.dto.*;
import exchange.velox.authservice.gateway.DocgenServiceGateway;
import exchange.velox.authservice.gateway.NotificationServiceGateway;
import exchange.velox.authservice.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.annotation.Async;

import java.util.*;

public class NotificationService {

    private Logger log = LogManager.getLogger(NotificationService.class);

    private String from;

    private String serverWeb;

    @Autowired
    private LogService logService;

    @Autowired
    private ResourceBundleMessageSource resource;

    @Autowired
    private NotificationServiceGateway notificationServiceGateway;

    @Autowired
    private DocgenServiceGateway docgenServiceGateway;

    @Autowired
    private ModelMapper modelMapper;

    private Map<String, Object> initDefaultModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("i18n", resource);
        model.put("serverWeb", serverWeb);
        model.put("emailDate", new Date());
        return model;
    }

    @Async
    public void sendMaxLoginAttemptReached(final UserDTO user) {
        String subject = resource.getMessage("mail.userLocked.subject", null, new Locale(user.getLang()));
        // prepare email model
        EmailRequestDTO emailRequest = new EmailRequestDTO();
        EmailOptionDTO emailOption = new EmailOptionDTO();
        Set<String> toEmails = new HashSet<>();
        toEmails.add(user.getEmail());
        emailOption.setTo(toEmails);
        emailOption.setFrom(from);
        emailOption.setTemplate(EmailRequestDTO.TemplateType.userLocked.name());
        emailOption.setSubject(subject);
        Map<String, Object> model = initDefaultModel();
        model.put("user", user);
        model.put("name", user.getFullName());
        model.put("email", user.getEmail());
        log.info("Sending user locked " + user.getEmail());
        String body = docgenServiceGateway.generateEmailContent(emailOption.getTemplate(), user.getLang(), model);
        emailOption.setBody(body);
        UserNotificationDTO userNotificationDTO = new UserNotificationDTO();
        modelMapper.map(user,userNotificationDTO);
        emailRequest.setAuthor(userNotificationDTO);
        emailRequest.setEmailContent(Arrays.asList(emailOption));
        logService.addLog(null, JsonUtils.getLogView(emailOption));
        notificationServiceGateway.sendMail(emailRequest);
    }

    @Async
    public void sendUserLockedMessage(final UserDTO user) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", user.getEmail());
        metadata.put("id", user.getHumanId());
        metadata.put("role", UserRole.valueOf(user.getRole()).getDescription());
        metadata.put("name", user.getFullName());
        metadata.put("uri", buildEditUserUri(serverWeb, user));
        notificationServiceGateway.postToTeams("user_locked", metadata);
    }

    private static String buildEditUserUri(String serverWeb, UserDTO user) {
        String uri = serverWeb + "#/";
        if (UserRole.SELLER.name().equals(user.getRole())) {
            uri += "sellers/edit/" + user.getId();
        }
        if (UserRole.BIDDER.name().equals(user.getRole())) {
            uri += "investors/edit/" + user.getId();
        }
        if (UserRole.INTRODUCER.name().equals(user.getRole())) {
            uri += "introducers/edit/" + user.getId();
        }
        return uri;

    }

    @Async
    public void sendForgotPasswordMail(final UserDTO user, final String token) {
        String subject = resource.getMessage("mail.forgotPassword.subject", null, new Locale(user.getLang()));
        // prepare email model
        EmailRequestDTO emailRequest = new EmailRequestDTO();
        EmailOptionDTO emailOption = new EmailOptionDTO();
        Set<String> toEmails = new HashSet<>();
        toEmails.add(user.getEmail());
        emailOption.setTo(toEmails);
        emailOption.setFrom(from);
        emailOption.setTemplate(EmailRequestDTO.TemplateType.forgotPassword.name());
        emailOption.setSubject(subject);
        Map<String, Object> model = initDefaultModel();
        model.put("subject", subject);
        model.put("name", user.getFullName());
        model.put("email", user.getEmail());
        model.put("token", token);
        log.info("Sending forgot password mail to " + user.getEmail() + " with token " + token);
        String body = docgenServiceGateway.generateEmailContent(emailOption.getTemplate(), user.getLang(), model);
        emailOption.setBody(body);
        UserNotificationDTO userNotificationDTO = new UserNotificationDTO();
        modelMapper.map(user,userNotificationDTO);
        emailRequest.setAuthor(userNotificationDTO);
        emailRequest.setEmailContent(Arrays.asList(emailOption));
        logService.addLog(null, JsonUtils.getLogView(emailOption));
        notificationServiceGateway.sendMail(emailRequest);
    }

    @Async
    public void sendInitiatedPasswordMail(final UserDTO user, String companyName) {
        String subject = resource.getMessage("mail.initiatedPassword.subject", null, new Locale(user.getLang()));
        // prepare email model
        EmailRequestDTO emailRequest = new EmailRequestDTO();
        EmailOptionDTO emailOption = new EmailOptionDTO();
        Set<String> toEmails = new HashSet<>();
        toEmails.add(user.getEmail());
        emailOption.setTo(toEmails);
        emailOption.setFrom(from);
        emailOption.setTemplate(EmailRequestDTO.TemplateType.initiatedPassword.name());
        emailOption.setSubject(subject);
        Map<String, Object> model = initDefaultModel();
        model.put("subject", subject);
        model.put("name", user.getFullName());
        model.put("email", user.getEmail());
        model.put("companyName", companyName);
        log.info("Sending initiation mail to " + user.getEmail());
        String body = docgenServiceGateway.generateEmailContent(emailOption.getTemplate(), user.getLang(), model);
        emailOption.setBody(body);
        UserNotificationDTO userNotificationDTO = new UserNotificationDTO();
        modelMapper.map(user,userNotificationDTO);
        emailRequest.setAuthor(userNotificationDTO);
        emailRequest.setEmailContent(Arrays.asList(emailOption));
        logService.addLog(null, JsonUtils.getLogView(emailOption));
        notificationServiceGateway.sendMail(emailRequest);
    }

    @Async
    public void sendChangedPasswordMail(final UserDTO user) {
        String subject = resource.getMessage("mail.changedPassword.subject", null, new Locale(user.getLang()));
        // prepare email model
        EmailRequestDTO emailRequest = new EmailRequestDTO();
        EmailOptionDTO emailOption = new EmailOptionDTO();
        Set<String> toEmails = new HashSet<>();
        toEmails.add(user.getEmail());
        emailOption.setTo(toEmails);
        emailOption.setFrom(from);
        emailOption.setTemplate(EmailRequestDTO.TemplateType.changedPassword.name());
        emailOption.setSubject(subject);
        Map<String, Object> model = initDefaultModel();
        model.put("subject", subject);
        model.put("email", user.getEmail());
        model.put("name", user.getFullName());
        log.info("Sending password changed mail to " + user.getEmail());
        String body = docgenServiceGateway.generateEmailContent(emailOption.getTemplate(), user.getLang(), model);
        emailOption.setBody(body);
        UserNotificationDTO userNotificationDTO = new UserNotificationDTO();
        modelMapper.map(user,userNotificationDTO);
        emailRequest.setAuthor(userNotificationDTO);
        emailRequest.setEmailContent(Arrays.asList(emailOption));
        logService.addLog(null, JsonUtils.getLogView(emailOption));
        notificationServiceGateway.sendMail(emailRequest);
    }

    @Async
    public void sendMemberRegistrationMail(final UserDTO user, final String token, final String companyName,
                                           final UserDTO inviter) {
        String subject = resource.getMessage("mail.memberRegistration.subject", null, new Locale(user.getLang()));
        // prepare email model
        EmailRequestDTO emailRequest = new EmailRequestDTO();
        EmailOptionDTO emailOption = new EmailOptionDTO();
        Set<String> toEmails = new HashSet<>();
        toEmails.add(user.getEmail());
        emailOption.setTo(toEmails);
        emailOption.setFrom(from);
        emailOption.setTemplate(EmailRequestDTO.TemplateType.memberRegistration.name());
        emailOption.setSubject(subject);
        Map<String, Object> model = initDefaultModel();
        model.put("subject", subject);
        model.put("name", user.getFullName());
        model.put("email", user.getEmail());
        model.put("token", token);
        model.put("inviter", inviter.getFullName());
        model.put("companyName", companyName);
        model.put("role", user.getRole());
        log.info("Sending invitation mail to " + user.getEmail());
        String body = docgenServiceGateway.generateEmailContent(emailOption.getTemplate(), user.getLang(), model);
        emailOption.setBody(body);
        UserNotificationDTO userNotificationDTO = new UserNotificationDTO();
        modelMapper.map(user,userNotificationDTO);
        emailRequest.setAuthor(userNotificationDTO);
        emailRequest.setEmailContent(Arrays.asList(emailOption));
        logService.addLog(inviter, JsonUtils.getLogView(emailOption));
        notificationServiceGateway.sendMail(emailRequest);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getServerWeb() {
        return serverWeb;
    }

    public void setServerWeb(String serverWeb) {
        this.serverWeb = serverWeb;
    }
}
