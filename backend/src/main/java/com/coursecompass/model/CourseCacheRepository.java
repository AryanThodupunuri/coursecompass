package com.coursecompass.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseCacheRepository extends JpaRepository<CourseCache, Long> {
  Optional<CourseCache> findFirstByProfessorNameAndCourseIdOrderByLastUpdatedDesc(
      String professorName,
      String courseId
  );
}
