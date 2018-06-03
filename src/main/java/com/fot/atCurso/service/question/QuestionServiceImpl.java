package com.fot.atCurso.service.question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fot.atCurso.dao.QuestionDAO;
import com.fot.atCurso.enums.ModalityEnum;
import com.fot.atCurso.exception.CannotGetNewQuestionWithAnswerBeforeException;
import com.fot.atCurso.exception.AlreadyDoneException;
import com.fot.atCurso.exception.ConstraintBreakException;
import com.fot.atCurso.exception.NotFoundException;
import com.fot.atCurso.model.Answer;
import com.fot.atCurso.model.Course;
import com.fot.atCurso.model.Question;
import com.fot.atCurso.model.Quiz;
import com.fot.atCurso.model.Selection;
import com.fot.atCurso.model.Tag;
import com.fot.atCurso.model.User;
import com.fot.atCurso.service.AbstractServiceImpl;
import com.fot.atCurso.service.answer.AnswerService;
import com.fot.atCurso.service.course.CourseService;
import com.fot.atCurso.service.quiz.QuizService;
import com.fot.atCurso.service.selection.SelectionService;
import com.fot.atCurso.service.tag.TagService;
import com.fot.atCurso.service.user.UserService;

@Service
public class QuestionServiceImpl extends AbstractServiceImpl<Question, QuestionDAO> implements QuestionService {
	
	private static final Integer maxAnswers = 4;
	
	@Autowired
	QuestionDAO questionDAO;
	
	@Autowired
	AnswerService answerService;
	
	@Autowired
	TagService tagService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	QuizService quizService;
	
	@Autowired
	CourseService courseService;
	
	@Autowired
	SelectionService selectionService;
	
	@Override
	public List<Question> findByTag(Integer idTag, Pageable p) throws NotFoundException {
		Tag tag = tagService.getAndCheck(idTag);
		return questionDAO.findByTag(tag, p);
	}
	
	@Override
	public boolean isEqual(Question q1, Question q2) {
		return q1.getName().equals(q2.getName()) &&
				q1.getTag().equals(q2.getTag()) &&
				q1.getDifficulty().equals(q2.getDifficulty()) &&
				CollectionUtils.isEqualCollection(getStringsAnswer(q1.getAnswer()),
												  getStringsAnswer(q2.getAnswer()));
	}
	
	@Override 
	public void setValues(Question to, Question from) {
		to.setName(from.getName());
		to.setTag(from.getTag());
		to.setDifficulty(from.getDifficulty());
		updateAnswers(to, from);
	}
	
	@Override
	public void deleteAll(Question q) {
		deleteAnswers(q);
		questionDAO.delete(q);
	}
	
	@Override
	public Question checkAndCreate(Question question) throws ConstraintBreakException {
		if(validate(question)) {
			addNewsAnswers(question);
			return questionDAO.save(question);
		}
		throw new ConstraintBreakException("El número de respuestas es incorrecto (1-" + maxAnswers + ") y solo debe existir una correcta.");
	}
	
	@Override
	public void checkAndUpdate(Question to, Question from) throws ConstraintBreakException {
		if(validate(from)) {
			setValues(to, from);
			questionDAO.save(to);
		}
		else 
			throw new ConstraintBreakException("El número de respuestas es incorrecto (1-" + maxAnswers + ") y solo debe existir una correcta.");
	}
	
	@Override
	public Question getAndCheck(Integer idQuestion) throws NotFoundException {
		Optional<Question> question = findById(idQuestion);
		question.orElseThrow(() -> new NotFoundException("La pregunta no existe"));
		return question.get();
	}
	
	@Override
	public Question getAndCheckBelongQuiz(Quiz quiz, Integer idQuestion) throws NotFoundException {
		final Optional<Question> question = quiz.getQuestion().stream().filter(q -> q.getIdQuestion() == idQuestion).findFirst();
		question.orElseThrow(() -> new NotFoundException("Esta pregunta no pertenece a este cuestionario"));
		return question.get();
	}
	
