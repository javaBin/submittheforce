package no.java.submit.model;

import io.quarkus.qute.TemplateEnum;
import no.java.submit.service.TalksService;

import java.util.Map;

@TemplateEnum
public enum Language {
    NORWEGIAN("Norwegian", "no"),
    ENGLISH("English", "en");

    public String name;
    public String key;

    Language(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public static Language of(String key) {
        for (var value : values())
            if (value.key.equals(key))
                return value;

        return NORWEGIAN;
    }

    public static Language of(Map<String, TalksService.DataField<Object>> data) {
        if (!data.containsKey("language"))
            return NORWEGIAN;

        return of((String) data.get("language").value);
    }
}
