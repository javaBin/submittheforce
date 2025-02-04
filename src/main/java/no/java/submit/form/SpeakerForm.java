package no.java.submit.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import no.java.submit.service.TalksService;
import org.hibernate.validator.constraints.Length;

import java.util.HashMap;
import java.util.Map;

public class SpeakerForm {

    @Length(min = 5, message = "Name is too short")
    public String name;

    @NotBlank(message = "Can't be empty")
    @Email(message = "Valid email address is required")
    public String email;

    @Pattern(regexp = "^$|^@?(\\w){1,15}$", message = "Must be a valid Twitter username")
    public String twitter;

    @Pattern(regexp = "^$|^https:\\/\\/[a-z]{2,3}\\.linkedin\\.com\\/.*$", message = "Must be a valid LinkedIn address")
    public String linkedin;

    @Pattern(regexp = "^$|^@?[\\w\\.]{1,50}$", message = "Must be a valid Bluesky username")
    public String bluesky;

    public String residence;

    @Pattern(regexp = "^$|^[0-9]{4}$", message = "Zip codes are 4 digits")
    public String zipCode;

    @NotBlank
    public String bio;

    public Map<String, TalksService.DataField<Object>> toData() {
        var result = new HashMap<String, TalksService.DataField<Object>>();
        result.put("bio", new TalksService.DataField<>(false, bio));

        if (twitter != null && !twitter.isBlank())
            result.put("twitter", new TalksService.DataField<>(false, twitter));
        if (bluesky != null && !bluesky.isBlank())
            result.put("bluesky", new TalksService.DataField<>(false, bluesky));
        if (linkedin != null && !bluesky.isBlank())
            result.put("linkedin", new TalksService.DataField<>(false, linkedin));
        if (residence != null && !residence.isBlank())
            result.put("residence", new TalksService.DataField<>(false, residence));
        if (zipCode != null && !zipCode.isBlank())
            result.put("zip-code", new TalksService.DataField<>(false, zipCode));

        return result;
    }

    public TalksService.Speaker asSpeaker() {
        var result = new TalksService.Speaker();
        result.data = toData();
        result.email = email;
        result.name = name;

        return result;
    }

    public static SpeakerForm parse(TalksService.Speaker source) {
        var result = new SpeakerForm();
        result.name = source.name;
        result.email = source.email;

        if (source.data.containsKey("bio"))
            result.bio = (String) source.data.get("bio").value;

        if (source.data.containsKey("twitter"))
            result.twitter = (String) source.data.get("twitter").value;

        if (source.data.containsKey("bluesky"))
            result.bluesky = (String) source.data.get("bluesky").value;

        if (source.data.containsKey("linkedin"))
            result.linkedin = (String) source.data.get("linkedin").value;

        if (source.data.containsKey("zip-code"))
            result.zipCode = (String) source.data.get("zip-code").value;

        if (source.data.containsKey("residence"))
            result.residence = (String) source.data.get("residence").value;

        return result;
    }

}
