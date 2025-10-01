package com.saksham.portal.groups.dto;

import java.util.List;

public record CreateGroupRequest(
    String name,
    String description,
    List<Long> userIds 
){
}
