package com.carhub.carrental.user.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.carhub.carrental.user.persistence.entity.ClientProfile;
import com.carhub.carrental.user.web.dto.client.ClientProfileResponse;

@Mapper
public interface ClientProfileMapper {

    @Mapping(target = ".", source = "user")
    @Mapping(target = "avatarUrl", ignore = true)
    ClientProfileResponse toResponse(ClientProfile entity);

}
