package com.pmt.PMT.project.services;

import com.pmt.PMT.project.events.TaskAssigneeChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @BeforeEach
    void setFromField() throws Exception {
        var field = MailService.class.getDeclaredField("from");
        field.setAccessible(true);
        field.set(mailService, "test@pmt.com");
    }

    @Test
    void sendAssigneeChangedEmail_shouldSend_whenEmailIsValid() {
        TaskAssigneeChangedEvent event = new TaskAssigneeChangedEvent(
                UUID.randomUUID(),
                "Tâche importante",
                "Alice",
                "bob@example.com",
                "Bob"
        );

        mailService.sendAssigneeChangedEmail(event);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();

        assertNotNull(message);
        assertEquals("test@pmt.com", message.getFrom());
        assertNotNull(message.getTo());
        assertEquals("bob@example.com", message.getTo()[0]);
        assertEquals("[PMT] Vous avez été assigné à une tâche", message.getSubject());
        assertNotNull(message.getText());
        assertTrue(message.getText().contains("Alice"));
        assertTrue(message.getText().contains("Tâche importante"));
    }

    @Test
    void sendAssigneeChangedEmail_shouldNotSend_whenEmailIsNull() {
        TaskAssigneeChangedEvent event = new TaskAssigneeChangedEvent(
                UUID.randomUUID(),
                "Tâche X",
                "Alice",
                null,
                "Bob"
        );

        mailService.sendAssigneeChangedEmail(event);

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendAssigneeChangedEmail_shouldNotSend_whenEmailIsBlank() {
        TaskAssigneeChangedEvent event = new TaskAssigneeChangedEvent(
                UUID.randomUUID(),
                "Tâche Y",
                "Alice",
                "   ",
                "Bob"
        );

        mailService.sendAssigneeChangedEmail(event);

        verifyNoInteractions(mailSender);
    }
}
