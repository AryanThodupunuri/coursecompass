package com.coursecompass.api;

import com.coursecompass.api.dto.AnalyzeResponse;
import com.coursecompass.service.ScraperService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class CourseController {

  private final ScraperService scraperService;

  public CourseController(ScraperService scraperService) {
    this.scraperService = scraperService;
  }

  @GetMapping("/analyze")
  public AnalyzeResponse analyze(
      @RequestParam("prof") String professorName,
      @RequestParam("course") String courseId
  ) {
    // Phase 2: DB lookup -> scrape -> return.
    // For now: scrape live each time; caching layer comes next.
    return scraperService.analyze(professorName, courseId);
  }
}
