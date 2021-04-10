package ru.sailor.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitBranch {
    String name;
    GitCommit commit;
}
