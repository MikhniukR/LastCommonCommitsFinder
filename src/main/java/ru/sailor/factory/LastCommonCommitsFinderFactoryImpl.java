package ru.sailor.factory;

import ru.sailor.GithubLastCommonCommitsFinder;
import ru.sailor.LastCommonCommitsFinder;

public class LastCommonCommitsFinderFactoryImpl implements LastCommonCommitsFinderFactory {

    @Override
    public LastCommonCommitsFinder create(String owner, String repo, String token) {
        return new GithubLastCommonCommitsFinder(owner, repo, token);
    }
}
