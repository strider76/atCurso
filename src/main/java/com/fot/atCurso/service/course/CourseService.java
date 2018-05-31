package com.fot.atCurso.service.course;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fot.atCurso.exceptions.NotFoundException;
import com.fot.atCurso.model.Course;
import com.fot.atCurso.model.Quiz;
import com.fot.atCurso.model.User;
import com.fot.atCurso.service.AbstractService;

public interface CourseService  extends AbstractService<Course, Integer> {

	boolean isEqual(Course u1, Course u2);
	void setValues(Course to, Course from);
	
	Set<User> findCourseUsers(Pageable p, Integer idCourse) throws NotFoundException;
	Set<Quiz> findCourseQuestionaries(Pageable p, Integer idCourse) throws NotFoundException;
}
