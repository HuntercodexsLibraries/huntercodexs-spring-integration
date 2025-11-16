package com.huntercodexs.integration.mock.dto;

import lombok.*;

@Data
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestMock {

    private String name;
    private String email;

}

