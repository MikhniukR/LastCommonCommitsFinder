package ru.sailor.data;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GitBranch {

    String name;
    GitCommit lastCommit;
}
