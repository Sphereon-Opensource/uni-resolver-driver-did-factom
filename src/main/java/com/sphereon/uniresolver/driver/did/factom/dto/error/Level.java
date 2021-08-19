//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.sphereon.uniresolver.driver.did.factom.dto.error;

import io.swagger.annotations.ApiModel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
@ApiModel(
        description = "error level"
)
public enum Level {
    @XmlEnumValue("INFO")
    INFO,
    @XmlEnumValue("WARNING")
    WARNING,
    @XmlEnumValue("FATAL")
    FATAL;

    Level() {
    }

    public static boolean is(Error error, Level level, boolean checkCauses) {
        if (error == null) {
            return false;
        } else {
            boolean match = error.getLevel() == level;
            if (match) {
                return true;
            } else {
                return checkCauses && is(error.getCause(), level, true);
            }
        }
    }
}
