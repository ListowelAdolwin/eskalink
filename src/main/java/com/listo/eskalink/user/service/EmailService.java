package com.listo.eskalink.user.service;

import com.listo.eskalink.application.enums.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String userName, String verificationLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify Your Email - A2SV Eskalate");

            String emailBody = buildVerificationEmailBody(userName, verificationLink);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {} - Error: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendJobApplicationNotification(String companyEmail, String companyName,
                                               String jobTitle, String applicantName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(companyEmail);
            message.setSubject("New Job Application Received - " + jobTitle);

            String emailBody = buildJobApplicationNotificationBody(companyName, jobTitle, applicantName);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Job application notification sent to company: {}", companyEmail);
        } catch (Exception e) {
            log.error("Failed to send job application notification to: {} - Error: {}", companyEmail, e.getMessage());
        }
    }

    @Async
    public void sendApplicationStatusUpdate(String applicantEmail, String applicantName,
                                            String jobTitle, String companyName,
                                            ApplicationStatus newStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(applicantEmail);
            message.setSubject("Application Status Update - " + jobTitle);

            String emailBody = buildStatusUpdateEmailBody(applicantName, jobTitle, companyName, newStatus);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Application status update email sent to: {} with status: {}", applicantEmail, newStatus);
        } catch (Exception e) {
            log.error("Failed to send status update email to: {} - Error: {}", applicantEmail, e.getMessage());
        }
    }

    private String buildVerificationEmailBody(String userName, String verificationLink) {
        return String.format("""
            Dear %s,
            
            Welcome to A2SV Eskalate!
            
            Thank you for signing up. To complete your registration and activate your account, 
            please click on the verification link below:
            
            %s
            
            This link will expire in 1 hour for security purposes.
            
            If you didn't create this account, please ignore this email.
            
            Best regards,
            The A2SV Eskalate Team
            """, userName, verificationLink);
    }

    private String buildJobApplicationNotificationBody(String companyName, String jobTitle, String applicantName) {
        return String.format("""
            Dear %s,
            
            You have received a new job application for the position: %s
            
            Applicant: %s
            
            Please log in to your dashboard to review the application and manage your candidates.
            
            Dashboard: %s/dashboard
            
            Best regards,
            The A2SV Eskalate Team
            """, companyName, jobTitle, applicantName, baseUrl);
    }

    private String buildStatusUpdateEmailBody(String applicantName, String jobTitle,
                                              String companyName, ApplicationStatus status) {
        String statusMessage = switch (status) {
            case INTERVIEW -> String.format("""
                Congratulations! You've been selected for an interview for the position of %s at %s.
                
                The hiring team will contact you soon with interview details. Please keep an eye on your email 
                and phone for further communication.
                
                Good luck with your interview!
                """, jobTitle, companyName);

            case REJECTED -> String.format("""
                We regret to inform you that your application for the position of %s at %s was not selected 
                for the next stage of our hiring process.
                
                We appreciate the time you took to apply and encourage you to apply for other opportunities 
                that match your skills and experience.
                
                Thank you for your interest in working with us.
                """, jobTitle, companyName);

            case HIRED -> String.format("""
                Congratulations! We are pleased to inform you that you have been selected for the position 
                of %s at %s.
                
                Welcome to our team! HR will contact you soon with onboarding details and next steps.
                
                We look forward to working with you!
                """, jobTitle, companyName);

            default -> String.format("""
                Your application status for the position of %s at %s has been updated to: %s
                
                Please log in to your dashboard for more details.
                """, jobTitle, companyName, status.name());
        };

        return String.format("""
            Dear %s,
            
            %s
            
            Dashboard: %s/dashboard
            
            Best regards,
            The A2SV Eskalate Team
            """, applicantName, statusMessage, baseUrl);
    }
}
