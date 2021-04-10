package ru.sailor.converter;

import ru.sailor.data.GitBranch;
import ru.sailor.data.GithubBranch;

public class GithubBranchToGitCommitConverter {

    public static GitBranch toGit(GithubBranch githubBranch) {
        return GitBranch.builder()
                .lastCommit(GithubCommitToGitCommitConverter.toGit(githubBranch.getCommit()))
                .name(githubBranch.getName())
                .build();
    }

}
