package com.track.track.repository.projection;

import com.track.track.enums.task.TaskStatus;

public interface TaskStatusCount {

    TaskStatus getStatus();

    Long getCount();
}