package com.pmt.PMT.project.services;

import com.pmt.PMT.project.events.TaskAssigneeChangedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@pmt.com}")
    private String from;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAssigneeChangedEmail(TaskAssigneeChangedEvent e) {
        if (e.newAssigneeEmail() == null || e.newAssigneeEmail().isBlank()) return;

        String subject = "[PMT] Vous avez été assigné à une tâche";
        String body = """
                Bonjour %s,

                %s vous a assigné à la tâche : "%s".

                — Message automatique
                """.formatted(
                e.newAssigneeName() != null ? e.newAssigneeName() : "",
                e.actorDisplayName(),
                e.taskTitle()
        );

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(e.newAssigneeEmail());
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
    }
}