	@Override
	public List<Question> getAndCheckQuestions(Integer idUser, Integer idQuiz) throws NotFoundException, CannotGetNewQuestionWithAnswerBeforeException, AlreadyDoneException {
		checkConditionsUserAndQuiz(idUser, idQuiz);
		Quiz quiz = quizService.getAndCheck(idQuiz);
		User user = userService.getAndCheck(idUser);
		if(quiz.getModality() == ModalityEnum.ALLINONE) return getAllQuestions(user, quiz); 
		else return Collections.singletonList(getOneQuestion(user, quiz));
	}
	
	@Override
	public void checkConditionsUserAndQuiz(Integer idUser, Integer idQuiz) throws NotFoundException {
		Optional<Course> course = courseService.findByQuiz(idQuiz);
		course.orElseThrow(() -> new NotFoundException("El cuestionario no existe"));
		userService.getAndCheckBelongCourse(course.get(), idUser);
	}
	
	private List<Question> getAllQuestions(User user, Quiz quiz) throws AlreadyDoneException {
		List<Question> questions = quiz.getQuestion();
		if(selectionService.isFirstTime(user, quiz))
			selectionService.create(user, quiz, questions);
		else throw new AlreadyDoneException("Ya has iniciado el cuestionario, respondelas por favor");
		Collections.shuffle(questions);
		return questions;
	}

	private Question getOneQuestion(User user, Quiz quiz) throws CannotGetNewQuestionWithAnswerBeforeException, AlreadyDoneException {
		List<Question> questions = quiz.getQuestion();
		Collections.shuffle(questions);
		if(selectionService.isFirstTime(user, quiz)) {
			selectionService.create(user, quiz, questions.get(0));
			return questions.get(0);
		}
		else {
			List<Selection> selections = selectionService.findByUserAndQuiz(user, quiz);
			if(selections.get(0).getRespondedDate() == null)
				throw new CannotGetNewQuestionWithAnswerBeforeException("No puedes obtener una nueva pregunta si no has respondido la anterior");
			if(selectionService.allQuestionsBeenAnswered(user, quiz)) 
				throw new AlreadyDoneException("Ya has realizado este cuestionario, revisa tu expediente");
			return getOtherQuestion(user, quiz, selections);
		}
	}
	
	private Question getOtherQuestion(User user, Quiz quiz, List<Selection> selections) throws AlreadyDoneException {
		List<Question> questions = quiz.getQuestion();
		for(Question q : questions) {
			int i = 0;
			boolean found = false;
			while(!found && i < selections.size()) {
				if(q.getName() == selections.get(i).getQuestion()) found = true;
				i++;
			}
			if(!found) {
				selectionService.create(user, quiz, q);
				return q;
			}
		}
		throw new AlreadyDoneException("Ya no quedan mas preguntas, has finalizado el cuestionario, revisa tu expediente.");
	}

	private boolean validate(Question question) {
		if(question.getAnswer().size() > 0 && question.getAnswer().size() <= maxAnswers)
			return question.getAnswer().stream().filter(a -> a.getCorrect() != null && a.getCorrect()).count() == 1;
		return false;
	}
	
	private void deleteAnswers(Question q) {
		if(q.getAnswer() != null) {
			for(Answer a : q.getAnswer()) {
				answerService.delete(a);
			}
			q.setAnswer(new ArrayList<Answer>());
		}
	}
	
	private void addNewsAnswers(Question q) {
		if(q.getAnswer() != null)
			for(Answer a : q.getAnswer())
				a = answerService.create(a);	
	}
	
	private void updateAnswers(Question to, Question from) {
		deleteAnswers(to);
		if(from.getAnswer() != null)
			for(Answer a : from.getAnswer())
				to.getAnswer().add(answerService.create(a));	
	}
	
	private List<String> getStringsAnswer(List<Answer> answers) {
		List<String> sAnswers = new ArrayList<String>();
		for(Answer a : answers) 
			sAnswers.add(a.getName());
		return sAnswers;
	}
}
