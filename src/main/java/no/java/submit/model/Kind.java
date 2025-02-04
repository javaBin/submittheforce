package no.java.submit.model;

import io.quarkus.qute.TemplateEnum;
import no.java.submit.service.TalksService;

import java.util.Map;

@TemplateEnum
public enum Kind {
    PRESENTATION("Presentation", "presentation", 0, false),
    PRESENTATION_45("Presentation, 45 mins", "presentation", 45, true),
    PRESENTATION_60("Presentation, 60 mins", "presentation", 60, true),
    LIGHTNING_TALK_10("Lightning talk, 10 mins", "lightning-talk", 10, true),
    LIGHTNING_TALK_20("Lightning talk, 20 mins", "lightning-talk", 20, true),
    WORKSHOP_120("Workshop, 2 hours", "workshop", 120, true),
    WORKSHOP_240("Workshop, 4 hours", "workshop", 240, true),
    WORKSHOP_480("Workshop, 8 hours", "workshop", 480, true),
    UNKNOWN("Unknown", "other", 0, false);

    public String name;
    public String format;
    public int length;
    public boolean active;

    Kind(String name, String format, int length, boolean active) {
        this.name = name;
        this.format = format;
        this.length = length;
        this.active = active;
    }

    public static Kind of(String format, int length) {
        for (var value : values())
            if (value.format.equals(format) && value.length == length)
                return value;

        return UNKNOWN;
    }

    public static Kind of(Map<String, TalksService.DataField<Object>> data) {
        if (!data.containsKey("format"))
            return PRESENTATION;

        return of((String) data.get("format").value, (data.containsKey("length") ? Integer.parseInt(String.valueOf(data.get("length").value)) : 0));
    }

    public boolean isWorkshop() {
        return "workshop".equals(format);
    }
}
