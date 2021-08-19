package com.sphereon.uniresolver.driver.did.factom.dto.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@ApiModel(
        description = "An error"
)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Error {
    @ApiModelProperty(
            required = true,
            allowableValues = "INFO,WARNING,FATAL"
    )
    @XmlElement(
            name = "level",
            required = true
    )
    @JsonProperty(
            value = "level",
            required = true
    )
    private Level level;
    @XmlElement(
            name = "code",
            required = true
    )
    @JsonProperty(
            value = "code",
            required = true
    )
    private String code;
    @XmlElement(
            name = "message",
            required = true
    )
    @JsonProperty(
            value = "message",
            required = true
    )
    private String message;
    @ApiModelProperty(
            reference = "Error"
    )
    @XmlElement(
            name = "cause"
    )
    @JsonProperty("cause")
    private Error cause;

    @JsonCreator(
            mode = Mode.PROPERTIES
    )
    public Error(@JsonProperty("level") Level level, @JsonProperty("code") String code, @JsonProperty("message") String message, @JsonProperty("cause") Error cause) {
        this.level = Level.FATAL;
        this.setLevel(level);
        this.setCode(code);
        this.setMessage(message);
        this.setCause(cause);
    }

    public Error(Level level, String code, String message) {
        this.level = Level.FATAL;
        this.setLevel(level);
        this.setCode(code);
        this.setMessage(message);
    }

    public Error(String code, String message) {
        this.level = Level.FATAL;
        this.setCode(code);
        this.setMessage(message);
    }

    public static Error from(Throwable t) {
        Throwable current = t;
        Error error = null;
        if (t == null) {
            error = new Error("exception", "Nullpointer during error creation from throwable!");
        }

        for (; current != null; current = current.getCause()) {
            if (error == null) {
                error = new Error(Level.FATAL, "exception", current.getMessage());
            } else {
                error.setCause(new Error(Level.FATAL, error.getCode(), current.getMessage()));
            }
        }

        return error;
    }

    public boolean is(Level level, boolean checkCauses) {
        return Level.is(this, level, checkCauses);
    }

    public Level getLevel() {
        return this.level;
    }

    public Error setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException("Cannot set error level to null");
        } else {
            this.level = level;
            return this;
        }
    }

    public String getCode() {
        return this.code;
    }

    public Error setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public Error setMessage(String message) {
        if (message == null) {
            throw new NullPointerException("Cannot set error message to null");
        } else {
            this.message = message;
            return this;
        }
    }

    public Error getCause() {
        return this.cause;
    }

    public Error setCause(Error cause) {
        this.cause = cause;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Error)) {
            return false;
        } else {
            Error error = (Error) o;
            if (this.getLevel() != error.getLevel()) {
                return false;
            } else {
                label35:
                {
                    if (this.getCode() != null) {
                        if (this.getCode().equals(error.getCode())) {
                            break label35;
                        }
                    } else if (error.getCode() == null) {
                        break label35;
                    }

                    return false;
                }

                if (!this.getMessage().equals(error.getMessage())) {
                    return false;
                } else {
                    return this.getCause() != null ? this.getCause().equals(error.getCause()) : error.getCause() == null;
                }
            }
        }
    }

    public int hashCode() {
        int result = this.getLevel().hashCode();
        result = 31 * result + (this.getCode() != null ? this.getCode().hashCode() : 0);
        result = 31 * result + this.getMessage().hashCode();
        result = 31 * result + (this.getCause() != null ? this.getCause().hashCode() : 0);
        return result;
    }

    public String toString() {
        return "Error{level=" + this.level + ", code='" + this.code + '\'' + ", message='" + this.message + '\'' + ", cause=" + this.cause + '}';
    }
}
