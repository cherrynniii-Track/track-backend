package com.track.track.repository.projection;

import com.track.track.enums.task.TaskDifficulty;

public interface TaskDifficultyCount {

    TaskDifficulty getDifficulty();

    Long getCount();
}