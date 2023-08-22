package it.unimi.cloudproject.ui.testcontainer.model;

public class SetupScriptResults {
    public record SetupScript(String restApiId,
                              String apiUsersResourceId,
                              String apiUsersWithIdResourceId,
                              String apiShopsResourceId) {}
}
