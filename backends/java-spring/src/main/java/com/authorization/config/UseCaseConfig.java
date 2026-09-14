/**
 * This is a program developed by BobTabo.
 *
 * Copyright (c) 2026 BobTabo. All Rights Reserved.
 */
package com.authorization.config;

import com.authorization.domain.client.mappers.ClientApiMapper;
import com.authorization.domain.client.mappers.ClientDtoMapper;
import com.authorization.domain.client.mappers.ClientConditionMapper;
import com.authorization.domain.client.repositories.ClientRepository;
import com.authorization.domain.client.repositories.JwtHistoryRepository;
import com.authorization.domain.gate.repositories.GateRepository;
import com.authorization.domain.invitation.repositories.InvitationAuthRepository;
import com.authorization.domain.invitation.repositories.InvitationRepository;
import com.authorization.domain.notification.mappers.NotificationConditionMapper;
import com.authorization.domain.notification.mappers.NotificationCreateMapper;
import com.authorization.domain.notification.repositories.NotificationRepository;
import com.authorization.domain.staff.mappers.SocialDtoMapper;
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

    /**
     * {@link StaffService} を組み立てます。
     *
     * @param repository スタッフRepository
     * @param conditionMapper DTO→Condition マッパー
     * @param apiMapper Entity→ValueObject マッパー
     * @return スタッフService
     */
    @Bean
    public StaffService staffService(StaffRepository repository, StaffConditionMapper conditionMapper,
            StaffApiMapper apiMapper) {
        return new StaffService(repository, conditionMapper, apiMapper);
    }

    /**
     * {@link ClientService} を組み立てます。
     *
     * @param repository クライアントRepository
     * @param conditionMapper DTO→Condition マッパー
     * @param apiMapper Entity→ValueObject マッパー
     * @param dtoMapper DTO→Entity マッパー
     * @return クライアントService
     */
    @Bean
    public ClientService clientService(ClientRepository repository, ClientConditionMapper conditionMapper,
            ClientApiMapper apiMapper, ClientDtoMapper dtoMapper) {
        return new ClientService(repository, conditionMapper, apiMapper, dtoMapper);
    }

    /**
     * {@link JwtHistoryService} を組み立てます。
     *
     * @param repository JWT履歴Repository
     * @return JWT履歴Service
     */
    @Bean
    public JwtHistoryService jwtHistoryService(JwtHistoryRepository repository) {
        return new JwtHistoryService(repository);
    }

    /**
     * {@link NotificationService} を組み立てます。
     *
     * @param notificationRepository 通知Repository
     * @param staffRepository スタッフRepository
     * @param conditionMapper DTO→Condition マッパー
     * @param createMapper DTO→Entity マッパー
     * @return 通知Service
     */
    @Bean
    public NotificationService notificationService(NotificationRepository notificationRepository,
            StaffRepository staffRepository, NotificationConditionMapper conditionMapper,
            NotificationCreateMapper createMapper) {
        return new NotificationService(notificationRepository, staffRepository, conditionMapper, createMapper);
    }

    /**
     * {@link InvitationService} を組み立てます。
     *
     * @param invitationRepository 招待Repository
     * @param invitationAuthRepository 招待認証Repository
     * @param cfg アプリケーション設定
     * @return 招待Service
     */
    @Bean
    public InvitationService invitationService(InvitationRepository invitationRepository,
            InvitationAuthRepository invitationAuthRepository, AppConfig cfg) {
        return new InvitationService(invitationRepository, invitationAuthRepository, cfg.app().frontendUrl());
    }

    /**
     * {@link GateService} を組み立てます。
     *
     * @param clientRepository クライアントRepository
     * @param gateRepository 認可Repository
     * @param historyRepository JWT履歴Repository
     * @param cfg アプリケーション設定
     * @return 認可Service
     */
    @Bean
    public GateService gateService(ClientRepository clientRepository, GateRepository gateRepository,
            JwtHistoryRepository historyRepository, AppConfig cfg) {
        return new GateService(clientRepository, gateRepository, historyRepository, cfg.jwt());
    }

    /**
     * {@link AuthService} を組み立てます。
     *
     * @param staffRepository スタッフRepository
     * @param invitationAuthRepository 招待認証Repository
     * @param conditionMapper DTO→Condition マッパー
     * @param apiMapper Entity→ValueObject マッパー
     * @param socialDtoMapper ソーシャルDTO→Entity マッパー
     * @return 認証Service
     */
    @Bean
    public AuthService authService(StaffRepository staffRepository,
            InvitationAuthRepository invitationAuthRepository, StaffConditionMapper conditionMapper,
            StaffApiMapper apiMapper, SocialDtoMapper socialDtoMapper) {
        return new AuthService(staffRepository, invitationAuthRepository, conditionMapper, apiMapper,
                socialDtoMapper);
    }
}
