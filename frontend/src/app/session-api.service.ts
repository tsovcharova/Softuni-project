import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PracticeSession {
  id: number;
  participantName: string;
  multiplier: number;
  operation: PracticeOperation;
  mode: PracticeMode;
  timeLimitMinutes: number | null;
  questions: PracticeQuestion[];
}

export type PracticeOperation = 'MULTIPLICATION' | 'DIVISION';
export type PracticeMode = 'TABLE' | 'MIXED';

export interface PracticeQuestion {
  factor: number;
  leftOperand: number;
  rightOperand: number;
}

export interface CreateSessionRequest {
  participantName: string;
  multiplier: number;
  operation: PracticeOperation;
  mode: PracticeMode;
  timeLimitMinutes: number | null;
}

export interface AnswerResult {
  factor: number;
  expected: number;
  submitted: number | null;
  correct: boolean;
}

export interface PracticeResult {
  correctCount: number;
  incorrectCount: number;
  details: AnswerResult[];
}

@Injectable({ providedIn: 'root' })
export class SessionApiService {
  private readonly http = inject(HttpClient);

  create(request: CreateSessionRequest): Observable<PracticeSession> {
    return this.http.post<PracticeSession>('/api/sessions', request);
  }

  submit(sessionId: number, answers: (number | null)[]): Observable<PracticeResult> {
    return this.http.post<PracticeResult>(`/api/sessions/${sessionId}/submit`, {
      answers: answers.map((answer, index) => ({ factor: index + 1, answer }))
    });
  }
}