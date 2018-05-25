package com.fot.atCurso.controller;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fot.atCurso.component.mapper.user.UserMapper;
import com.fot.atCurso.dto.user.UserDTO;
import com.fot.atCurso.dto.user.UserPostDTO;
import com.fot.atCurso.exceptions.IdValueCannotBeReceivedException;
import com.fot.atCurso.exceptions.NotFoundException;
import com.fot.atCurso.exceptions.ObjectsDoNotMatchException;
import com.fot.atCurso.exceptions.ParametersNotAllowedException;
import com.fot.atCurso.exceptions.UniqueValueViolationException;
import com.fot.atCurso.model.User;
import com.fot.atCurso.service.user.UserService;
 
@RestController
@RequestMapping(value= "/user")
public class UserController {

	private static final Integer maxSize = new Integer(10);
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserMapper userMapper;
	
	@RequestMapping(method = RequestMethod.GET)
	public Set<UserDTO> findAll(@RequestParam(defaultValue = "0", required= false ) Integer page, 
							 @RequestParam(defaultValue = "10", required= false ) Integer size) throws ParametersNotAllowedException {
		if(page < 0 || size <= 0 || size > maxSize)
			throw new ParametersNotAllowedException("Los parámetros introducidos contienen valores no permitidos, page mayor o igual a 0 y size entre 1 y " + maxSize + " incluídos");
		final Set<User> users = userService.findAll(PageRequest.of(page, size));
		return userMapper.modelToDto(users);
	}
	
	@RequestMapping(value = "/{idUser}", method = RequestMethod.GET)
	public UserDTO findById(@PathVariable("idUser") Integer id) throws NotFoundException {
		final Optional<User> user = userService.findById(id);
		if (user.isPresent()) return userMapper.modelToDto(user.get());
		throw new NotFoundException("El usuario no existe");
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public UserDTO create(@RequestBody UserPostDTO dto) throws IdValueCannotBeReceivedException, UniqueValueViolationException {
		if(dto.getIdUser() != null) 
			throw new IdValueCannotBeReceivedException("El idUser no se puede recibir");
		if(userService.findByEmail(dto.getEmail()).isPresent())
			throw new UniqueValueViolationException("El email introducido ya existe");
		final User user = userMapper.dtoToModel(dto);
		final User createUser = userService.create(user);
		return userMapper.modelToDto(createUser);
	}
	
	@RequestMapping(value = "/{idUser}", method = RequestMethod.PUT)
	public void update(@PathVariable("idUser") Integer id, @RequestBody UserPostDTO dto) throws IdValueCannotBeReceivedException, NotFoundException, UniqueValueViolationException {
		if(dto.getIdUser() != null) 
			throw new IdValueCannotBeReceivedException("El idUser no se puede recibir en el body");
		final Optional<User> user = userService.findById(id);
		user.orElseThrow(() -> new NotFoundException("El usuario no existe"));
		if (user.get().getEmail() != dto.getEmail() && userService.findByEmail(dto.getEmail()).isPresent()) 
			throw new UniqueValueViolationException("El email a cambiar ya existe");
		userService.setValues(user.get(), userMapper.dtoToModel(dto));
		userService.update(user.get());
	}
	
	@RequestMapping(value = "/{idUser}", method = RequestMethod.DELETE)
	public void delete(@PathVariable("idUser") Integer id, @RequestBody UserDTO dto) throws NotFoundException, ObjectsDoNotMatchException {
		final Optional<User> user = userService.findById(id);
		user.orElseThrow(() -> new NotFoundException("El usuario no existe"));
		if(!userService.isEqual(userMapper.dtoToModel(dto), user.get())) 
			throw new ObjectsDoNotMatchException("El usuario recibido no coincide con el almacenado");
		userService.delete(user.get());
	}
}
