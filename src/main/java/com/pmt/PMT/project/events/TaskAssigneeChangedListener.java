package com.pmt.PMT.project.events;
import lombok.extern.slf4j.Slf4j;

import com.pmt.PMT.project.services.MailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
public class TaskAssigneeChangedListener {

    private final MailService mailService;

    public TaskAssigneeChangedListener(MailService mailService) {
        this.mailService = mailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TaskAssigneeChangedEvent event) {
        try {
            mailService.sendAssigneeChangedEmail(event);
        } catch (org.springframework.mail.MailException ex) {
            log.error("Failed to send assignee email for task {}", event.taskId(), ex);
        }
    }
}
