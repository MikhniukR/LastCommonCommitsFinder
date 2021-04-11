package ru.sailor.factory;

import ru.sailor.GithubLastCommonCommitsFinder;
import ru.sailor.LastCommonCommitsFinder;

public class LastCommonCommitsFinderFactoryImpl implements LastCommonCommitsFinderFactory {

    @Override
    public LastCommonCommitsFinder create(String owner, String repo, String token) {
        if (owner == null || repo == null || owner.isBlank() || repo.isBlank()) {
            throw new IllegalArgumentException("For getting repo information owner and repo should be not null/Blank");
        }

        return new GithubLastCommonCommitsFinder(owner, repo, token);
    }
}
