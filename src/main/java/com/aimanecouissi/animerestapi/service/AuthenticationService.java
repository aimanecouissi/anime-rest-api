package com.aimanecouissi.animerestapi.service;

import com.aimanecouissi.animerestapi.payload.dto.LoginDTO;
import com.aimanecouissi.animerestapi.payload.dto.RegisterDTO;

public interface AuthenticationService {
    String login(LoginDTO loginDTO);

    LoginDTO register(RegisterDTO registerDTO);
}
