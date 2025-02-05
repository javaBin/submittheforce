package no.java.submit.model;

import io.quarkus.qute.TemplateEnum;

@TemplateEnum
public enum Category {
    CORE("core", "JVM, Java (core), compilers", true),
    LANG("lang", "Language, framework, API", true),
    EXP("exp", "Experience report, software application", true),
    FRONTEND("front",  "Frontend, design, user experience (UX)", true),
    ARCH("arch",  "Software architecture", true),
    PROC("proc",  "Process, methodology", true),
    AI("ai", "Artificial intelligence (AI), machine learning", true),
    DATA("data", "Database technology, data processing", true),
    SECURITY("sec", "Security", true),
    OPS("ops",  "Continuous integration/delivery (CI/CD), DevOps", true),
    QA("qa", "Testing, quality assurance", true),
    INFRA("infra", "Infrastructure, cloud", true),
    DEVEX("devex", "Developer experience", true),
    MISC("misc", "Miscellaneous", true),
    UNKNOWN("", "Unknown", false);

    public String tag;
    public String description;
    public boolean active;

    Category(String tag, String description, boolean active) {
        this.tag = tag;
        this.description = description;
        this.active = active;
    }

    public static Category of(String tag) {
        for (var v : values())
            if (v.tag.equals(tag))
                return v;

        return null;
    }
}
