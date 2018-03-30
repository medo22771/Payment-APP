package com.yelloco.payment.utils;

import com.alcineo.administrative.commands.GetConfList;
import java.util.List;

public enum ConfigFileType {

    CONTACT("Contact"),
    EXCEPTION_FILE("Exception file"),
    REVOCATION_LIST("Revocation list"),
    CA_PUBLIC_KEYS("CA Public keys"),
    LANGUAGES("Languages");

    private final String name;

    ConfigFileType(String name) {
        this.name = name;
    }

    public static ConfigFileType getType(String name) {
        for (ConfigFileType type: values()) {
            if (type.name().equals(name))
                return type;
        }
        throw new IllegalArgumentException("Config file name not supported: " + name);
    }

    public byte getId(List<GetConfList.UploadableFile> files) {
        for (GetConfList.UploadableFile file: files) {
            if (file.name.equals(this.name))
                return file.id;
        }
        throw new IllegalArgumentException("Config file not supported: " + this.name);
    }
}
