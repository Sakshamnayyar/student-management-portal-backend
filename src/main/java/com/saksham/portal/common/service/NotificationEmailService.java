package com.saksham.portal.common.service;

import com.saksham.portal.common.enums.EmailType;
import com.saksham.portal.common.util.EmailTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationEmailService {

    @Autowired
    private EmailService emailService;

    public void sendNotification(EmailType type, String toEmail, Object... params) {
        String subject = "";
        String body = "";

        switch (type) {
            case DIRECT_MESSAGE -> {
                // params: receiverName, senderName, messageContent
                subject = "📩 New Message from " + params[1];
                body = EmailTemplate.newDirectMessage(
                        (String) params[0],
                        (String) params[1],
                        (String) params[2]
                );
            }

            case GROUP_MESSAGE -> {
                // params: groupName, senderName, messageContent
                subject = "💬 New Group Message from " + params[1];
                body = EmailTemplate.newGroupMessage(
                        (String) params[0],
                        (String) params[1],
                        (String) params[2]
                );
            }

            case SUBMISSION -> {
                // params: username, submissionType, weekNumber, fileUrl, submittedAt
                subject = "📥 New " + params[1] + " Submission from " + params[0];
                body = EmailTemplate.newSubmission(
                        (String) params[0],
                        (String) params[1],
                        (Integer) params[2],
                        (String) params[3],
                        (String) params[4]
                );
            }

            case REGISTRATION -> {
                // params: username
                subject = "🎉 Welcome to the Consultancy Portal, " + params[0] + "!";
                body = EmailTemplate.registrationSuccess((String) params[0]);
            }
        }

        emailService.sendEmail(toEmail, subject, body);
    }
}


/* Usage Examples:
 * 
 * Direct Message:
 * notificationEmailService.sendNotification(
        EmailType.DIRECT_MESSAGE,
        receiver.getEmail(),
        receiver.getUsername(),
        sender.getUsername(),
        message.getContent()
    );
 * 
 * 
 * Group Message:
 * notificationEmailService.sendNotification(
        EmailType.GROUP_MESSAGE,
        user.getEmail(),
        group.getName(),
        sender.getUsername(),
        message.getContent()
    );
 * 
 * 
 * Submission notification:
 * notificationEmailService.sendNotification(
        EmailType.SUBMISSION,
        admin.getEmail(),
        user.getUsername(),
        submission.getType().name(),
        submission.getWeekNumber(),
        submission.getFileUrl(),
        submission.getSubmittedAt().toString()
    );
 * 
 * Registration:
 * notificationEmailService.sendNotification(
        EmailType.REGISTRATION,
        newUser.getEmail(),
        newUser.getUsername()
    );
 *
 */