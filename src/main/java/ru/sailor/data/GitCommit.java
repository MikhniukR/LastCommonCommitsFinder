package ru.sailor.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GitCommit {
    String sha;
    List<GitCommit> parents;

}
