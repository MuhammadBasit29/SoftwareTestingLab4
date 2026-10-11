package Library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private EmailProvider emailProvider;

    @Mock
    private ResourceRepository resourceRepository;

    private LibraryService libraryService;
    private UUID resourceId;
    private String memberEmail;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService(
                emailProvider,
                resourceRepository
        );

        resourceId = UUID.randomUUID();
        memberEmail = "mbasit@uca.com";
    }

    // Test 1: Null resource ID returns false.
    @Test
    void checkoutResource_nullId_returnsFalse() throws Exception {
        boolean result = libraryService.checkoutResource(
                null, memberEmail
        );

        assertFalse(result);
        verifyNoInteractions(resourceRepository, emailProvider);
    }

    // Test 2: Unavailable resource returns false.
    @Test
    void checkoutResource_resourceUnavailable_returnsFalse()
            throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(false);

        boolean result = libraryService.checkoutResource(
                resourceId, memberEmail
        );

        assertFalse(result);
        verify(resourceRepository).isResourceAvailable(resourceId);
        verify(resourceRepository, never())
                .updateStatus(any(UUID.class), anyBoolean());
        verifyNoInteractions(emailProvider);
    }

    // Test 3: Successful checkout updates status and sends an email.
    @Test
    void checkoutResource_success_returnsTrue() throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(true);

        when(resourceRepository.updateStatus(resourceId, false))
                .thenReturn(true);

        String expectedMessage =
                "Resource ID: " + resourceId + " checked out.";

        when(emailProvider.sendEmail(memberEmail, expectedMessage))
                .thenReturn(true);

        boolean result = libraryService.checkoutResource(
                resourceId, memberEmail
        );

        assertTrue(result);
        verify(resourceRepository).updateStatus(resourceId, false);
        verify(emailProvider).sendEmail(memberEmail, expectedMessage);
    }

    // Test 4: Failed status update throws DatabaseFailureException.
    @Test
    void checkoutResource_statusUpdateFails_throwsDatabaseFailureException()
            throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(true);

        when(resourceRepository.updateStatus(resourceId, false))
                .thenReturn(false);

        assertThrows(
                DatabaseFailureException.class,
                () -> libraryService.checkoutResource(
                        resourceId, memberEmail
                )
        );

        verify(emailProvider, never())
                .sendEmail(anyString(), anyString());
    }

    // Test 5: Failed email delivery throws EmailFailureException.
    @Test
    void checkoutResource_emailFails_throwsEmailFailureException()
            throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(true);

        when(resourceRepository.updateStatus(resourceId, false))
                .thenReturn(true);

        String expectedMessage =
                "Resource ID: " + resourceId + " checked out.";

        when(emailProvider.sendEmail(memberEmail, expectedMessage))
                .thenReturn(false);

        assertThrows(
                EmailFailureException.class,
                () -> libraryService.checkoutResource(
                        resourceId, memberEmail
                )
        );

        verify(resourceRepository).updateStatus(resourceId, false);
        verify(emailProvider).sendEmail(memberEmail, expectedMessage);
    }

    // Test 6: Unexpected repository exception propagates to the caller.
    @Test
    void checkoutResource_repositoryThrowsException_propagatesException()
            throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenThrow(new RuntimeException("Repository failure"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> libraryService.checkoutResource(
                        resourceId, memberEmail
                )
        );

        assertEquals("Repository failure", exception.getMessage());
        verifyNoInteractions(emailProvider);
    }

    // Test 7: Email provider exception propagates to the caller.
    @Test
    void checkoutResource_emailThrowsException_propagatesException()
            throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(true);

        when(resourceRepository.updateStatus(resourceId, false))
                .thenReturn(true);

        String expectedMessage =
                "Resource ID: " + resourceId + " checked out.";

        when(emailProvider.sendEmail(memberEmail, expectedMessage))
                .thenThrow(
                        new EmailFailureException("Email service error")
                );

        assertThrows(
                EmailFailureException.class,
                () -> libraryService.checkoutResource(
                        resourceId, memberEmail
                )
        );

        verify(resourceRepository).updateStatus(resourceId, false);
        verify(emailProvider).sendEmail(memberEmail, expectedMessage);
    }
// Test 9: Availability check returns false; status is never updated.
    @Test
    void checkoutResource_unavailable_doesNotUpdateStatus() throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(false);

        libraryService.checkoutResource(resourceId, memberEmail);

        verify(resourceRepository, never())
                .updateStatus(any(UUID.class), anyBoolean());
    }

    // Test 10: Email is sent to the correct recipient with the exact message.
    @Test
    void checkoutResource_success_sendsCorrectEmail() throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(true);
        when(resourceRepository.updateStatus(resourceId, false))
                .thenReturn(true);

        String expectedMessage =
                "Resource ID: " + resourceId + " checked out.";

        when(emailProvider.sendEmail(memberEmail, expectedMessage))
                .thenReturn(true);

        libraryService.checkoutResource(resourceId, memberEmail);

        verify(emailProvider, times(1))
                .sendEmail(memberEmail, expectedMessage);
    }

    // Test 11: Email is not sent when the status update fails.
    @Test
    void checkoutResource_statusUpdateFails_doesNotSendEmail()
            throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(true);
        when(resourceRepository.updateStatus(resourceId, false))
                .thenReturn(false);

        assertThrows(
                DatabaseFailureException.class,
                () -> libraryService.checkoutResource(resourceId, memberEmail)
        );

        verifyNoInteractions(emailProvider);
    }

    // Test 12: Availability check throws a database-related runtime exception.
    @Test
    void checkoutResource_availabilityCheckThrowsException()
            throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenThrow(new RuntimeException("Availability check failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> libraryService.checkoutResource(resourceId, memberEmail)
        );

        assertEquals("Availability check failed", exception.getMessage());
        verifyNoInteractions(emailProvider);
    }

    // Test 13: Email failure occurs after the resource status is updated.
    @Test
    void checkoutResource_emailFails_statusWasUpdated() throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(true);
        when(resourceRepository.updateStatus(resourceId, false))
                .thenReturn(true);
        when(emailProvider.sendEmail(
                eq(memberEmail), contains(resourceId.toString())
        )).thenReturn(false);

        assertThrows(
                EmailFailureException.class,
                () -> libraryService.checkoutResource(resourceId, memberEmail)
        );

        verify(resourceRepository).updateStatus(resourceId, false);
    }

    // Test 14: Status update is attempted exactly once on successful checkout.
    @Test
    void checkoutResource_success_updatesStatusExactlyOnce()
            throws Exception {
        when(resourceRepository.isResourceAvailable(resourceId))
                .thenReturn(true);
        when(resourceRepository.updateStatus(resourceId, false))
                .thenReturn(true);
        when(emailProvider.sendEmail(
                eq(memberEmail), anyString()
        )).thenReturn(true);

        libraryService.checkoutResource(resourceId, memberEmail);

        verify(resourceRepository, times(1))
                .updateStatus(resourceId, false);
    }

}
