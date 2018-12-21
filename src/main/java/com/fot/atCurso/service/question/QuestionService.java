package com.fot.atCurso.service.question;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.fot.atCurso.exception.RequirementsNotMetException;
import com.fot.atCurso.exception.AlreadyDoneException;
import com.fot.atCurso.exception.ConstraintBreakException;
import com.fot.atCurso.exception.NotFoundException;
import com.fot.atCurso.model.Question;
import com.fot.atCurso.model.Quiz;
import com.fot.atCurso.model.Tag;
import com.fot.atCurso.service.AbstractService;

public interface QuestionService  extends AbstractService<Question, Integer> {

	List<Question> findByTag(Integer idTag, Pageable p) throws NotFoundException;
	List<Question> findByTags(List<Tag> tags);
	
	boolean isEqual(Question q1, Question q2);
	void deleteAll(Question q);
	
	Question getAndCheck(Integer idQuestion) throws NotFoundException;
	Question getAndCheckBelongQuiz(Quiz quiz, Integer idQuestion) throws NotFoundException;
	Question checkAndCreate(Question question) throws ConstraintBreakException;
	void checkAndUpdate(Question to, Question from) throws ConstraintBreakException;
	
	void checkConditionsUserAndQuiz(Integer idUser, Integer idQuiz) throws NotFoundException;
	
	List<Question> getAndCheckQuestions(Integer idUser, Integer idQuiz) throws NotFoundException, RequirementsNotMetException, AlreadyDoneException;
}
