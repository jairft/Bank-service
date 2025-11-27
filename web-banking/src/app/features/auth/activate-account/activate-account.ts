import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { ErrorMessageComponent } from '../../../shared/components/error-message/error-message';
import { SuccessMessageComponent } from '../../../shared/components/success-message/success-message';

@Component({
  selector: 'app-activate-account',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    ErrorMessageComponent,
    SuccessMessageComponent
  ],
  templateUrl: './activate-account.html',
  styleUrls: ['./activate-account.scss']
})
export class ActivateAccountComponent {
  activateForm: FormGroup;
  hidePassword = true;

  isLoading = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.activateForm = this.fb.group({
      activationToken: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get activationToken() {
    return this.activateForm.get('activationToken');
  }

  get password() {
    return this.activateForm.get('password');
  }

  onSubmit() {
    // Limpa mensagens antigas
    this.errorMessage = '';
    this.successMessage = '';

    if (this.activateForm.invalid) {
      this.activateForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    const body = {
      activationToken: this.activationToken?.value,
      password: this.password?.value
    };

    this.http.post('/api/auth/activate', body).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.successMessage = res?.message || 'Conta ativada com sucesso!';
        // Redireciona após 2 segundos
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => this.handleError(err)
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  private handleError(err: any) {
    this.isLoading = false;

    // Extrai mensagem de forma confiável
    if (err.error) {
      if (typeof err.error === 'string') {
        this.errorMessage = err.error;
      } else if (err.error.message) {
        this.errorMessage = err.error.message;
      } else {
        this.errorMessage = JSON.stringify(err.error);
      }
    } else {
      this.errorMessage = 'Erro desconhecido. Tente novamente.';
    }
  }
}
