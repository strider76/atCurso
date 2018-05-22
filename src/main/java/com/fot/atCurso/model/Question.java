package com.fot.atCurso.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Question {

	public static final String FIELD_IDTAG = "idTag";
	public static final String FIELD_IDDIFFICULTY = "idDifficulty";
	public static final String FIELD_IDQUESTION = "idQuestion";
	
	@Id
	@GeneratedValue
	@Column(name=FIELD_IDQUESTION)
	private Integer idQuestion;
	
	@Column(nullable = false)
	private String name;
	
	@JoinColumn(name = FIELD_IDTAG)
	@ManyToOne(fetch = FetchType.LAZY)
	private Tag tag;
	
	@JoinColumn(name = FIELD_IDDIFFICULTY)
	@ManyToOne(fetch = FetchType.LAZY)
	private Difficulty dificulty;
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name=FIELD_IDQUESTION, referencedColumnName=FIELD_IDQUESTION)
	private List<Answer> answer;
}
