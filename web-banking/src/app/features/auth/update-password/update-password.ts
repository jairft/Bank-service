import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

import { ErrorMessageComponent } from '../../../shared/components/error-message/error-message';
import { SuccessMessageComponent } from '../../../shared/components/success-message/success-message';

/* Angular Material */
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

/* Serviço Auth */
import { AuthService, User } from '../../../core/services/auth';

@Component({
  selector: 'app-update-password',
  standalone: true,
  templateUrl: './update-password.html',
  styleUrls: ['./update-password.scss'],
  imports: [
    CommonModule,
    FormsModule,
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
export class UpdatePasswordComponent implements OnInit {

  private http = inject(HttpClient);
  private router = inject(Router);
  private authService: AuthService = inject(AuthService);

  /* Formulário */
  currentPassword: string = '';
  newPassword: string = '';
  confirmNewPassword: string = '';

  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  currentUser: User | null = null;

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    if (!this.currentUser) {
      this.router.navigate(['/login']);
    }
  }

  updatePassword(form: NgForm): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (!this.currentPassword || !this.newPassword || !this.confirmNewPassword) {
      this.errorMessage = 'Preencha todos os campos ⚠️';
      return;
    }

    if (this.newPassword !== this.confirmNewPassword) {
      this.errorMessage = 'Nova senha e confirmação não coincidem ⚠️';
      return;
    }

    if (!this.currentUser) return;

    const token = this.currentUser.token;
    const headers = { Authorization: `Bearer ${token}` };
    const body = {
      currentPassword: this.currentPassword,
      newPassword: this.newPassword,
      confirmNewPassword: this.confirmNewPassword
    };

    this.isLoading = true;

    this.http.post('/api/auth/update-password', body, { headers, responseType: 'text' })
      .subscribe({
        next: (res: string) => {
          this.isLoading = false;
          this.successMessage = res;

          // Reseta o formulário
          form.resetForm();
          this.currentPassword = '';
          this.newPassword = '';
          this.confirmNewPassword = '';
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Ocorreu um erro inesperado ⚠️';
        }
      });
  }

  goToDashboard() {
    this.router.navigate(['/dashboard']);
  }
}
