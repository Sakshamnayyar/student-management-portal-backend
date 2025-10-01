package com.saksham.portal.common.util;

public class EmailTemplate {

    public static String newDirectMessage(String receiverName, String senderName, String messageContent) {
        return "Hello " + receiverName + ",\n\n" +
               "You have received a new message from " + senderName + ":\n\n" +
               "\"" + messageContent + "\"\n\n" +
               "Please log in to the portal to reply.";
    }

    public static String newGroupMessage(String groupName, String senderName, String messageContent) {
        return "Hello " + groupName + " Members,\n\n" +
               senderName + " has posted a new message in your group:\n\n" +
               "\"" + messageContent + "\"\n\n" +
               "Log in to the portal to view the conversation.";
    }

    public static String newSubmission(String username, String submissionType, Integer weekNumber, String fileUrl, String submittedAt) {
        String weekInfo = (weekNumber != null) ? "\n- Week: " + weekNumber : "";
        return "Hello Admin,\n\n" +
               username + " has submitted a new " + submissionType + ".\n\n" +
               "Details:\n" +
               "- Submission Type: " + submissionType + 
               weekInfo +
               "\n- File: " + fileUrl +
               "\n- Submitted At: " + submittedAt + "\n\n" +
               "Please log in to the portal to review it.";
    }

    public static String registrationSuccess(String username) {
        return "Hello " + username + ",\n\n" +
            "Welcome to the Consultancy Student Management Portal! 🎉\n\n" +
            "Your account has been created successfully, and you are now ready to begin your journey.\n\n" +
            "Next Steps:\n" +
            "- Log in to your dashboard using your registered email and password.\n" +
            "- You will be guided through the onboarding phase.\n" +
            "- Admin will be in touch with you for further instructions.\n\n" +
            "We’re excited to have you onboard and wish you success in your training.\n\n" +
            "Regards,\n" +
            "Consultancy Portal Team";
    }
}
