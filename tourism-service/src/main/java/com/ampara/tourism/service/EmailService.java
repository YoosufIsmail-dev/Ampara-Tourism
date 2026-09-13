package com.ampara.tourism.service;

import com.ampara.tourism.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@ampara-tourism.example}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBookingConfirmation(Booking booking) {
        String to = booking.getUser().getEmail();
        String subject = "Booking confirmed: " + booking.getHotel().getName();
        String body = """
                Hi %s,

                Your booking is confirmed!

                Hotel: %s
                Check-in: %s
                Check-out: %s
                Guests: %d
                Total: %.2f

                Thanks for booking with Ampara Tourism.
                """.formatted(
                booking.getUser().getName(),
                booking.getHotel().getName(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getGuests(),
                booking.getTotalPrice()
        );

        if (!mailEnabled) {
            // No SMTP credentials configured yet - log instead of failing the booking.
            log.info("[email disabled] Would send to {}: {}\n{}", to, subject, body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Don't let a mail failure fail the booking itself.
            log.warn("Failed to send booking confirmation email to {}: {}", to, e.getMessage());
        }
    }
}
