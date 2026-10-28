import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import {
	PracticeMode,
	PracticeOperation,
	PracticeResult,
	PracticeSession,
	SessionApiService
} from './session-api.service';

type ViewState = 'setup' | 'loading' | 'practice' | 'submitting' | 'results' | 'error';

@Component({
	selector: 'app-root',
	standalone: true,
	imports: [
		FormsModule,
		MatButtonModule,
		MatButtonToggleModule,
		MatFormFieldModule,
		MatInputModule,
		MatProgressBarModule,
		MatSlideToggleModule
	],
	templateUrl: './app.component.html',
	styleUrl: './app.component.scss'
})
export class AppComponent implements OnDestroy {
	private readonly api = inject(SessionApiService);

	readonly multipliers = Array.from({ length: 10 }, (_, index) => index + 1);
	readonly timeOptions = [2, 3, 5];
	readonly questionCount = 10;
	participantName = '';
	selectedMultiplier = 5;
	operation: PracticeOperation = 'MULTIPLICATION';
	mode: PracticeMode = 'TABLE';
	timerEnabled = false;
	selectedDuration = 3;
	answers: (number | null)[] = Array(this.questionCount).fill(null);
	session: PracticeSession | null = null;
	result: PracticeResult | null = null;
	state: ViewState = 'setup';
	errorMessage = '';
	currentIndex = 0;
	currentAnswer: number | null = null;
	remainingSeconds = 0;
	timedOut = false;
	private timerId: ReturnType<typeof setInterval> | null = null;

	get canStart(): boolean {
		return this.participantName.trim().length > 0 && this.state !== 'loading';
	}

	get canSubmitCurrent(): boolean {
		return this.currentAnswer !== null && Number.isInteger(Number(this.currentAnswer));
	}

	get timerLabel(): string {
		const minutes = Math.floor(this.remainingSeconds / 60).toString().padStart(2, '0');
		const seconds = (this.remainingSeconds % 60).toString().padStart(2, '0');
		return `${minutes}:${seconds}`;
	}

	get timerProgress(): number {
		const totalSeconds = (this.session?.timeLimitMinutes ?? 0) * 60;
		return totalSeconds === 0 ? 100 : (this.remainingSeconds / totalSeconds) * 100;
	}

	get timerUrgent(): boolean {
		return this.remainingSeconds > 0 && this.remainingSeconds <= 30;
	}

	ngOnDestroy(): void {
		this.clearTimer();
	}

	startPractice(): void {
		if (!this.canStart) {
			return;
		}

		this.state = 'loading';
		this.errorMessage = '';
		this.api.create({
			participantName: this.participantName.trim(),
			multiplier: this.selectedMultiplier,
			operation: this.operation,
			mode: this.mode,
			timeLimitMinutes: this.timerEnabled ? this.selectedDuration : null
		}).subscribe({
			next: (session) => {
				this.session = session;
				this.result = null;
				this.answers = Array(this.questionCount).fill(null);
				this.currentIndex = 0;
				this.currentAnswer = null;
				this.timedOut = false;
				this.state = 'practice';
				this.startTimer(session.timeLimitMinutes);
			},
			error: () => this.fail('Не успяхме да започнем упражнението. Провери дали сървърът работи.')
		});
	}

	advanceQuestion(): void {
		if (!this.session || !this.canSubmitCurrent || this.state !== 'practice') {
			return;
		}

		const currentFactor = this.session.questions[this.currentIndex].factor;
		this.answers[currentFactor - 1] = Number(this.currentAnswer);
		if (this.currentIndex === this.session.questions.length - 1) {
			this.finishPractice(false);
			return;
		}

		this.currentIndex += 1;
		this.currentAnswer = null;
	}

	questionText(factor: number): string {
		if (!this.session) {
			return '';
		}

		const question = this.session.questions.find((item) => item.factor === factor);
		if (!question) {
			return '';
		}

		const operator = this.session.operation === 'MULTIPLICATION' ? '×' : '÷';
		return `${question.leftOperand} ${operator} ${question.rightOperand} =`;
	}

	reset(): void {
		this.clearTimer();
		this.session = null;
		this.result = null;
		this.answers = Array(this.questionCount).fill(null);
		this.currentIndex = 0;
		this.currentAnswer = null;
		this.remainingSeconds = 0;
		this.timedOut = false;
		this.errorMessage = '';
		this.state = 'setup';
	}

	private startTimer(durationMinutes: number | null): void {
		this.clearTimer();
		if (durationMinutes === null) {
			this.remainingSeconds = 0;
			return;
		}

		this.remainingSeconds = durationMinutes * 60;
		this.timerId = setInterval(() => {
			if (this.remainingSeconds <= 1) {
				this.remainingSeconds = 0;
				this.finishPractice(true);
				return;
			}
			this.remainingSeconds -= 1;
		}, 1000);
	}

	private finishPractice(timedOut: boolean): void {
		if (!this.session || this.state !== 'practice') {
			return;
		}

		const currentFactor = this.session.questions[this.currentIndex].factor;
		this.answers[currentFactor - 1] = this.currentAnswer === null
			? null
			: Number(this.currentAnswer);
		this.timedOut = timedOut;
		this.clearTimer();
		this.state = 'submitting';
		this.api.submit(this.session.id, this.answers).subscribe({
			next: (result) => {
				this.result = result;
				this.state = 'results';
			},
			error: () => this.fail('Отговорите не бяха проверени. Опитай отново.')
		});
	}

	private clearTimer(): void {
		if (this.timerId !== null) {
			clearInterval(this.timerId);
			this.timerId = null;
		}
	}

	private fail(message: string): void {
		this.errorMessage = message;
		this.state = 'error';
	}
}
