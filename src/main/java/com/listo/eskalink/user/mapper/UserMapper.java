package com.listo.eskalink.user.mapper;

import com.listo.eskalink.user.dto.SignupRequest;
import com.listo.eskalink.user.dto.UserDto;
import com.listo.eskalink.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    @Mapping(target = "verificationTokenExpiresAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User signupRequestToUser(SignupRequest signupRequest);

    UserDto userToUserDto(User user);
}
