package com.sphereon.uniresolver.driver.did.factom;

import com.sphereon.factom.identity.did.DIDRuntimeException;
import com.sphereon.uniresolver.driver.did.factom.dto.error.Error;
import com.sphereon.uniresolver.driver.did.factom.dto.error.ErrorResponse;
import com.sphereon.uniresolver.driver.did.factom.dto.error.Level;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uniresolver.ResolutionException;

@ControllerAdvice
@Slf4j
public class FactomDriverControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ResolutionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse resolutionException(ResolutionException registrationException) {
        log.warn(registrationException.getMessage());
        return new ErrorResponse(registrationException);
    }

    @ResponseBody
    @ExceptionHandler(DIDRuntimeException.NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFoundException(DIDRuntimeException.NotFoundException notFoundException) {
        log.warn(notFoundException.getMessage());
        final Error error = new Error(Level.FATAL, "404", notFoundException.getMessage());
        return new ErrorResponse(error);
    }


    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse fallbackException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return new ErrorResponse(exception);
    }
}
