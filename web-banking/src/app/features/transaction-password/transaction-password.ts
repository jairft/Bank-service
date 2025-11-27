// ✅ TransactionalPasswordComponent atualizado
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

import { ErrorMessageComponent } from '../../shared/components/error-message/error-message';
import { SuccessMessageComponent } from '../../shared/components/success-message/success-message';

/* Angular Material */
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/* Serviços */
import { AuthService, User } from '../../core/services/auth';

@Component({
  selector: 'app-transactional-password',
  standalone: true,
  templateUrl: './transaction-password.html',
  styleUrls: ['./transaction-password.scss'],
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
  ]
})
export class TransactionalPasswordComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private authService = inject(AuthService);

  transactionForm: FormGroup;
  updateForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  currentUser: User | null = null;

  status: 'NOT_SET' | 'ACTIVE' | 'BLOCKED' | null = null;
  showInitialForm = false;
  showUpdateForm = false;

  submittedTransaction = false;
  submittedUpdate = false;

  constructor() {
    this.transactionForm = this.fb.group({
      password: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
      confirmPassword: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]]
    }, { validators: this.passwordsMatchValidator });

    this.updateForm = this.fb.group({
      currentPassword: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
      newPassword: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
      confirmPassword: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]]
    }, { validators: this.passwordsMatchValidatorUpdate });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    if (!this.currentUser) this.router.navigate(['/login']);
    else this.checkPasswordStatus();
  }

  // GETTERS
  get password() { return this.transactionForm.get('password'); }
  get confirmPassword() { return this.transactionForm.get('confirmPassword'); }
  get currentPassword() { return this.updateForm.get('currentPassword'); }
  get newPassword() { return this.updateForm.get('newPassword'); }
  get confirmNewPassword() { return this.updateForm.get('confirmPassword'); }

  // Validators
  passwordsMatchValidator(form: FormGroup) {
    const pass = form.get('password')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return pass && confirm && pass === confirm ? null : { passwordsMismatch: true };
  }

  passwordsMatchValidatorUpdate(form: FormGroup) {
    const newPass = form.get('newPassword')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return newPass && confirm && newPass === confirm ? null : { passwordsMismatch: true };
  }

  // Verifica status da senha
  checkPasswordStatus() {
    if (!this.currentUser) return;

    const headers = {
      Authorization: `Bearer ${this.currentUser.token}`,
      'X-User-Id': this.currentUser.userId.toString()
    };

    this.http.get('http://localhost:8080/api/transactional-password/status', { headers, responseType: 'text' })
      .subscribe({
        next: (res) => {
          this.status = res as 'NOT_SET' | 'ACTIVE' | 'BLOCKED';
          this.showInitialForm = this.status === 'NOT_SET';
          this.showUpdateForm = false;

          if (this.status === 'BLOCKED') {
            this.errorMessage = 'Senha bloqueada. Tente novamente mais tarde.';
          }
        },
        error: (err) => this.handleError(err)
      });
  }

  // Cadastrar senha pela primeira vez
  onSubmit(): void {
    this.submittedTransaction = true;
    if (this.transactionForm.invalid || !this.currentUser) return;

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const payload = {
      password: this.password?.value,
      confirmPassword: this.confirmPassword?.value
    };

    const headers = {
      Authorization: `Bearer ${this.currentUser.token}`,
      'X-User-Id': this.currentUser.userId.toString()
    };

    this.http.post<{ message: string }>('http://localhost:8080/api/transactional-password/set', payload, { headers })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = res.message;

          this.transactionForm.reset({ password: '', confirmPassword: '' });
          this.password?.setErrors(null);
          this.confirmPassword?.setErrors(null);
          this.submittedTransaction = false;

          setTimeout(() => this.router.navigate(['/dashboard']), 3000);
        },
        error: (err) => this.handleError(err)
      });
  }

  // Mostrar formulário de atualização
  showUpdate() {
    this.showUpdateForm = true;
    this.showInitialForm = false;
    this.successMessage = null;
    this.errorMessage = null;
  }

  // Atualizar senha
  onUpdate(): void {
    this.submittedUpdate = true;
    if (this.updateForm.invalid || !this.currentUser) return;
    if (this.currentPassword?.value === this.newPassword?.value) {
      this.errorMessage = 'A nova senha não pode ser igual à atual';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const payload = {
      currentPassword: this.currentPassword?.value,
      newPassword: this.newPassword?.value,
      confirmPassword: this.confirmNewPassword?.value
    };

    const headers = {
      Authorization: `Bearer ${this.currentUser.token}`,
      'X-User-Id': this.currentUser.userId.toString()
    };

    this.http.put<{ message: string }>('http://localhost:8080/api/transactional-password/change', payload, { headers })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = res.message;

          this.updateForm.reset({ currentPassword: '', newPassword: '', confirmPassword: '' });
          this.currentPassword?.setErrors(null);
          this.newPassword?.setErrors(null);
          this.confirmNewPassword?.setErrors(null);
          this.submittedUpdate = false;

          setTimeout(() => this.router.navigate(['/dashboard']), 3000);
        },
        error: (err) => this.handleError(err)
      });
  }

  private handleError(err: any) {
    this.isLoading = false;
    if (err.error?.message) this.errorMessage = err.error.message;
    else if (typeof err.error === 'string') this.errorMessage = err.error;
    else this.errorMessage = 'Erro desconhecido. Tente novamente.';

    setTimeout(() => this.errorMessage = null, 5000);
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  onForgotPassword(): void {
    this.router.navigate(['/reset-transactional-password']);
  }
}
