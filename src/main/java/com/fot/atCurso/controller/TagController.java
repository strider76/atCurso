package com.fot.atCurso.controller;

import java.util.Optional;
import java.util.Set;

import org.hibernate.exception.ConstraintViolationException;
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

import com.fot.atCurso.component.mapper.tag.TagMapper;
import com.fot.atCurso.dto.tag.TagDTO;
import com.fot.atCurso.exceptions.IdValueCannotBeReceivedException;
import com.fot.atCurso.exceptions.NotFoundException;
import com.fot.atCurso.exceptions.ObjectsDoNotMatchException;
import com.fot.atCurso.exceptions.ParametersNotAllowedException;
import com.fot.atCurso.model.Tag;
import com.fot.atCurso.service.tag.TagService;

@RestController
@RequestMapping(value= "/tag")
public class TagController {
	
	@Autowired
	TagService tagService;
	
	@Autowired
	TagMapper tagMapper;
	
	@GetMapping
	public Set<TagDTO> findAll(@RequestParam(defaultValue = "0", required= false ) Integer page, 
							 @RequestParam(defaultValue = "10", required= false ) Integer size) throws ParametersNotAllowedException {
		final Set<Tag> tags = tagService.findAll(PageRequest.of(page, size));
		return tagMapper.modelToDto(tags);
	}
	
	@GetMapping("/{idTag}")
	public TagDTO findById(@PathVariable("idTag") Integer id) throws NotFoundException {
		final Optional<Tag> tag = tagService.findById(id);
		if (tag.isPresent()) return tagMapper.modelToDto(tag.get());
		throw new NotFoundException("El tag no existe");
	}
	
	@PostMapping
	public TagDTO create(@RequestBody TagDTO dto) throws IdValueCannotBeReceivedException, ConstraintViolationException, NotFoundException {
		if(dto.getIdTag() != null) 
			throw new IdValueCannotBeReceivedException("El idTag no se puede recibir");
		final Tag tag = tagMapper.dtoToModel(dto);
		final Tag createTag = tagService.create(tag);
		return tagMapper.modelToDto(createTag);
	}
	
	@PutMapping("/{idTag}")
	public void update(@PathVariable("idTag") Integer id, @RequestBody TagDTO dto) throws IdValueCannotBeReceivedException, NotFoundException {
		if(dto.getIdTag() != null) 
			throw new IdValueCannotBeReceivedException("El idTag no se puede recibir en el body");
		final Optional<Tag> tag = tagService.findById(id);
		tag.orElseThrow(() -> new NotFoundException("El usuario no existe"));
		tagService.setValues(tag.get(), tagMapper.dtoToModel(dto));
		tagService.update(tag.get());
	}
	
	@DeleteMapping("/{idTag}")
	public void delete(@PathVariable("idTag") Integer id, @RequestBody TagDTO dto) throws NotFoundException, ObjectsDoNotMatchException {
		final Optional<Tag> tag = tagService.findById(id);
		tag.orElseThrow(() -> new NotFoundException("El tag no existe"));
		if(!tagService.isEqual(tagMapper.dtoToModel(dto), tag.get())) 
			throw new ObjectsDoNotMatchException("El tag recibido no coincide con el almacenado");
		tagService.delete(tag.get());
	}
}
