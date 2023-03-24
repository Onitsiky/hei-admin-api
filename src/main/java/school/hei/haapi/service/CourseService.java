package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.dao.CourseManagerDao;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;
  private final CourseManagerDao courseManagerDao;

  public List<Course> getAllCourses(String code, String name, Integer credits, String teacherFirstName, String teacherLastName,
                                    PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return courseManagerDao.findCoursesByCriteria(code, name, credits,teacherFirstName, teacherLastName, pageable);
  }

  public Course getCourseById(String courseId) {
    return repository.getById(courseId);
  }

  public List<Course> crupdateCourses(List<Course> toCrupdate) {
    return repository.saveAll(toCrupdate);
  }
}
