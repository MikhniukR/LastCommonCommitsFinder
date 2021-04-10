package ru.sailor.converter;

import ru.sailor.data.GitCommit;
import ru.sailor.data.GithubCommit;

import java.util.stream.Collectors;

public class GithubCommitToGitCommitConverter {

    public static GitCommit toGit(GithubCommit githubCommit) {
        return GitCommit.builder()
                .sha(githubCommit.getSha())
                .parents(githubCommit.getParents().stream()
                        .map(GithubCommitToGitCommitConverter::toGit)
                        .collect(Collectors.toList())
                )
                .build();
    }

}
