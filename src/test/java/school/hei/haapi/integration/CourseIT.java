package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.TeacherIT.teacher1;
import static school.hei.haapi.integration.TeacherIT.teacher2;
import static school.hei.haapi.integration.TeacherIT.teacher3;
import static school.hei.haapi.integration.conf.TestUtils.BAD_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.COURSE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.api.TeachingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.integration.conf.TestUtils;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = GroupIT.ContextInitializer.class)
@AutoConfigureMockMvc
public class CourseIT {
  @MockBean
  private SentryConf sentryConf;

  @MockBean
  private CognitoComponent cognitoComponentMock;

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, GroupIT.ContextInitializer.SERVER_PORT);
  }

  public static Course course1(){
    Course course = new Course();
    course.setId(COURSE1_ID);
    course.setCode("PROG1");
    course.setName("Algorithmics");
    course.setCredits(3);
    course.setTotalHours(40);
    course.setMainTeacher(teacher1());
    return course;
  }

  public static Course course2(){
    Course course = new Course();
    course.setId(COURSE2_ID);
    course.setCode("WEB1");
    course.setName(null);
    course.setCredits(2);
    course.setTotalHours(36);
    course.setMainTeacher(teacher1());
    return course;
  }
  public static Course course3(){
    Course course = new Course();
    course.setId(COURSE3_ID);
    course.setCode("MGT1");
    course.setName("name");
    course.setCredits(2);
    course.setTotalHours(20);
    course.setMainTeacher(teacher2());
    return course;
  }
  public static CrupdateCourse someCreatableCourse() {
    CrupdateCourse createCourse = new CrupdateCourse();
    createCourse.setId("COURSE21-" + randomUUID());
    createCourse.setName("Some name");
    createCourse.setCode("Code");
    createCourse.setCredits(5);
    createCourse.setTotalHours(40);
    createCourse.setMainTeacherId("Teacher6_id");
    return createCourse;
  }
  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
  }

  @Test
  void student_read_courses_ko() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    UsersApi api = new UsersApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentCoursesById(STUDENT2_ID, CourseStatus.LINKED));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentCoursesById(STUDENT2_ID, null));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentCoursesById(STUDENT2_ID, CourseStatus.UNLINKED));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    TeachingApi api = new TeachingApi(teacher1Client);
    assertThrowsForbiddenException(() -> api.crupdateCourses(List.of()));
  }

  @Test
  void badtoken_write_ko() {
    ApiClient anonymousClient = anApiClient(BAD_TOKEN);

    TeachingApi api = new TeachingApi(anonymousClient);
    assertThrowsForbiddenException(() -> api.crupdateCourses(List.of()));
  }

  @Test
  void student_write_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);

    TeachingApi api = new TeachingApi(student1Client);
    assertThrowsForbiddenException(() -> api.crupdateCourses(List.of()));
  }

  @Test
  void teacher_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    TeachingApi api = new TeachingApi(teacher1Client);

    Course actual1 = api.getCourseById(COURSE1_ID);
    List<Course> actualCourses = api.getCourses(null, null,1,3);

    assertEquals(course1(), actual1);
    assertTrue(actualCourses.contains(course1()));
    assertTrue(actualCourses.contains(course3()));
  }

  @Test
  void manager_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    Course actual1 = api.getCourseById(COURSE1_ID);
    List<Course> actualCourses = api.getCourses(null, null, 1,3);

    assertEquals(course1(), actual1);
    assertTrue(actualCourses.contains(course1()));
    assertTrue(actualCourses.contains(course3()));
  }

  @Test
  void manager_read_filtered_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    TeachingApi api = new TeachingApi(manager1Client);

    List<Course> actualFilteredByTeacherFirstName = api.getCourses("tWo", null, null, null);
    List<Course> actualFilteredByTeacherLastName = api.getCourses(null, "Teacher", null, null);
    List<Course> actualFilteredByTeacherFirstNameContaining = api.getCourses("O", null, null, null);
    List<Course> actualFilteredByTeacherLastNameContaining = api.getCourses(null, "eAc", null,
        null);
    List<Course> actualFilteredByTeacherFirstAndLastName = api.getCourses("tHrEe", "teaCH", null,
        null);

    assertEquals(1, actualFilteredByTeacherFirstName.size());
    assertEquals(2, actualFilteredByTeacherLastName.size());
    assertEquals(2, actualFilteredByTeacherFirstNameContaining.size());
    assertEquals(3, actualFilteredByTeacherLastNameContaining.size());
    assertEquals(1, actualFilteredByTeacherFirstAndLastName.size());
    assertTrue(actualFilteredByTeacherFirstName.contains(teacher2()));
    assertTrue(actualFilteredByTeacherLastName.containsAll(List.of(teacher1(), teacher2())));
    assertTrue(actualFilteredByTeacherFirstNameContaining.containsAll(List.of(teacher1(), teacher2())));
    assertTrue(actualFilteredByTeacherLastNameContaining.containsAll(List.of(teacher1(),
        teacher2(), teacher3())));
    assertTrue(actualFilteredByTeacherFirstAndLastName.contains(teacher3()));
  }
  @Test
  void student_read_ok() throws school.hei.haapi.endpoint.rest.client.ApiException {
    ApiClient studentClient = anApiClient(STUDENT1_TOKEN);
    TeachingApi api = new TeachingApi(studentClient);

    Course actual1 = api.getCourseById(COURSE1_ID);
    List<Course> actualCourses = api.getCourses(null, null, 1,3);

    assertEquals(course1(), actual1);
    assertTrue(actualCourses.contains(course1()));
    assertTrue(actualCourses.contains(course3()));
  }
}
