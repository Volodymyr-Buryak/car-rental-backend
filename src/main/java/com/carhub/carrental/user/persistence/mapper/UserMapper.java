package com.carhub.carrental.user.persistence.mapper;

import org.mapstruct.*;
import com.carhub.carrental.user.persistence.entity.User;
import com.carhub.carrental.user.web.dto.client.profile.section.AccountResponse;
import com.carhub.carrental.user.web.dto.client.profile.section.UpdatePersonalInfoRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "stripPersonName")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "stripPersonName")
    @Mapping(target = "middleName", source = "middleName", qualifiedByName = "stripPersonName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    void updatePersonalInfo(UpdatePersonalInfoRequest request, @MappingTarget User user);

    AccountResponse toAccountResponse(User user);

    @Named("stripPersonName")
    default String stripPersonName(String name) {
        return name == null ? null : name.strip();
    }
}
