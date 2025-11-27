import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

/* Angular Material */
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/* Componentes de mensagem */
import { ErrorMessageComponent } from '../../shared/components/error-message/error-message';
import { SuccessMessageComponent } from '../../shared/components/success-message/success-message';

/* Serviço de autenticação */
import { AuthService, User } from '../../core/services/auth';

@Component({
  selector: 'app-reset-transactional-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    ErrorMessageComponent,
    SuccessMessageComponent
  ],
  templateUrl: './reset-transactional-password.html',
  styleUrls: ['./reset-transactional-password.scss']
})
export class ResetTransactionalPasswordComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  step: 1 | 2 = 1;
  currentUser: User | null = null;
  resetForm: FormGroup;

  constructor() {
    this.currentUser = this.authService.currentUserValue;
    this.resetForm = this.fb.group({
      token: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordsMatchValidator });
  }

  passwordsMatchValidator(form: FormGroup) {
    const newPass = form.get('newPassword')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return newPass && confirm && newPass === confirm ? null : { passwordsMismatch: true };
  }

  generateToken() {
    if (!this.currentUser) {
      this.showError('Usuário não está logado');
      return;
    }

    const headers = {
      Authorization: `Bearer ${this.currentUser.token}`,
      'X-User-Id': this.currentUser.userId.toString()
    };

    this.isLoading = true;
    this.clearMessages();

    this.http.post(
      'http://localhost:8080/api/transactional-password/resend-token',
      {},
      { headers }
    ).subscribe({
      next: () => this.showSuccess('Token gerado! Confira seu E-mail de cadastro.', () => this.step = 2),
      error: (err) => this.handleError(err)
    });
  }

  resetPassword() {
    if (!this.currentUser || this.resetForm.invalid) return;

    const headers = {
      Authorization: `Bearer ${this.currentUser.token}`,
      'X-User-Id': this.currentUser.userId.toString()
    };

    this.isLoading = true;
    this.clearMessages();

    this.http.post(
      'http://localhost:8080/api/transactional-password/reset',
      this.resetForm.value,
      { headers }
    ).subscribe({
      next: () => this.showSuccess('Senha redefinida com sucesso!', () => 
        setTimeout(() => this.router.navigate(['/dashboard']), 3000)
      ),
      error: (err) => this.handleError(err)
    });
  }

  private handleError(err: any) {
    this.isLoading = false;
    let msg = 'Erro desconhecido. Tente novamente.';

    if (err.error) {
      if (Array.isArray(err.error.messages)) {
        msg = err.error.messages.join('; ');
      } else if (err.error.message) {
        msg = err.error.message;
      } else if (typeof err.error === 'string') {
        msg = err.error;
      }
    } else {
      msg = 'Erro de conexão. Tente novamente.';
    }

    this.showError(msg);
  }

  private showError(msg: string) {
    this.errorMessage = msg;
    setTimeout(() => this.errorMessage = null, 5000);
  }

  private showSuccess(msg: string, callback?: () => void) {
    this.isLoading = false;
    this.successMessage = msg;
    if (callback) callback();
    setTimeout(() => this.successMessage = null, 5000);
  }

  private clearMessages() {
    this.errorMessage = null;
    this.successMessage = null;
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }
}
