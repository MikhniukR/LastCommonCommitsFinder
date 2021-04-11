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
    private final Integer MAX_PARENTS_COUNT = 2;

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
        makeRequestToGithubOnMerge(branchA.getLastCommit());
        makeRequestToGithubOnMerge(branchB.getLastCommit());

        var previousA = new HashSet<String>() {{
            add(branchA.getLastCommit().getSha());
        }};
        var previousB = new HashSet<String>() {{
            add(branchB.getLastCommit().getSha());
        }};

        //queue to get the newestCommit from not processed
        var commitsQueue = new PriorityQueue<GitCommit>((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp()));
        var uniqInCommitsQueueSha = new HashSet<String>();
        //queue to get the newestCommit from reachable from A and B commits
        var reachableCommits = new PriorityQueue<GitCommit>((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp()));
        var uniqInReachableCommitsSha = new HashSet<String>();
        commitsQueue.add(branchA.getLastCommit());
        commitsQueue.add(branchB.getLastCommit());
        uniqInCommitsQueueSha.add(branchA.getLastCommit().getSha());
        uniqInCommitsQueueSha.add(branchB.getLastCommit().getSha());

        var commonCommitsSha = new HashSet<String>();
        while (!commitsQueue.isEmpty()) {
            //process common commit to don't add extra common commits,
            // to result, that are reachable from another common commit.
            if (isSameCommits(commitsQueue, reachableCommits)) {
                commitsQueue.poll();
                continue;
            }
            if (!reachableCommits.isEmpty() && isReachableCommitNewest(commitsQueue, reachableCommits)) {
                addParentsToQueue(reachableCommits.poll(), reachableCommits, uniqInReachableCommitsSha);
                continue;
            }

            //process new uncommon commit
            var newestCommit = commitsQueue.poll();
            if (previousA.contains(newestCommit.getSha()) && previousB.contains(newestCommit.getSha())) {
                commonCommitsSha.add(newestCommit.getSha());
                reachableCommits.add(newestCommit);
                continue;
            }

            addParentsToQueue(newestCommit, commitsQueue, uniqInCommitsQueueSha);
            addParentsToPrevious(newestCommit, previousA, previousB);
        }

        return commonCommitsSha;
    }

    private void makeRequestToGithubOnMerge(GitCommit commit) throws GitCommunicationException {
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
        if (commitsQueue.isEmpty() || reachableCommits.isEmpty()) {
            return false;
        }

        return commitsQueue.peek().equals(reachableCommits.peek());
    }

    private boolean isReachableCommitNewest(PriorityQueue<GitCommit> commitsQueue, PriorityQueue<GitCommit> reachableCommits) {
        if (commitsQueue.isEmpty() || reachableCommits.isEmpty()) {
            return false;
        }

        return commitsQueue.peek().getTimestamp().compareTo(reachableCommits.peek().getTimestamp()) > 0;
    }

    private void addParentsToPrevious(GitCommit newestCommit, HashSet<String> previousA, HashSet<String> previousB) {
        for (int i = 0; i < MAX_PARENTS_COUNT; i++) {
            if (newestCommit.getParents().size() > i) {
                if (previousA.contains(newestCommit.getSha())) {
                    previousA.add(newestCommit.getParents().get(i).getSha());
                } else {
                    previousB.add(newestCommit.getParents().get(i).getSha());
                }
            }
        }
    }

    private void addLastBranchCommitToKnownCommits(GitBranch branchA, GitBranch branchB) {
        knownCommits.put(branchA.getLastCommit().getSha(), branchA.getLastCommit());
        knownCommits.put(branchB.getLastCommit().getSha(), branchB.getLastCommit());
    }

    private void addParentsToQueue(GitCommit commit, PriorityQueue<GitCommit> commitsQueue,
                                   HashSet<String> uniqInCommitsQueueSha) throws GitCommunicationException {
        for (int i = 0; i < MAX_PARENTS_COUNT; i++) {
            if (commit.getParents().size() > i) {
                if (!knownCommits.containsKey(commit.getParents().get(i).getSha())) {
                    knownCommits.putAll(
                            githubClient.getCommitHistory(commit.getParents().get(i).getSha(), maxCommitsToFastAsk).stream()
                                    .collect(Collectors.toMap(GitCommit::getSha, c -> c))
                    );
                }

                if (!uniqInCommitsQueueSha.contains(commit.getParents().get(i).getSha())) {
                    uniqInCommitsQueueSha.add(commit.getParents().get(i).getSha());
                    commitsQueue.add(knownCommits.get(commit.getParents().get(i).getSha()));
                }
            }
        }
    }

}
