import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule, Router } from '@angular/router';

import { ErrorMessageComponent } from '../../../shared/components/error-message/error-message';
import { SuccessMessageComponent } from '../../../shared/components/success-message/success-message';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.scss'],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    RouterModule,
    ErrorMessageComponent,
    SuccessMessageComponent
  ]
})
export class ResetPasswordComponent {
  step: 'request' | 'reset' = 'request';

  email: string = '';
  token: string = '';
  newPassword: string = '';
  confirmNewPassword: string = '';

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private http: HttpClient, private router: Router) {}

  sendRecoveryEmail() {
    if (!this.email) {
      this.errorMessage = 'Digite um email válido';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post('/api/auth/forgot-password', { email: this.email }, { responseType: 'text' })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = res;
          this.step = 'reset';
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Usuário não encontrado';
        }
      });
  }

  resetPassword() {
    if (!this.token || !this.newPassword || !this.confirmNewPassword) {
      this.errorMessage = 'Preencha todos os campos';
      return;
    }

    if (this.newPassword !== this.confirmNewPassword) {
      this.errorMessage = 'As senhas não conferem';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post('/api/auth/reset-password', {
      token: this.token,
      newPassword: this.newPassword,
      confirmNewPassword: this.confirmNewPassword
    }, { responseType: 'text' })
      .subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = res;
          this.step = 'request';
          this.email = '';
          this.token = '';
          this.newPassword = '';
          this.confirmNewPassword = '';
        },
        error: (err) => this.handleError(err)
      });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

   private handleError(err: any) {
    this.isLoading = false;
    if (err.error?.message) this.errorMessage = err.error.message;
    else if (typeof err.error === 'string') this.errorMessage = err.error;
    else this.errorMessage = 'Erro desconhecido. Tente novamente.';

  }
}
