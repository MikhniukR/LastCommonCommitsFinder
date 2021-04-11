package ru.sailor.client;

import ru.sailor.data.GitBranch;
import ru.sailor.data.GitCommit;
import ru.sailor.exceptions.GitCommunicationException;

import java.util.List;

public interface GitClient {

    List<GitCommit> getCommitHistory(String commitSHA, Integer countOfCommits) throws GitCommunicationException;

    GitBranch getBranchInfo(String branchName) throws GitCommunicationException;


}
