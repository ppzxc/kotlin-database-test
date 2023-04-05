Feature: UsersService

    Scenario: 회원가입
        Given 유저 1명이 회원가입된 상태
        When 중복 이메일로 회원가입을 요청 하면
        Then 회원 가입에 실패한다.
