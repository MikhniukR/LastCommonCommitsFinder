package ru.sailor.data;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GitCommit {

    String sha;
    LocalDateTime timestamp;
    List<GitCommit> parents;

}
