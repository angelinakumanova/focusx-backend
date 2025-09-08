package app.focusx.model;

public enum EmailType {
    WELCOME("welcome-email"),
    VERIFICATION("verification-email");

    private final String template;

    EmailType(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}
