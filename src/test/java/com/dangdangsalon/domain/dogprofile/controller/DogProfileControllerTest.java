package com.dangdangsalon.domain.dogprofile.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.service.DogProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = DogProfileController.class)
@MockBean(JpaMetamodelMappingContext.class)
class DogProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DogProfileService dogProfileService;

    @Test
    @DisplayName("견적 요청 강아지 프로필 조회 테스트")
    public void getEstimateRequestDogProfilesSuccess() throws Exception {
        // Mock 사용자 설정
        Long mockUserId = 1L;
        CustomOAuth2User customOAuth2User = Mockito.mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        DogProfileResponseDto dog1 = new DogProfileResponseDto(1L, "dog1-image-key", "댕댕이");
        DogProfileResponseDto dog2 = new DogProfileResponseDto(2L, "dog2-image-key", "구름이");

        List<DogProfileResponseDto> mockResponse = List.of(dog1, dog2);

        when(dogProfileService.getDogProfilesByUserId(anyLong())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/estimaterequest/dogprofiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .principal(authentication))
                .andExpect(status().isOk()) // HTTP 200 확인
                .andExpect(jsonPath("$.response[0].dogProfileId").value(1L)) // 첫 번째 강아지 프로필 ID 확인
                .andExpect(jsonPath("$.response[0].name").value("댕댕이")) // 첫 번째 강아지 이름 확인
                .andExpect(jsonPath("$.response[1].dogProfileId").value(2L)) // 두 번째 강아지 프로필 ID 확인
                .andExpect(jsonPath("$.response[1].name").value("구름이")); // 두 번째 강아지 이름 확인
    }
}
