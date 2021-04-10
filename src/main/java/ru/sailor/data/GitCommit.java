package ru.sailor.data;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class GitCommit {

    String sha;
    LocalDateTime timestamp;
    List<GitCommit> parents;

    public Boolean hasFirstParent() {
        return parents.size() > 0;
    }

    public Boolean hasSecondParent() {
        return parents.size() > 1;
    }

    public String getFirstParentSha() {
        return parents.get(0).getSha();
    }

    public String getSecondParentSha() {
        return parents.get(1).getSha();
    }

}
