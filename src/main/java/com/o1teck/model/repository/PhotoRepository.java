package com.o1teck.model.repository;

import org.springframework.data.repository.CrudRepository;

import com.o1teck.model.entity.Photo;

public interface PhotoRepository extends CrudRepository<Photo, Long> {
}
