package com.sphereon.uniresolver.driver.did.factom.dto.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@ApiModel(
        description = "The error response"
)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorResponse {
    @XmlElement(
            name = "errors"
    )
    @JsonProperty("errors")
    @ApiModelProperty
    private List<Error> errors;

    public ErrorResponse(String code, Throwable t) {
        this.add(code, t);
    }

    public ErrorResponse(Throwable t) {
        this.add(t);
    }

    public ErrorResponse(String code, String message) {
        this.add(code, message);
    }

    public ErrorResponse(Error error, Error... additionalErrors) {
        this.add(error, additionalErrors);
    }

    @JsonCreator
    public ErrorResponse(Collection<Error> errors) {
        this.assertCondition(errors != null, "Errors must not be null!");
        this.assertCondition(!errors.isEmpty(), "Errors must not be empty!");
        this.errors = new ArrayList(errors);
    }

    protected ErrorResponse() {
        this.initErrorsWhenNeeded();
    }

    public ErrorResponse add(Throwable t) {
        return this.add("exception", t);
    }

    public ErrorResponse add(String code, Throwable t) {
        return this.add(Error.from(t).setCode(code));
    }

    public ErrorResponse add(String code, String message) {
        this.assertCondition(code != null, "Code must not be null");
        this.assertCondition(message != null, "Message must not be null");
        return this.add(new Error(code, message));
    }

    public ErrorResponse add(Error error) {
        this.assertCondition(error != null, "Error must not be null");
        this.initErrorsWhenNeeded();
        this.errors.add(error);
        return this;
    }

    public ErrorResponse add(Error error, Error... additionalErrors) {
        this.add(error);
        if (additionalErrors != null) {
            Error[] var3 = additionalErrors;
            int var4 = additionalErrors.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Error additionalError = var3[var5];
                this.add(additionalError);
            }
        }

        return this;
    }

    public List<Error> getErrors() {
        return this.errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
        this.initErrorsWhenNeeded();
    }

    public List<Error> getErrors(Level level, boolean checkCauses) {
        List<Error> filtered = new ArrayList();
        Iterator var4 = this.getErrors().iterator();

        while (var4.hasNext()) {
            Error error = (Error) var4.next();
            if (error.is(level, checkCauses)) {
                filtered.add(error);
            }
        }

        return filtered;
    }

    public boolean hasErrors(Level level, boolean checkCauses) {
        return this.getErrors(level, checkCauses).size() > 0;
    }

    private void assertCondition(boolean condition, String error) {
        if (!condition) {
            throw new RuntimeException(error);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ErrorResponse)) {
            return false;
        } else {
            ErrorResponse that = (ErrorResponse) o;
            return this.errors != null ? this.errors.equals(that.errors) : that.errors == null;
        }
    }

    public int hashCode() {
        return this.errors != null ? this.errors.hashCode() : 0;
    }

    public String toString() {
        return "ErrorResponse{errors=" + this.errors + '}';
    }

    private void initErrorsWhenNeeded() {
        if (this.errors == null) {
            this.errors = new ArrayList();
        }

    }
}
