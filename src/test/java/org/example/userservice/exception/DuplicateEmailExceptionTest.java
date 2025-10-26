package org.example.userservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateEmailExceptionTest {

    @Test
    void duplicateEmailException_ShouldHaveCorrectMessage() {
        String message = "Email already exists";
        DuplicateEmailException exception = new DuplicateEmailException(message);

        assertEquals(message, exception.getMessage());
    }
}
