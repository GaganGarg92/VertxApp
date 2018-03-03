package com.example.VertxApp;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserTest {

    private String name;
    private String email;
    private String phone;
    private String question;

}
