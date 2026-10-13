
package com.contextgraph.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

@Service
public class MetadataService {

    static final Pattern DATE = Pattern.compile(
        "\\b(?:\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[a-z]*\\s+\\d{1,2}(?:,\\s*\\d{4})?)\\b",
        Pattern.CASE_INSENSITIVE
    );

    static final Pattern URL = Pattern.compile(
        "https?://[^\\s]+",
        Pattern.CASE_INSENSITIVE
    );

    static final Set<String> STOP = Set.of(
        "the", "and", "for", "with", "that", "this", "from",
        "your", "have", "will", "are", "was", "were", "into",
        "about", "then", "than", "they", "their", "there",
        "which", "when", "where", "what", "how", "you", "has",
        "had", "not", "but", "can", "all", "our", "use",
        "using", "also", "been", "its", "more"
    );

    public Map<String, Object> analyze(String text, String filename) {
        String s = text == null ? "" : text;

        return Map.of(
            "keywords", keywords(s),
            "entities", entities(s),
            "dates", extract(DATE, s),
            "urls", extract(URL, s),
            "topic", topic(s, filename)
        );
    }

    List<String> keywords(String s) {
        Map<String, Integer> m = new HashMap<>();

        for (String x : s.toLowerCase()
                .replaceAll("[^a-z0-9+#. ]", " ")
                .split("\\s+")) {
            if (x.length() >= 4 && !STOP.contains(x)) {
                m.merge(x, 1, Integer::sum);
            }
        }

        return m.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(12)
            .map(Map.Entry::getKey)
            .toList();
    }

    List<String> entities(String s) {
        LinkedHashSet<String> r = new LinkedHashSet<>();

        Pattern p = Pattern.compile(
            "\\b(Java|Python|JavaScript|React|Spring Boot|Node\\.js|SQL|MongoDB|MySQL|PostgreSQL|AWS|Azure|GitHub|Docker|Machine Learning|Artificial Intelligence|AI|DSA)\\b",
            Pattern.CASE_INSENSITIVE
        );

        extract(p, s).forEach(r::add);

        return r.stream().limit(25).toList();
    }

    List<String> extract(Pattern p, String s) {
        Matcher m = p.matcher(s);
        List<String> r = new ArrayList<>();

        while (m.find() && r.size() < 25) {
            r.add(m.group());
        }

        return r;
    }

    String topic(String s, String f) {
        String a = ((f == null ? "" : f) + " " + s).toLowerCase();

        if (a.contains("internship")) return "Internship";
        if (a.contains("project")) return "Project";
        if (a.contains("event") || a.contains("workshop")) return "Event";
        if (a.contains("exam") || a.contains("course")) return "Education";
        if (a.contains("resume") || a.contains("cv")) return "Career";

        return "General";
    }
}
