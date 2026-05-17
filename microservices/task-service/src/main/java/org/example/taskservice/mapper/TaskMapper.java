package org.example.taskservice.mapper;

import org.example.taskservice.dto.TaskResponse;
import org.example.taskservice.entity.Tag;
import org.example.taskservice.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "tagIds", expression = "java(mapTagIds(task.getTags()))")
    TaskResponse toResponse(Task task);

    default List<Long> mapTagIds(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream().map(Tag::getId).toList();
    }
}
