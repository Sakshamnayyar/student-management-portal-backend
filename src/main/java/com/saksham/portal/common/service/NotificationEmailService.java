package com.saksham.portal.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.saksham.portal.common.enums.EmailType;
import com.saksham.portal.common.util.EmailTemplate;

@Service
public class NotificationEmailService {

    @Autowired
    private EmailService emailService;

    public void sendNotification(EmailType type, String toEmail, String... params) {
        String subject = EmailTemplate.getSubject(type, params);
        String body = EmailTemplate.getMessageBody(type, params);
        emailService.sendEmail(toEmail, subject, body);
    }

    // Specific notification methods for better type safety and clarity
    public void sendRegistrationNotification(String toEmail, String username) {
        sendNotification(EmailType.REGISTRATION, toEmail, username);
    }

    public void sendDirectMessageNotification(String toEmail, String senderName, String messageContent) {
        sendNotification(EmailType.DIRECT_MESSAGE, toEmail, senderName, messageContent);
    }

    public void sendGroupMessageNotification(String toEmail, String groupName, String senderName, String messageContent) {
        sendNotification(EmailType.GROUP_MESSAGE, toEmail, groupName, senderName, messageContent);
    }

    public void sendSubmissionNotification(String toEmail, String assignmentTitle, String studentName) {
        sendNotification(EmailType.SUBMISSION, toEmail, assignmentTitle, studentName);
    }

    public void sendSubmissionReviewNotification(String toEmail, String assignmentTitle, String grade, String feedback) {
        sendNotification(EmailType.SUBMISSION_REVIEW, toEmail, assignmentTitle, grade, feedback);
    }

    public void sendStatusChangeNotification(String toEmail, String newStatus, String reason) {
        sendNotification(EmailType.STATUS_CHANGE, toEmail, newStatus, reason);
    }

    public void sendGroupAssignmentNotification(String toEmail, String groupName, String assignedBy) {
        sendNotification(EmailType.GROUP_ASSIGNMENT, toEmail, groupName, assignedBy);
    }

    public void sendGroupCreatedNotification(String toEmail, String groupName, String createdBy) {
        sendNotification(EmailType.GROUP_CREATED, toEmail, groupName, createdBy);
    }

    public void sendReminderNotification(String toEmail, String reminderText) {
        sendNotification(EmailType.REMINDER, toEmail, reminderText);
    }
    // Legacy method for backward compatibility
    public void sendNotification(EmailType type, String toEmail, Object... params) {
        String[] stringParams = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            stringParams[i] = params[i] != null ? params[i].toString() : "";
        }
        sendNotification(type, toEmail, stringParams);
    }
}
