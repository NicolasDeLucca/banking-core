package com.bankingcore.shared.error;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.bankingcore.account.domain.AccountBlockedException;
import com.bankingcore.account.domain.AccountNotFoundException;
import com.bankingcore.account.domain.UnauthorizedAccountAccessException;
import com.bankingcore.auth.domain.InvalidCredentialsException;
import com.bankingcore.auth.domain.TooManyLoginAttemptsException;
import com.bankingcore.auth.domain.EmailAlreadyRegisteredException;
import com.bankingcore.shared.error.dtos.ApiErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A pure unit test - GlobalExceptionHandler is a plain @RestControllerAdvice
 * with no Spring-injected collaborators, so there's no need for a full
 * @SpringBootTest just to exercise its status-code mapping. The one handler
 * not covered here (MethodArgumentNotValidException) needs a real
 * DispatcherServlet-driven @Valid failure to construct meaningfully - see
 * GlobalExceptionHandlerIntegrationTest for that one instead.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsNotFoundExceptionsTo404() {
        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(new AccountNotFoundException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("ACCOUNT_NOT_FOUND");
    }

    @Test
    void mapsConflictExceptionsTo409() {
        ResponseEntity<ApiErrorResponse> response = handler.handleConflict(new EmailAlreadyRegisteredException("a@b.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("EMAIL_ALREADY_EXISTS");
    }

    @Test
    void mapsAuthenticationFailedExceptionsTo401() {
        ResponseEntity<ApiErrorResponse> response = handler.handleAuthenticationFailed(new InvalidCredentialsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void mapsForbiddenOperationExceptionsTo403() {
        ResponseEntity<ApiErrorResponse> response = handler.handleForbidden(new UnauthorizedAccountAccessException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().code()).isEqualTo("ACCOUNT_ACCESS_DENIED");
    }

    @Test
    void mapsBusinessRuleViolationExceptionsTo422() {
        ResponseEntity<ApiErrorResponse> response = handler.handleBusinessRuleViolation(new AccountBlockedException(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().code()).isEqualTo("ACCOUNT_BLOCKED");
    }

    @Test
    void mapsRateLimitExceededExceptionsTo429() {
        ResponseEntity<ApiErrorResponse> response = handler.handleRateLimitExceeded(new TooManyLoginAttemptsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody().code()).isEqualTo("TOO_MANY_ATTEMPTS");
    }

    @Test
    void mapsOptimisticLockingFailureTo409WithAConcurrentModificationCode() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleConcurrentModification(new OptimisticLockingFailureException("stale"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("CONCURRENT_MODIFICATION");
    }

    @Test
    void mapsNoResourceFoundTo404WithoutLeakingAsAServerError() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleNoResourceFound(new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/nope"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
    }

    @Test
    void mapsAnyOtherUnexpectedExceptionTo500WithAGenericBody() {
        // The generic body is deliberate (see GlobalExceptionHandler) - it
        // must never leak the real exception's message to the client, only
        // to the server log.
        ResponseEntity<ApiErrorResponse> response = handler.handleGeneric(new IllegalStateException("boom, some internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).doesNotContain("boom");
    }
}
