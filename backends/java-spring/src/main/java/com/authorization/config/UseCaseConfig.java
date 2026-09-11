/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.config;

import com.authorization.domain.client.mappers.ClientApiMapper;
import com.authorization.domain.client.mappers.ClientConditionMapper;
import com.authorization.domain.client.repositories.ClientRepository;
import com.authorization.domain.client.repositories.JwtHistoryRepository;
import com.authorization.domain.gate.repositories.GateRepository;
import com.authorization.domain.invitation.repositories.InvitationAuthRepository;
import com.authorization.domain.invitation.repositories.InvitationRepository;
import com.authorization.domain.notification.mappers.NotificationConditionMapper;
import com.authorization.domain.notification.repositories.NotificationRepository;
import com.authorization.domain.staff.mappers.StaffApiMapper;
import com.authorization.domain.staff.mappers.StaffConditionMapper;
import com.authorization.domain.staff.repositories.StaffRepository;
import com.authorization.usecases.auth.AuthService;
import com.authorization.usecases.client.ClientService;
import com.authorization.usecases.gate.GateService;
import com.authorization.usecases.invitation.InvitationService;
import com.authorization.usecases.jwthistory.JwtHistoryService;
import com.authorization.usecases.notification.NotificationService;
import com.authorization.usecases.staff.StaffService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 各ドメインの UseCase Service を Bean 登録します。
 * PHP版の {@code AppServiceProvider::register()} のアプリケーションサービス登録に相当します。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public StaffService staffService(StaffRepository repository, StaffConditionMapper conditionMapper,
            StaffApiMapper apiMapper) {
        return new StaffService(repository, conditionMapper, apiMapper);
    }

    @Bean
    public ClientService clientService(ClientRepository repository, ClientConditionMapper conditionMapper,
            ClientApiMapper apiMapper) {
        return new ClientService(repository, conditionMapper, apiMapper);
    }

    @Bean
    public JwtHistoryService jwtHistoryService(JwtHistoryRepository repository) {
        return new JwtHistoryService(repository);
    }

    @Bean
    public NotificationService notificationService(NotificationRepository notificationRepository,
            StaffRepository staffRepository, NotificationConditionMapper conditionMapper) {
        return new NotificationService(notificationRepository, staffRepository, conditionMapper);
    }

    @Bean
    public InvitationService invitationService(InvitationRepository invitationRepository,
            InvitationAuthRepository invitationAuthRepository, AppConfig cfg) {
        return new InvitationService(invitationRepository, invitationAuthRepository, cfg.app().frontendUrl());
    }

    @Bean
    public GateService gateService(ClientRepository clientRepository, GateRepository gateRepository,
            JwtHistoryRepository historyRepository, AppConfig cfg) {
        return new GateService(clientRepository, gateRepository, historyRepository, cfg.jwt());
    }

    @Bean
    public AuthService authService(StaffRepository staffRepository,
            InvitationAuthRepository invitationAuthRepository, StaffConditionMapper conditionMapper,
            StaffApiMapper apiMapper) {
        return new AuthService(staffRepository, invitationAuthRepository, conditionMapper, apiMapper);
    }
}
