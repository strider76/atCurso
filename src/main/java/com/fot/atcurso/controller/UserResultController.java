package com.fot.atcurso.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fot.atcurso.component.mapper.result.ResultMapper;
import com.fot.atcurso.dto.result.ResultDTO;
import com.fot.atcurso.exception.IdValueCannotBeReceivedException;
import com.fot.atcurso.exception.NotFoundException;
import com.fot.atcurso.exception.UnequalObjectsException;
import com.fot.atcurso.model.Result;
import com.fot.atcurso.service.result.ResultService;

@RestController
@RequestMapping(value= "/user/{idUser}/result")
public class UserResultController {
	
	@Autowired
	ResultService resultService;
	
	@Autowired
	ResultMapper resultMapper;
	
	@GetMapping
	public List<ResultDTO> findAll(@RequestParam(defaultValue = "0", required= false ) Integer page, 
							 @RequestParam(defaultValue = "10", required= false ) Integer size,
							 @PathVariable("idUser") Integer idUser) throws NotFoundException {
		final List<Result> results = resultService.findResultByUser(idUser, PageRequest.of(page, size));
		return resultMapper.modelToDto(results);
	}
	
	@GetMapping("/{idResult}")
	public ResultDTO findById(@PathVariable("idUser") Integer idUser,
			 @PathVariable("idResult") Integer idResult) throws NotFoundException {
		final Result result = resultService.findOneResultByUser(idUser, idResult);
		return resultMapper.modelToDto(result);
	}
	
	@PostMapping
	public ResultDTO create(@RequestBody ResultDTO dto,
			@PathVariable("idUser") Integer idUser) throws IdValueCannotBeReceivedException, NotFoundException {
		if(dto.getIdResult() != null) 
			throw new IdValueCannotBeReceivedException("El idResult no se puede recibir en el body");
		Result createResult = resultService.addToUser(idUser, resultMapper.dtoToModel(dto));
		return resultMapper.modelToDto(createResult);
	}
	
	@PutMapping("/{idResult}")
	public void update(@PathVariable("idUser") Integer idUser,
			@PathVariable("idResult") Integer idResult, 
		    @RequestBody ResultDTO dto) throws IdValueCannotBeReceivedException, NotFoundException {
		if(dto.getIdResult() != null) 
			throw new IdValueCannotBeReceivedException("El idResult no se puede recibir en el body");
		resultService.updateToUser(idUser, idResult, resultMapper.dtoToModel(dto));
	}
	
	@DeleteMapping("/{idResult}")
	public void delete(@PathVariable("idUser") Integer idUser,
			@PathVariable("idResult") Integer idResult, 
			@RequestBody ResultDTO dto) throws NotFoundException, UnequalObjectsException {
		resultService.deleteToUser(idUser, idResult, resultMapper.dtoToModel(dto));
	}
}
