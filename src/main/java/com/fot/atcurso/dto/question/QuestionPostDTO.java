package com.fot.atcurso.dto.question;

import java.io.Serializable;
import java.util.List;

import com.fot.atcurso.dto.answer.AnswerPostDTO;

import lombok.Data;

@Data
public class QuestionPostDTO implements Serializable {
	
	private static final long serialVersionUID = 9689751L;
	
	private Integer idQuestion;
	private String name;
	private Integer idTag;
	private Integer idDifficulty;
	private List<AnswerPostDTO> answers;
}
