package com.sunlight.invest.notification.service;

import com.sunlight.invest.notification.entity.EmailRecipient;
import com.sunlight.invest.notification.mapper.EmailRecipientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailRecipientServiceTest {

    @Mock
    private EmailRecipientMapper emailRecipientMapper;

    @InjectMocks
    private EmailRecipientService emailRecipientService;

    private EmailRecipient testRecipient;

    @BeforeEach
    void setUp() {
        testRecipient = new EmailRecipient("张三", "zhangsan@example.com");
        testRecipient.setId(1L);
    }

    @Test
    void testAddEmailRecipient_Success() {
        // Given
        when(emailRecipientMapper.selectByEmail(testRecipient.getEmail())).thenReturn(null);
        when(emailRecipientMapper.insert(any(EmailRecipient.class))).thenReturn(1);

        // When
        int result = emailRecipientService.addEmailRecipient(testRecipient);

        // Then
        assertEquals(1, result);
        verify(emailRecipientMapper).selectByEmail(testRecipient.getEmail());
        verify(emailRecipientMapper).insert(testRecipient);
    }

    @Test
    void testAddEmailRecipient_EmailExists() {
        // Given
        when(emailRecipientMapper.selectByEmail(testRecipient.getEmail())).thenReturn(testRecipient);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailRecipientService.addEmailRecipient(testRecipient);
        });

        assertTrue(exception.getMessage().contains("邮箱地址已存在"));
        verify(emailRecipientMapper).selectByEmail(testRecipient.getEmail());
        verify(emailRecipientMapper, never()).insert(any());
    }

    @Test
    void testGetEmailRecipientById() {
        // Given
        when(emailRecipientMapper.selectById(1L)).thenReturn(testRecipient);

        // When
        EmailRecipient result = emailRecipientService.getEmailRecipientById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testRecipient.getId(), result.getId());
        assertEquals(testRecipient.getName(), result.getName());
        assertEquals(testRecipient.getEmail(), result.getEmail());
        verify(emailRecipientMapper).selectById(1L);
    }

    @Test
    void testGetEmailRecipientByEmail() {
        // Given
        when(emailRecipientMapper.selectByEmail(testRecipient.getEmail())).thenReturn(testRecipient);

        // When
        EmailRecipient result = emailRecipientService.getEmailRecipientByEmail(testRecipient.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(testRecipient.getEmail(), result.getEmail());
        verify(emailRecipientMapper).selectByEmail(testRecipient.getEmail());
    }

    @Test
    void testGetAllEnabledEmailRecipients() {
        // Given
        List<EmailRecipient> recipients = Arrays.asList(testRecipient);
        when(emailRecipientMapper.selectAllEnabled()).thenReturn(recipients);

        // When
        List<EmailRecipient> result = emailRecipientService.getAllEnabledEmailRecipients();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRecipient.getEmail(), result.get(0).getEmail());
        verify(emailRecipientMapper).selectAllEnabled();
    }

    @Test
    void testGetAllEmailRecipients() {
        // Given
        List<EmailRecipient> recipients = Arrays.asList(testRecipient);
        when(emailRecipientMapper.selectAll()).thenReturn(recipients);

        // When
        List<EmailRecipient> result = emailRecipientService.getAllEmailRecipients();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRecipient.getEmail(), result.get(0).getEmail());
        verify(emailRecipientMapper).selectAll();
    }

    @Test
    void testUpdateEmailRecipient_Success() {
        // Given
        EmailRecipient updatedRecipient = new EmailRecipient("李四", "lisi@example.com");
        updatedRecipient.setId(1L);
        
        when(emailRecipientMapper.selectByEmail(updatedRecipient.getEmail())).thenReturn(null);
        when(emailRecipientMapper.update(any(EmailRecipient.class))).thenReturn(1);

        // When
        int result = emailRecipientService.updateEmailRecipient(updatedRecipient);

        // Then
        assertEquals(1, result);
        verify(emailRecipientMapper).selectByEmail(updatedRecipient.getEmail());
        verify(emailRecipientMapper).update(updatedRecipient);
    }

    @Test
    void testUpdateEmailRecipient_EmailExists() {
        // Given
        EmailRecipient updatedRecipient = new EmailRecipient("李四", "lisi@example.com");
        updatedRecipient.setId(1L);
        
        EmailRecipient existingRecipient = new EmailRecipient("王五", "lisi@example.com");
        existingRecipient.setId(2L);
        
        when(emailRecipientMapper.selectByEmail(updatedRecipient.getEmail())).thenReturn(existingRecipient);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailRecipientService.updateEmailRecipient(updatedRecipient);
        });

        assertTrue(exception.getMessage().contains("邮箱地址已存在"));
        verify(emailRecipientMapper).selectByEmail(updatedRecipient.getEmail());
        verify(emailRecipientMapper, never()).update(any());
    }

    @Test
    void testDeleteEmailRecipientById() {
        // Given
        when(emailRecipientMapper.deleteById(1L)).thenReturn(1);

        // When
        int result = emailRecipientService.deleteEmailRecipientById(1L);

        // Then
        assertEquals(1, result);
        verify(emailRecipientMapper).deleteById(1L);
    }

    @Test
    void testDeleteEmailRecipientByEmail() {
        // Given
        String email = "zhangsan@example.com";
        when(emailRecipientMapper.deleteByEmail(email)).thenReturn(1);

        // When
        int result = emailRecipientService.deleteEmailRecipientByEmail(email);

        // Then
        assertEquals(1, result);
        verify(emailRecipientMapper).deleteByEmail(email);
    }
}