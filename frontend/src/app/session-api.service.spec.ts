import '@angular/compiler';
import { HttpClient } from '@angular/common/http';
import { Injector, runInInjectionContext } from '@angular/core';
import { firstValueFrom, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PracticeResult, PracticeSession, SessionApiService } from './session-api.service';

describe('SessionApiService', () => {
  let injector: Injector;
  let post: ReturnType<typeof vi.fn>;
  let service: SessionApiService;

  beforeEach(() => {
    post = vi.fn().mockReturnValue(of(null));
    injector = Injector.create({
      providers: [{ provide: HttpClient, useValue: { post } }]
    });
    service = runInInjectionContext(injector, () => new SessionApiService());
  });

  it('creates a session with the selected settings', async () => {
    const request = {
      participantName: 'Ива',
      multiplier: 6,
      operation: 'MULTIPLICATION' as const,
      mode: 'TABLE' as const,
      timeLimitMinutes: 2
    };
    const response: PracticeSession = {
      id: 12,
      ...request,
      questions: [{ factor: 1, leftOperand: 6, rightOperand: 1 }]
    };
    post.mockReturnValue(of(response));

    await expect(firstValueFrom(service.create(request))).resolves.toEqual(response);
    expect(post).toHaveBeenCalledWith('/api/sessions', request);
  });

  it('submits answers with their factor numbers, including unanswered questions', async () => {
    const response: PracticeResult = {
      correctCount: 1,
      incorrectCount: 2,
      details: []
    };
    post.mockReturnValue(of(response));

    await expect(firstValueFrom(service.submit(12, [42, null, 0]))).resolves.toEqual(response);
    expect(post).toHaveBeenCalledWith('/api/sessions/12/submit', {
      answers: [
        { factor: 1, answer: 42 },
        { factor: 2, answer: null },
        { factor: 3, answer: 0 }
      ]
    });
  });
});