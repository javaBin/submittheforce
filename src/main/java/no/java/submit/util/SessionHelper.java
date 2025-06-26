package no.java.submit.util;

import no.java.submit.service.TalksService;

import java.util.ArrayList;
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
}
