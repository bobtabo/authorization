package service

import (
	"authorization-go/internal/repository"
	"authorization-go/pkg/apperror"
)

type InvitationService struct {
	repo *repository.InvitationRepository
}

func NewInvitationService(repo *repository.InvitationRepository) *InvitationService {
	return &InvitationService{repo: repo}
}

func (s *InvitationService) Current() (*repository.InvitationResult, error) {
	result, err := s.repo.GetCurrent()
	if err != nil {
		return nil, err
	}
	if result == nil {
		return nil, apperror.NotFound("invitation_not_found")
	}
	return result, nil
}

func (s *InvitationService) Issue() (*repository.InvitationResult, error) {
	return s.repo.Issue()
}

func (s *InvitationService) FindByToken(token string) (*repository.InvitationResult, error) {
	if token == "" {
		return nil, apperror.BadRequest("invitation_invalid")
	}
	result, err := s.repo.FindByToken(token)
	if err != nil {
		return nil, err
	}
	if result == nil {
		return nil, apperror.BadRequest("invitation_invalid")
	}
	return result, nil
}
