package no.java.submit.util;

import no.java.submit.service.TalksService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface SessionHelper {

    @SuppressWarnings("unchecked")
    static List<String> getTags(TalksService.Session session) {
        var result = new ArrayList<String>();

        if (session.data.containsKey("tagswithauthor")) {
            var tags = (List< Map<String, String>>) session.data.get("tagswithauthor").value;
            for (Map<String, String> tag : tags) {
                if (tag.containsKey("tag")) {
                    result.add(tag.get("tag"));
                }
            }
        }

        if (session.data.containsKey("tags")) {
            result.addAll((List<String>) session.data.get("tags").value);
        }

        return result;
    }

    static void partialUpdate(TalksService.Session session, TalksService.Session updates) {
        for (var key : Arrays.asList("title", "abstract", "outline", "intendedAudience", "equipment", "participation", "workshopPrerequisites", "suggestedKeywords", "infoToProgramCommittee", "interestedInLocalCommunity")) {
            if (updates.data.containsKey(key))
                session.data.put(key, updates.data.get(key));
            else
                session.data.remove(key);
        }

        for (var speaker : session.speakers) {
            var updated = updates.getSpeaker(speaker.name);

            if (updated != null) {
                speaker.id = null;
                for (var key : Arrays.asList("email", "twitter", "bluesky", "linkedin", "residence", "zipCode", "bio")) {
                    if (updated.data.containsKey(key))
                        speaker.data.put(key, updated.data.get(key));
                    else
                        speaker.data.remove(key);
                }
            }
        }
    }
}
