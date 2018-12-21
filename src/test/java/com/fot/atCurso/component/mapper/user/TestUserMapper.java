package com.fot.atCurso.component.mapper.user;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.fot.atCurso.dto.user.UserDTO;
import com.fot.atCurso.exception.NotFoundException;
import com.fot.atCurso.model.User;
import org.springframework.beans.factory.annotation.Autowired;


@RunWith(MockitoJUnitRunner.class)
public class TestUserMapper {

	private static final String NAME = "Pepe";
	
	@InjectMocks
	@Autowired
    private UserMapper mapper;
	
	@Mock
	DozerBeanMapper dozer;
	
	@Test
	public void testDTOToModel() throws NotFoundException {
		final UserDTO userDTO = new UserDTO();
		userDTO.setName(NAME);
		final User user = new User();
		user.setName(NAME);
		Mockito.when(dozer.map(userDTO, User.class)).thenReturn(user);
		
		final User res = mapper.dtoToModel(userDTO);
		Assert.assertEquals(NAME, res.getName());
	}
}
