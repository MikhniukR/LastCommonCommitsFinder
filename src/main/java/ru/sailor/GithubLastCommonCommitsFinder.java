package ru.sailor;

import ru.sailor.client.GitClient;
import ru.sailor.client.GithubClient;
import ru.sailor.data.GitBranch;
import ru.sailor.data.GitCommit;
import ru.sailor.exceptions.GitCommunicationException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class GithubLastCommonCommitsFinder implements LastCommonCommitsFinder {

    private final Integer maxCommitsToFastAsk;
    private final GitClient githubClient;
    private Map<String, GitCommit> knownCommits;

    public GithubLastCommonCommitsFinder(String owner, String repo, String token) {
        if (token == null) {
            githubClient = new GithubClient(owner, repo);
        } else {
            githubClient = new GithubClient(owner, repo, token);
        }
        knownCommits = new HashMap<>();
        maxCommitsToFastAsk = GithubClient.MAX_COMMITS_PER_PAGE;
    }

    @Override
    public Collection<String> findLastCommonCommits(String branchAName, String branchBName) throws IOException {
        var branchA = githubClient.getBranchInfo(branchAName);
        var branchB = githubClient.getBranchInfo(branchBName);
        addLastBranchCommitToKnownCommits(branchA, branchB);
        //make extra request to potentially not doing two requests
        makeRequestIfMergeCommit(branchA.getLastCommit());
        makeRequestIfMergeCommit(branchB.getLastCommit());

        var previousA = new HashSet<String>() {{
            add(branchA.getLastCommit().getSha());
        }};
        var previousB = new HashSet<String>() {{
            add(branchB.getLastCommit().getSha());
        }};

        //queue to get the newestCommit from not processed
        var commitsQueue = new PriorityQueue<GitCommit>((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp()));
        //queue to get the newestCommit from reachable from A and B commits
        var reachableCommits = new PriorityQueue<GitCommit>((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp()));
        commitsQueue.add(branchA.getLastCommit());
        commitsQueue.add(branchB.getLastCommit());

        var commonCommitsSha = new HashSet<String>();
        while (!commitsQueue.isEmpty()) {
            //process common commit to don't add extra common commits,
            // to result, that are reachable from another common commit.
            if (isSameCommits(commitsQueue, reachableCommits)) {
                commitsQueue.poll();
                continue;
            }
            if (!reachableCommits.isEmpty() && isReachableCommitNewest(commitsQueue, reachableCommits)) {
                addParentsToQueue(reachableCommits.poll(), reachableCommits);
                continue;
            }

            //process new uncommon commit
            var newestCommit = commitsQueue.poll();
            if (previousA.contains(newestCommit.getSha()) && previousB.contains(newestCommit.getSha())) {
                if (!commonCommitsSha.contains(newestCommit.getSha())) {
                    commonCommitsSha.add(newestCommit.getSha());
                    reachableCommits.add(newestCommit);
                }
                continue;
            }

            addParentsToQueue(newestCommit, commitsQueue);
            addParentsToPrevious(newestCommit, previousA, previousB);
        }

        return commonCommitsSha;
    }

    private void makeRequestIfMergeCommit(GitCommit commit) throws GitCommunicationException {
        if (commit.hasSecondParent() &&
                !knownCommits.containsKey(commit.getFirstParentSha()) &&
                !knownCommits.containsKey(commit.getSecondParentSha())) {
            knownCommits.putAll(
                    githubClient.getCommitHistory(commit.getSha(), maxCommitsToFastAsk).stream()
                            .collect(Collectors.toMap(GitCommit::getSha, c -> c))
            );
        }
    }

    private boolean isSameCommits(PriorityQueue<GitCommit> commitsQueue, PriorityQueue<GitCommit> reachableCommits) {
        if (reachableCommits.isEmpty()) {
            return false;
        }

        return commitsQueue.peek().equals(reachableCommits.peek());
    }

    private boolean isReachableCommitNewest(PriorityQueue<GitCommit> commitsQueue, PriorityQueue<GitCommit> reachableCommits) {
        return commitsQueue.peek().getTimestamp().compareTo(reachableCommits.peek().getTimestamp()) > 0;
    }

    private void addParentsToPrevious(GitCommit newestCommit, HashSet<String> previousA, HashSet<String> previousB) {
        if (newestCommit.hasFirstParent()) {
            if (previousA.contains(newestCommit.getSha())) {
                previousA.add(newestCommit.getFirstParentSha());
            } else {
                previousB.add(newestCommit.getFirstParentSha());
            }
        }
        if (newestCommit.hasSecondParent()) {
            if (previousA.contains(newestCommit.getSha())) {
                previousA.add(newestCommit.getSecondParentSha());
            } else {
                previousB.add(newestCommit.getSecondParentSha());
            }
        }
    }

    private void addLastBranchCommitToKnownCommits(GitBranch branchA, GitBranch branchB) {
        knownCommits.put(branchA.getLastCommit().getSha(), branchA.getLastCommit());
        knownCommits.put(branchB.getLastCommit().getSha(), branchB.getLastCommit());
    }

    private void addParentsToQueue(GitCommit newestCommit, PriorityQueue<GitCommit> commitsQueue) throws GitCommunicationException {
        if (newestCommit.hasFirstParent()) {
            if (!knownCommits.containsKey(newestCommit.getFirstParentSha())) {
                knownCommits.putAll(
                        githubClient.getCommitHistory(newestCommit.getFirstParentSha(), maxCommitsToFastAsk).stream()
                                .collect(Collectors.toMap(GitCommit::getSha, commit -> commit))
                );
            }

            commitsQueue.add(knownCommits.get(newestCommit.getFirstParentSha()));
        }
        if (newestCommit.hasSecondParent()) {
            if (!knownCommits.containsKey(newestCommit.getSecondParentSha())) {
                knownCommits.putAll(
                        githubClient.getCommitHistory(newestCommit.getSecondParentSha(), maxCommitsToFastAsk).stream()
                                .collect(Collectors.toMap(GitCommit::getSha, commit -> commit))
                );
            }

            commitsQueue.add(knownCommits.get(newestCommit.getSecondParentSha()));
        }
    }

}
