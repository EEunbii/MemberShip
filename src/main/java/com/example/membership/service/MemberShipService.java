package com.example.membership.service;

import com.example.membership.constant.Role;
import com.example.membership.dto.MemberShipDTO;
import com.example.membership.entity.MemberShip;
import com.example.membership.repository.MemberShipRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.asm.Advice;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberShipService {


    private final MemberShipRepository membershipRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    //회원가입 : 컨트롤러에서 dto를 입력받아 entity로 변환하여 repository의
    //save를 이용해서 저장한다.
    // 반환값은 뭐로 할까? : dto 전체를 반환으로 하자
    // 만약, 회원 가입이 되어있으면 어떻게 할 껀가?
    public MemberShipDTO saveMember(MemberShipDTO memberShipDTO) {


        // 사용자가 이미 있는지 확인
        // 가입하려는 email로 이미 사용자가 가입이 되어있는지 확인한다.
        MemberShip memberShip =
                membershipRepository.findByEmail(memberShipDTO.getEmail());

        if (memberShip != null) {     //확인 했더니 이미 가입이 되어있다면

            throw new IllegalStateException("이미 가입된 회원입니다.");
        }

        memberShip =
                modelMapper.map(memberShipDTO, MemberShip.class);

        //일반유저
        memberShip.setRole(Role.ADMIN);
        memberShip.setPassword(passwordEncoder.encode(memberShipDTO.getPassword()));


        memberShip =
                membershipRepository.save(memberShip);  //저장


        return modelMapper.map(memberShip, MemberShipDTO.class);
    }

    //로그인
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.info("유저디테일 서비스로 들어온 이메일 : " + email);  //안들어왔다면 input창으로 넣은 값이 도달을 못함
        MemberShip memberShip =
                this.membershipRepository.findByEmail(email);

        if(memberShip == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        log.info("현재 찾은 회원정보" +  memberShip);
        // 권한 처리
        String role = "";
        if ("ADMIN".equals(memberShip.getRole().name())){
            log.info("관리자");
            role = Role.ADMIN.name();
        } else {
            log.info("일반유저");
            role = Role.USER.name();
        }
        return User.builder()
                .username(memberShip.getEmail())
                .password(memberShip.getPassword())   //<input name="password">
                .roles(role)
                .build();
    }