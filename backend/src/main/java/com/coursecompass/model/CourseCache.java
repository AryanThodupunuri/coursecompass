package com.coursecompass.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "course_cache",
    indexes = {
        @Index(name = "idx_course_cache_prof_course", columnList = "professorName, courseId")
    }
)
public class CourseCache {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String professorName;

  @Column(nullable = false)
  private String courseId;

  private Double avgRating;

  @Column(length = 2000)
  private String sentimentSummary;

  private Instant lastUpdated;

  public CourseCache() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getProfessorName() {
    return professorName;
  }

  public void setProfessorName(String professorName) {
    this.professorName = professorName;
  }

  public String getCourseId() {
    return courseId;
  }

  public void setCourseId(String courseId) {
    this.courseId = courseId;
  }

  public Double getAvgRating() {
    return avgRating;
  }

  public void setAvgRating(Double avgRating) {
    this.avgRating = avgRating;
  }

  public String getSentimentSummary() {
    return sentimentSummary;
  }

  public void setSentimentSummary(String sentimentSummary) {
    this.sentimentSummary = sentimentSummary;
  }

  public Instant getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Instant lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
}
