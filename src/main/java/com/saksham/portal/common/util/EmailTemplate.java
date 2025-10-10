package com.saksham.portal.common.util;

import com.saksham.portal.common.enums.EmailType;


public class EmailTemplate {

    public static String getSubject(EmailType type, String... params) {
        return switch (type) {
            case REGISTRATION -> "Welcome to Student Portal - Registration Successful";
            case DIRECT_MESSAGE -> "New Direct Message from " + (params.length > 0 ? params[0] : "User");
            case GROUP_MESSAGE -> "New Group Message in " + (params.length > 0 ? params[0] : "Group");
            case SUBMISSION -> "Assignment Submission Received";
            case SUBMISSION_REVIEW -> "Your Submission Has Been Reviewed";
            case STATUS_CHANGE -> "Account Status Updated";
            case GROUP_ASSIGNMENT -> "Assigned to New Group: " + (params.length > 0 ? params[0] : "Group");
            case GROUP_CREATED -> "New Group Created: " + (params.length > 0 ? params[0] : "Group");
            case REMINDER -> "Reminder: " + (params.length > 0 ? params[0] : "Important Notice");
        };
    }

    public static String getMessageBody(EmailType type, String... params) {
        return switch (type) {
            case REGISTRATION -> buildRegistrationMessage(params);
            case DIRECT_MESSAGE -> buildDirectMessageBody(params);
            case GROUP_MESSAGE -> buildGroupMessageBody(params);
            case SUBMISSION -> buildSubmissionMessage(params);
            case SUBMISSION_REVIEW -> buildSubmissionReviewMessage(params);
            case STATUS_CHANGE -> buildStatusChangeMessage(params);
            case GROUP_ASSIGNMENT -> buildGroupAssignmentMessage(params);
            case GROUP_CREATED -> buildGroupCreatedMessage(params);
            case REMINDER -> buildReminderMessage(params);
        };
    }


    public static String registrationSuccess(String username) {
        return buildRegistrationMessage(username);
    }

    private static String buildRegistrationMessage(String... params) {
        String username = params.length > 0 ? params[0] : "User";
        return String.format("""
            Dear %s,
            
            Welcome to the Student Management Portal!
            
            Your account has been successfully created. You can now:
            - Access your dashboard
            - View assignments
            - Submit work
            - Communicate with your team
            
            Please log in to complete your profile setup.
            
            Best regards,
            Student Portal Team
            """, username);
    }

    private static String buildDirectMessageBody(String... params) {
        String senderName = params.length > 0 ? params[0] : "User";
        String messageContent = params.length > 1 ? params[1] : "You have a new message";
        return String.format("""
            You have received a new direct message from %s:
            
            "%s"
            
            Please log in to the portal to view the full conversation.
            
            Best regards,
            Student Portal Team
            """, senderName, messageContent);
    }

    private static String buildGroupMessageBody(String... params) {
        String groupName = params.length > 0 ? params[0] : "Group";
        String senderName = params.length > 1 ? params[1] : "User";
        String messageContent = params.length > 2 ? params[2] : "New group message";
        return String.format("""
            New message in group "%s" from %s:
            
            "%s"
            
            Please log in to the portal to participate in the group discussion.
            
            Best regards,
            Student Portal Team
            """, groupName, senderName, messageContent);
    }

    private static String buildSubmissionMessage(String... params) {
        String assignmentTitle = params.length > 0 ? params[0] : "Assignment";
        String studentName = params.length > 1 ? params[1] : "Student";
        return String.format("""
            A new submission has been received for "%s" from %s.
            
            Please log in to the portal to review the submission.
            
            Best regards,
            Student Portal Team
            """, assignmentTitle, studentName);
    }

    private static String buildSubmissionReviewMessage(String... params) {
        String assignmentTitle = params.length > 0 ? params[0] : "Assignment";
        String grade = params.length > 1 ? params[1] : "N/A";
        String feedback = params.length > 2 ? params[2] : "No feedback provided";
        return String.format("""
            Your submission for "%s" has been reviewed.
            
            Grade: %s
            Feedback: %s
            
            Please log in to the portal to view detailed feedback.
            
            Best regards,
            Student Portal Team
            """, assignmentTitle, grade, feedback);
    }

    private static String buildStatusChangeMessage(String... params) {
        String newStatus = params.length > 0 ? params[0] : "Updated";
        String reason = params.length > 1 ? params[1] : "Administrative update";
        return String.format("""
            Your account status has been updated to: %s
            
            Reason: %s
            
            If you have any questions, please contact your administrator.
            
            Best regards,
            Student Portal Team
            """, newStatus, reason);
    }

    private static String buildGroupAssignmentMessage(String... params) {
        String groupName = params.length > 0 ? params[0] : "Group";
        String assignedBy = params.length > 1 ? params[1] : "Administrator";
        return String.format("""
            You have been assigned to the group: %s
            
            Assigned by: %s
            
            Please log in to the portal to connect with your new team members.
            
            Best regards,
            Student Portal Team
            """, groupName, assignedBy);
    }

    private static String buildGroupCreatedMessage(String... params) {
        String groupName = params.length > 0 ? params[0] : "Group";
        String createdBy = params.length > 1 ? params[1] : "Administrator";
        return String.format("""
            A new group "%s" has been created by %s.
            
            You are now a member of this group.
            
            Please log in to the portal to start collaborating with your team.
            
            Best regards,
            Student Portal Team
            """, groupName, createdBy);
    }

    private static String buildReminderMessage(String... params) {
        String reminderText = params.length > 0 ? params[0] : "You have pending items in the portal";
        return String.format("""
            Reminder: %s
            
            Please log in to the portal to take necessary action.
            
            Best regards,
            Student Portal Team
            """, reminderText);
    }
}