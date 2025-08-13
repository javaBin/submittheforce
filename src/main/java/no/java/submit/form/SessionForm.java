package no.java.submit.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import no.java.submit.model.Category;
import no.java.submit.model.Kind;
import no.java.submit.model.Language;
import no.java.submit.service.TalksService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionForm {

    @NotBlank(message = "Must not be blank")
    public String title;

    @Valid
    public List<SpeakerForm> speakers;

    @NotNull(message = "Must be provided")
    public Kind kind;

    @NotNull(message = "Must be provided")
    public Language language;

    @NotBlank(message = "Must not be blank")
    public String abstractText;

    @NotBlank(message = "Must not be blank")
    public String outline;

    public String equipment;

    @NotBlank(message = "Must not be blank")
    public String participation;

    public String suggestedKeywords;

    @NotBlank(message = "Must not be blank")
    public String intendedAudience;

    public String workshopPrerequisites;

    public String infoToProgramCommittee;

    public Category suggestedCategory;

    @AssertTrue(message = "Must select a valid option")
    // Make sure one of the active kinds are selected
    private boolean isKind() {
        return kind.active;
    }

    @AssertTrue(message = "Must be provided for workshops")
    private boolean isWorkshopPrerequisites() {
        return !kind.isWorkshop() || (workshopPrerequisites != null && !workshopPrerequisites.isBlank());
    }

    @AssertTrue(message = "Maximum 3 speakers for workshops and 2 speakers for talks")
    private boolean isSpeakers() {
        return speakers.size() <= (kind.isWorkshop() ? 3 : 2);
    }

    public Map<String, TalksService.DataField<Object>> toData() {
        var result = new HashMap<String, TalksService.DataField<Object>>();
        result.put("title", new TalksService.DataField<>(false, title));
        result.put("format", new TalksService.DataField<>(false, kind.format));
        result.put("length", new TalksService.DataField<>(false, String.valueOf(kind.length)));
        result.put("language", new TalksService.DataField<>(false, language.key));
        result.put("abstract", new TalksService.DataField<>(false, abstractText));
        result.put("outline", new TalksService.DataField<>(true, outline));
        result.put("equipment", new TalksService.DataField<>(true, equipment));
        result.put("participation", new TalksService.DataField<>(true, participation));
        result.put("intendedAudience", new TalksService.DataField<>(false, intendedAudience));
        result.put("suggestedKeywords", new TalksService.DataField<>(false, suggestedKeywords));
        result.put("infoToProgramCommittee", new TalksService.DataField<>(true, infoToProgramCommittee));
        result.put("suggestedCategory", new TalksService.DataField<>(true, suggestedCategory != null ? suggestedCategory.tag : ""));

        if (kind.isWorkshop())
            result.put("workshopPrerequisites", new TalksService.DataField<>(false, workshopPrerequisites));

        return result;
    }

    public TalksService.Session asSession() {
        var result = new TalksService.Session();
        result.data = toData();
        result.speakers = speakers.stream().map(SpeakerForm::asSpeaker).toList();

        return result;
    }

    public static SessionForm parse(TalksService.Session source) {
        var result = new SessionForm();
        result.title = (String) source.data.get("title").value;
        result.kind = Kind.of(source.data);
        result.language = Language.of(source.data);
        result.abstractText = (String) source.data.get("abstract").value;
        result.outline = (String) source.data.get("outline").value;
        if (source.data.containsKey("intendedAudience"))
            result.intendedAudience = (String) source.data.get("intendedAudience").value;
        if (source.data.containsKey("equipment"))
            result.equipment = (String) source.data.get("equipment").value;
        if (source.data.containsKey("participation"))
            result.participation = (String) source.data.get("participation").value;
        if (source.data.containsKey("workshopPrerequisites"))
            result.workshopPrerequisites = (String) source.data.get("workshopPrerequisites").value;
        if (source.data.containsKey("suggestedKeywords"))
            result.suggestedKeywords = (String) source.data.get("suggestedKeywords").value;
        if (source.data.containsKey("infoToProgramCommittee"))
            result.infoToProgramCommittee = (String) source.data.get("infoToProgramCommittee").value;
        if (source.data.containsKey("suggestedCategory"))
            result.suggestedCategory = Category.of((String) source.data.get("suggestedCategory").value);

        result.speakers = source.speakers.stream().map(SpeakerForm::parse).toList();

        return result;
    }
}
