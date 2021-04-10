package ru.sailor;

import ru.sailor.client.GitClient;
import ru.sailor.client.GithubClient;

import java.io.IOException;
import java.util.Collection;

public class GithubLastCommonCommitsFinder implements LastCommonCommitsFinder {

    private final GitClient githubClient;

    public GithubLastCommonCommitsFinder(String owner, String repo, String token) {

        githubClient = new GithubClient(owner, repo);
    }

    @Override
    public Collection<String> findLastCommonCommits(String branchA, String branchB) throws IOException {
        return null;
    }
}
